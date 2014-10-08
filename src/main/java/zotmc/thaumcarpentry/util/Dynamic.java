package zotmc.thaumcarpentry.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.objectweb.asm.Type;

import zotmc.thaumcarpentry.util.Registry.Query;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.reflect.Invokable;

public class Dynamic {
	
	public static <T> Refer<T> refer(Class<?> clz, String field) {
		return new Refer<T>(Klas.ofClass(clz), Utils.nullSupplier(), field);
	}
	public static <T> Refer<T> refer(String clz, String field) {
		return new Refer<T>(Klas.ofName(clz), Utils.nullSupplier(), field);
	}
	
	public static <U> Invoke<U> invoke(Class<?> clz, String method) {
		return new Invoke<U>(Klas.ofClass(clz), Utils.nullSupplier(), method);
	}
	public static <U> Invoke<U> invoke(String clz, String method) {
		return new Invoke<U>(Klas.ofName(clz), Utils.nullSupplier(), method);
	}
	
	public static <T> Construct<T> construct(Class<? extends T> clz) {
		return new Construct<T>(Klas.ofClass(clz));
	}
	public static <T> Construct<T> construct(final String clz) {
		return new Construct<T>(Klas.<T>ofName(clz));
	}
	
	
	
	
	private static class TypedArg<T> {
		final Klas<T> type;
		final T arg;
		private TypedArg(Klas<T> type, T arg) {
			this.type = type;
			this.arg = arg;
		}
		private static <T> TypedArg<T> of(Klas<T> type, T arg) {
			return new TypedArg<T>(type, arg);
		}
		
		private static final Function<TypedArg<?>, Type> TO_ASM_TYPE =
				new Function<TypedArg<?>, Type>() {
					@Override public Type apply(TypedArg<?> input) {
						return input.type.toType();
					}
				};
	}
	
	public static abstract class Chainable<T> implements Supplier<T> {
		private Chainable() { }
		public <U> Refer<U> refer(String field) {
			return new Refer<U>(null, this, field);
		}
		public <U> Invoke<U> invoke(String method) {
			return new Invoke<U>(null, this, method);
		}
		public <U> Chainable<T> assign(final String field, final U value) {
			return new Chainable<T>() {
				@Override public T get() {
					try {
						T ret = Chainable.this.get();
						ret.getClass().getField(field).set(ret, value);
						return ret;
					} catch (Throwable e) {
						throw Throwables.propagate(e);
					}
				}
			};
		}
		public Chainable<T> call(final Invokable<T, ?> method, final Object... args) {
			return new Chainable<T>() {
				@Override public T get() {
					try {
						T ret = Chainable.this.get();
						method.invoke(ret, args);
						return ret;
					} catch (Throwable e) {
						throw Throwables.propagate(e);
					}
				}
			};
		}
		public <K> Query<K, T> fallback(Registry<K, T> registry) {
			return registry.query().external(this);
		}
	}
	
	public static abstract class Arguments<T, U extends Arguments<T, U>> extends Chainable<T> {
		protected final ImmutableList<Supplier<TypedArg<?>>> args;
		private Arguments(ImmutableList<Supplier<TypedArg<?>>> args) {
			this.args = args;
		}
		protected abstract U derive(ImmutableList<Supplier<TypedArg<?>>> args);
		
		protected <V> U viaArg(Supplier<TypedArg<?>> arg) {
			return derive(ImmutableList.<Supplier<TypedArg<?>>>builder()
					.addAll(args)
					.add(arg)
					.build());
		}
		
		protected <V> U viaKlas(final Klas<V> type, final Supplier<V> arg) {
			return viaArg(new Supplier<TypedArg<?>>() {
				@Override public TypedArg<?> get() {
					return new TypedArg<V>(type, arg.get());
				}
			});
		}
		public <V> U via(Class<V> type, Supplier<V> arg) {
			return viaKlas(Klas.ofClass(type), arg);
		}
		public <V> U via(Class<V> type, V arg) {
			return via(type, Suppliers.ofInstance(arg));
		}
		public <V> U via(String type, Supplier<V> arg) {
			return viaKlas(Klas.<V>ofName(type), arg);
		}
		
		public <V> U via(final Supplier<V> arg) {
			return viaArg(new Supplier<TypedArg<?>>() {
				@SuppressWarnings("unchecked")
				@Override public TypedArg<?> get() {
					V v = arg.get();
					return new TypedArg<V>((Klas<V>) Klas.ofClass(v.getClass()), v);
				}
			});
		}
		public <V> U via(V arg) {
			return via(Suppliers.ofInstance(arg));
		}
		
		public U viaInt(final Supplier<Integer> arg) {
			return viaArg(new Supplier<TypedArg<?>>() {
				@Override public TypedArg<?> get() {
					return new TypedArg<Integer>(Klas.INT, arg.get());
				}
			});
		}
		public U viaInt(int arg) {
			return viaInt(Suppliers.ofInstance(arg));
		}
		
		@Override public <V> Refer<V> refer(String field) {
			return new Refer<V>(null, this, field);
		}
		@Override public <V> Invoke<V> invoke(String method) {
			return new Invoke<V>(null, this, method);
		}
	}
	
	public static class Refer<T> extends Chainable<T> {
		private final Klas<?> clz;
		private final Supplier<?> obj;
		private final String field;
		private Refer(Klas<?> clz, Supplier<?> obj, String field) {
			this.clz = clz;
			this.obj = obj;
			this.field = field;
		}
		
		@SuppressWarnings("unchecked")
		@Override public T get() {
			try {
				if (clz != null) {
					Field f = clz.toClass().getDeclaredField(field);
					f.setAccessible(true);
					return (T) f.get(obj.get());
				}
				
				Object o = obj.get();
				return (T) o.getClass().getField(field).get(o);
				
			} catch (Throwable e) {
				throw Throwables.propagate(e);
			}
		}
	}
	
	public static class Invoke<T> extends Arguments<T, Invoke<T>> {
		private final Klas<?> clz;
		private final Supplier<?> obj;
		private final String method;
		private Invoke(Klas<?> clz, Supplier<?> obj, String method,
				ImmutableList<Supplier<TypedArg<?>>> args) {
			super(args);
			this.clz = clz;
			this.obj = obj;
			this.method = method;
		}
		private Invoke(Klas<?> clz, Supplier<?> obj, String method) {
			this(clz, obj, method, ImmutableList.<Supplier<TypedArg<?>>>of());
		}
		@Override protected Invoke<T> derive(ImmutableList<Supplier<TypedArg<?>>> args) {
			return new Invoke<T>(clz, obj, method, args);
		}
		
		@SuppressWarnings("unchecked")
		@Override public T get() {
			try {
				List<Class<?>> types = Lists.newArrayList();
				List<Object> args = Lists.newArrayList();
				
				for (Supplier<TypedArg<?>> supplier : this.args) {
					TypedArg<?> p = supplier.get();
					types.add(p.type.toClass());
					args.add(p.arg);
				}
				
				if (clz != null) {
					Method m = clz.toClass()
							.getDeclaredMethod(method, Iterables.toArray(types, Class.class));
					m.setAccessible(true);
					return (T) m.invoke(obj.get(), args.toArray());
				}
				
				Object o = obj.get();
				return (T) o.getClass()
						.getMethod(method, Iterables.toArray(types, Class.class))
						.invoke(o, args.toArray());
				
			} catch (Throwable e) {
				throw Throwables.propagate(e);
			}
		}
	}
	
	public static class Construct<T> extends Arguments<T, Construct<T>> {
		private final Klas<? extends T> clz;
		private Construct(Klas<? extends T> clz, ImmutableList<Supplier<TypedArg<?>>> args) {
			super(args);
			this.clz = clz;
		}
		private Construct(Klas<? extends T> clz) {
			this(clz, ImmutableList.<Supplier<TypedArg<?>>>of());
		}
		@Override protected Construct<T> derive(ImmutableList<Supplier<TypedArg<?>>> args) {
			return new Construct<T>(clz, args);
		}
		
		@Override public T get() {
			try {
				List<Class<?>> types = Lists.newArrayList();
				List<Object> args = Lists.newArrayList();
				
				for (Supplier<TypedArg<?>> supplier : this.args) {
					TypedArg<?> p = supplier.get();
					types.add(p.type.toClass());
					args.add(p.arg);
				}
				
				Constructor<? extends T> c = clz.toClass()
						.getDeclaredConstructor(Iterables.toArray(types, Class.class));
				c.setAccessible(true);
				return c.newInstance(args.toArray());
				
			} catch (Throwable e) {
				throw Throwables.propagate(e);
			}
		}
	}
	
}

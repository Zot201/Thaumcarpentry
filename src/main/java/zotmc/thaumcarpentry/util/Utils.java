package zotmc.thaumcarpentry.util;

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Type;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Sets;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import cpw.mods.fml.common.versioning.ArtifactVersion;
import cpw.mods.fml.common.versioning.VersionParser;

public class Utils {
	
	public static final SimpleVersion MC_VERSION = new SimpleVersion(
			Fields.<String>get(null, findField(Loader.class, "MC_VERSION", "mccversion"))); // prevent inlining
	
	
	
	// functional idioms
	
	@SuppressWarnings("unchecked")
	public static <T> Supplier<T> nullSupplier() {
		return (Supplier<T>) NullSupplier.INSTANCE;
	}
	private enum NullSupplier implements Supplier<Object> {
		INSTANCE;
		@Override public Object get() {
			return null;
		}
	}
	
	
	
	// syntax
	
	@SafeVarargs public static <E> FluentMultiset<E> multiset(E... a) {
		return FluentMultiset.of(a);
	}
	
	public static <E> FluentMultiset<E> multiset(E e, int count) {
		return FluentMultiset.of(e, count);
	}
	
	@SafeVarargs public static <T> Iterable<T> enumValues(Class<? extends T>... a) {
		for (Class<? extends T> c : a = a.clone())
			checkArgument(c.isEnum());
		
		return Iterables.concat(Iterables.transform(
				Arrays.asList(a),
				new Function<Class<? extends T>, List<T>>() { public List<T> apply(Class<? extends T> input) {
					return Arrays.asList((T[]) input.getEnumConstants());
				}}
		));
	}
	
	
	
	// reflections
	
	public static Field findField(Class<?> clz, String... names) {
		String owner = unmapTypeName(clz);
		for (String s : names)
			try {
				Field f = clz.getDeclaredField(remapFieldName(owner, s));
				f.setAccessible(true);
				return f;
			} catch (Throwable ignored) { }
		
		throw new UnknownFieldException(clz.getName() + ".[" + Joiner.on(", ").join(names) + "]");
	}
	
	private static String unmapTypeName(Class<?> clz) {
		return FMLDeobfuscatingRemapper.INSTANCE.unmap(Type.getInternalName(clz));
	}
	private static String remapFieldName(String owner, String field) {
		return FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(owner, field, null);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T[] newArray(String componentType, int length) {
		try {
			return ObjectArrays.newArray((Class<T>) Class.forName(componentType), length);
		} catch (ClassNotFoundException e) {
			throw new UnknownClassException(e);
		}
	}
	
	
	
	// version validations
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Modid { }
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface Dependency { }

	/**
	 * When applied to an outer class, this represents a map from building MC versions to the required MC versions.
	 * When applied to an inner class, this represents a map from actual MC versions to the required mod versions.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface Requirements {
		public String[] value();
	}
	
	public static Set<ArtifactVersion> checkRequirements(Class<?> clz, String mcString) {
		Set<ArtifactVersion> missing = Sets.newHashSet();
		
		ModContainer mc = Loader.instance().getMinecraftModContainer();
		ArtifactVersion m0 = check(clz, "Minecraft", new SimpleVersion(mcString), mc);
		if (m0 != null)
			missing.add(m0);
		
		Map<String, ModContainer> mods = Loader.instance().getIndexedModList();
		for (Class<?> c : clz.getDeclaredClasses()) {
			String modid = null;
			
			for (Field f : c.getDeclaredFields())
				if (f.getAnnotation(Modid.class) != null) {
					checkArgument(modid == null);
					checkArgument(Modifier.isStatic(f.getModifiers()));
					
					f.setAccessible(true);
					modid = Fields.get(null, f);
				}
			
			if (modid != null) {
				ArtifactVersion m = check(c, modid, MC_VERSION, mods.get(modid));
				if (m != null)
					missing.add(m);
			}
		}
		
		return missing;
	}
	
	private static ArtifactVersion check(Class<?> c, String modid, SimpleVersion key, ModContainer mc) {
		boolean isLoaded = Loader.isModLoaded(modid);
		
		if (isLoaded || c.getAnnotation(Dependency.class) != null) {
			Requirements requirements = c.getAnnotation(Requirements.class);
			
			if (requirements != null) {
				for (String s : requirements.value()) {
					List<String> entry = Splitter.on('=').trimResults().splitToList(s);
					checkArgument(entry.size() == 2);
					
					if (key.isAtLeast(entry.get(0))) {
						ArtifactVersion r = parse(modid, entry.get(1));
						
						if (!isLoaded || !r.containsVersion(mc.getProcessedVersion()))
							return r;
						break;
					}
				}
			}
			
			if (!isLoaded)
				return VersionParser.parseVersionReference(modid);
		}
		
		return null;
	}
	
	private static ArtifactVersion parse(String modid, String versionRange) {
		char c = versionRange.charAt(0);
		if (c != '[' && c != '(')
			versionRange = "[" + versionRange + ",)";
		return VersionParser.parseVersionReference(modid + "@" + versionRange);
	}
	
	
	
	// exceptions
	
	public static class UnknownClassException extends RuntimeException {
		public UnknownClassException() { }
		public UnknownClassException(String message, Throwable cause) {
			super(message, cause);
		}
		public UnknownClassException(String message) {
			super(message);
		}
		public UnknownClassException(Throwable cause) {
			super(cause);
		}
	}
	
	public static class UnknownFieldException extends RuntimeException {
		public UnknownFieldException() { }
		public UnknownFieldException(String message, Throwable cause) {
			super(message, cause);
		}
		public UnknownFieldException(String message) {
			super(message);
		}
		public UnknownFieldException(Throwable cause) {
			super(cause);
		}
	}
	
}

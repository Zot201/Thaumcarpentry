package zotmc.thaumcarpentry.util;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;

import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import cpw.mods.fml.common.registry.GameData;

public abstract class Registry<K, V> {
	
	private Registry() { }
	
	public abstract boolean containsKey(K key) ;
	public abstract V get(K key);
	public abstract Iterable<V> values();
	
	public Query<K, V> query() {
		return new Query<K, V>() {
			@Override Registry<K, V> registry() {
				return Registry.this;
			}
			@Override public V get() {
				return null;
			}
		};
	}
	
	
	private static abstract class NamespacedRegistry<V> extends Registry<String, V> {
		protected abstract Class<V> type();
		protected abstract FMLControlledNamespacedRegistry<V> backing();
		
		@Override public boolean containsKey(String key) {
			return get(key) != null;
		}
		@Override public V get(String key) {
			try {
				return type().cast(backing().getRaw(key));
			} catch (ClassCastException ignored) { }
			return null;
		}
		@Override public Iterable<V> values() {
			return Iterables.filter(backing(), type());
		}
	}
	
	public static Registry<String, Item> items() {
		return ITEM_REGISTRY;
	}
	private static final Registry<String, Item> ITEM_REGISTRY = new NamespacedRegistry<Item>() {
		@Override protected Class<Item> type() {
			return Item.class;
		}
		@Override protected FMLControlledNamespacedRegistry<Item> backing() {
			return GameData.getItemRegistry();
		}
	};
	
	public static Registry<String, Block> blocks() {
		return BLOCK_REGISTRY;
	}
	private static final Registry<String, Block> BLOCK_REGISTRY = new NamespacedRegistry<Block>() {
		@Override protected Class<Block> type() {
			return Block.class;
		}
		@Override protected FMLControlledNamespacedRegistry<Block> backing() {
			return GameData.getBlockRegistry();
		}
	};
	
	public static Registry<String, Class<? extends Entity>> entities() {
		return ENTITY_REGISTRY;
	}
	private static final Registry<String, Class<? extends Entity>> ENTITY_REGISTRY =
			new Registry<String, Class<? extends Entity>>() {
		
		private final Predicate<Object> valuePredicate = new Predicate<Object>() { public boolean apply(Object input) {
			return input instanceof Class && Entity.class.isAssignableFrom((Class<?>) input);
		}};
		
		@Override public boolean containsKey(String key) {
			return get(key) != null;
		}
		@SuppressWarnings("unchecked")
		@Override public Class<? extends Entity> get(String key) {
			Object ret = EntityList.stringToClassMapping.get(key);
			return valuePredicate.apply(ret) ? (Class<? extends Entity>) ret : null;
		}
		@SuppressWarnings("unchecked")
		@Override public Iterable<Class<? extends Entity>> values() {
			return Iterables.unmodifiableIterable(Iterables.filter(EntityList.stringToClassMapping.values(), valuePredicate));
		}
	};
	
	
	public abstract static class Query<K, V> implements Supplier<V> {
		private Query() { }
		abstract Registry<K, V> registry();
		
		class DerivedRegistryEntry extends Query<K, V> {
			@Override Registry<K, V> registry() {
				return Query.this.registry();
			}
			@Override public V get() {
				return Query.this.get();
			}
		}
		
		public Query<K, V> external(final Supplier<? extends V> supplier) {
			return this.new DerivedRegistryEntry() { public V get() {
				V ret = super.get();
				if (ret == null)
					try {
						ret = supplier.get();
					} catch (Throwable ignored) { }
				return ret;
			}};
		}
		public Query<K, V> lookup(final K key) {
			return this.new DerivedRegistryEntry() { public V get() {
				V ret = super.get();
				if (ret == null)
					ret = registry().get(key);
				return ret;
			}};
		}
		public Query<K, V> typing(final String type) {
			return this.new DerivedRegistryEntry() { public V get() {
				V ret = super.get();
				if (ret == null)
					for (V v : registry().values())
						if (v.getClass().getName().equals(type)) {
							ret = v;
							break;
						}
				return ret;
			}};
		}
	}
	
}

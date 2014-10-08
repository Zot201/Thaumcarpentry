package zotmc.thaumcarpentry;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import zotmc.thaumcarpentry.data.ModData.CarpentersBlocks;
import zotmc.thaumcarpentry.data.ModData.Thaumcraft;
import zotmc.thaumcarpentry.util.Dynamic;
import zotmc.thaumcarpentry.util.Dynamic.Chainable;
import zotmc.thaumcarpentry.util.Registry;
import zotmc.thaumcarpentry.util.Utils;

import com.google.common.collect.Multiset;

public enum CarpentersItem implements AspectHolder<Item> {
	Hammer (Utils.multiset(Aspect.TOOL, Aspect.CRAFT)),
	Chisel (Utils.multiset(Aspect.TOOL, Aspect.CRAFT));
	
	
	private final Multiset<Aspect> aspects;
	
	private CarpentersItem(Multiset<Aspect> aspects) {
		this.aspects = aspects;
	}
	
	@Override public boolean isEnabled() {
		try {
			return Dynamic.<Boolean>refer(CarpentersBlocks.ITEM_REGISTRY, "enable" + name()).get();
		} catch (Throwable e) {
			Thaumcarpentry.instance.log.catching(e);
		}
		return true;
	}
	
	@Override public Item get() {
		return getImpl(this);
	}
	
	@Override public void registerObjectTag() {
		registerObjectTagImpl(get(), aspects);
	}
	
	@Override public String toString() {
		return toStringImpl(this);
	}
	
	
	
	static Item getImpl(AspectHolder<?> holder) {
		return Dynamic.<Item>refer(CarpentersBlocks.ITEM_REGISTRY, "itemCarpenters" + holder.name())
				.fallback(Registry.items())
				.lookup(holder.toString())
				.typing("com.carpentersblocks.item.ItemCarpenters" + holder.name())
				.get();
	}
	
	static void registerObjectTagImpl(Item i, Multiset<Aspect> aspects) {
		Chainable<?> aspectList = Dynamic.construct(Thaumcraft.ASPECT_LIST);
		
		for (Multiset.Entry<Aspect> entry : aspects.entrySet())
			aspectList = aspectList.invoke(Thaumcraft.ADD)
				.via(Thaumcraft.ASPECT, entry.getElement())
				.viaInt(entry.getCount());
		
		Dynamic.<Void>invoke(Thaumcraft.THAUMCRAFT_API, Thaumcraft.REGISTER_COMPLEX_OBJECT_TAG)
			.via(new ItemStack(i, 1, OreDictionary.WILDCARD_VALUE))
			.via(Thaumcraft.ASPECT_LIST, aspectList)
			.get();
	}
	
	static String toStringImpl(AspectHolder<?> holder) {
		return CarpentersBlocks.MODID + ":itemCarpenters" + holder.name();
	}
	
}

package zotmc.thaumcarpentry;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import zotmc.thaumcarpentry.data.ModData.CarpentersBlocks;
import zotmc.thaumcarpentry.util.Dynamic;
import zotmc.thaumcarpentry.util.Registry;
import zotmc.thaumcarpentry.util.Utils;

import com.google.common.collect.Multiset;

public enum CarpentersBlock implements AspectHolder<Block> {
	Button (Utils.multiset(Aspect.MECHANISM)),
	DaylightSensor (Utils.multiset(Aspect.SENSES, 3).tag(Aspect.LIGHT, 3).tag(Aspect.MECHANISM, 2)),
	FlowerPot (Utils.multiset(Aspect.VOID, Aspect.PLANT)),
	Gate (Utils.multiset(Aspect.MECHANISM, Aspect.TRAVEL)),
	Hatch (Utils.multiset(Aspect.MOTION)),
	Ladder (Utils.multiset(Aspect.TRAVEL)),
	Lever (Utils.multiset(Aspect.MECHANISM)),
	PressurePlate (Utils.multiset(Aspect.MECHANISM, Aspect.SENSES)),
	Safe (Utils.multiset(Aspect.MECHANISM).tag(Aspect.TRAP, 2).tag(Aspect.VOID, 4).tag(Aspect.SENSES, 2)),
	Torch (Utils.multiset(Aspect.LIGHT));
	
	// unused: Barrier, Block, CollapsibleBlock, Slope, Stairs
	
	
	private final Multiset<Aspect> aspects;
	
	private CarpentersBlock(Multiset<Aspect> aspects) {
		this.aspects = aspects;
	}
	
	@Override public boolean isEnabled() {
		return isEnabledImpl(this);
	}
	
	@Override public Block get() {
		return Dynamic.<Block>refer(CarpentersBlocks.BLOCK_REGISTRY, "blockCarpenters" + name())
				.fallback(Registry.blocks())
				.lookup(toString())
				.typing("com.carpentersblocks.block.BlockCarpenters" + name())
				.get();
	}
	
	@Override public void registerObjectTag() {
		CarpentersItem.registerObjectTagImpl(Item.getItemFromBlock(get()), aspects);
	}
	
	@Override public String toString() {
		return CarpentersBlocks.MODID + ":blockCarpenters" + name();
	}
	
	
	
	static boolean isEnabledImpl(AspectHolder<?> holder) {
		try {
			return Dynamic.<Boolean>refer(CarpentersBlocks.BLOCK_REGISTRY, "enable" + holder.name()).get();
		} catch (Throwable e) {
			Thaumcarpentry.instance.log.catching(e);
		}
		return true;
	}
	
}

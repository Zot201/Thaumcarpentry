package zotmc.thaumcarpentry;

import net.minecraft.item.Item;
import zotmc.thaumcarpentry.util.Utils;

import com.google.common.collect.Multiset;

public enum CarpentersBlockAsItem implements AspectHolder<Item> {
	Bed (Utils.multiset(Aspect.SENSES)),
	Door (Utils.multiset(Aspect.MECHANISM, Aspect.MOTION, Aspect.SENSES));
	
	
	private final Multiset<Aspect> aspects;
	
	private CarpentersBlockAsItem(Multiset<Aspect> aspects) {
		this.aspects = aspects;
	}
	
	@Override public boolean isEnabled() {
		return CarpentersBlock.isEnabledImpl(this);
	}
	
	@Override public Item get() {
		return CarpentersItem.getImpl(this);
	}
	
	@Override public void registerObjectTag() {
		CarpentersItem.registerObjectTagImpl(get(), aspects);
	}
	
	@Override public String toString() {
		return CarpentersItem.toStringImpl(this);
	}
	
}

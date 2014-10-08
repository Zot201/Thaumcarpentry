package zotmc.thaumcarpentry;

import zotmc.thaumcarpentry.data.ModData.Thaumcraft;
import zotmc.thaumcarpentry.util.Dynamic;

import com.google.common.base.Supplier;

public enum Aspect implements Supplier<Object> {
	ORDER,
	VOID,
	LIGHT,
	MOTION,
	METAL,
	TRAVEL,
	PLANT,
	TREE,
	SENSES,
	TOOL,
	CRAFT,
	CLOTH,
	MECHANISM,
	TRAP;
	
	@Override public Object get() {
		return Dynamic.refer(Thaumcraft.ASPECT, name()).get();
	}
	
}

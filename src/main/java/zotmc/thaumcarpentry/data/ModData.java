package zotmc.thaumcarpentry.data;

import zotmc.thaumcarpentry.util.Utils.Dependency;
import zotmc.thaumcarpentry.util.Utils.Modid;
import zotmc.thaumcarpentry.util.Utils.Requirements;

import com.google.common.collect.Lists;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModMetadata;

@Requirements("1.7 = 1.7")
public class ModData {
	
	public static class Thaumcarpentrys {
		public static final String
		MODID = "thaumcarpentry",
		NAME = "Thaumcarpentry",
		MC_STRING = Loader.MC_VERSION,
		VERSION = "0.0.1.5-" + MC_STRING,
		DEPENDENCIES = "after:" + Thaumcraft.MODID;
	}
	
	public static void init(ModMetadata metadata) {
		metadata.autogenerated = false;
		metadata.logoFile = "Thaumcarpentry.png";
		metadata.authorList = Lists.newArrayList("Zot");
		metadata.url = "https://github.com/Zot201/Thaumcarpentry";
		metadata.description = "Thaumcraft compatibilities for Carpenter's Blocks.";
	}
	
	
	@Dependency
	@Requirements({"1.7.10 = 10.13.0.1207", "1.7.2 = 10.12.2.1121"})
	public static class Forge {
		@Modid public static final String MODID = "Forge";
	}
	
	@Requirements("1.7.2 = 3.2.8")
	public static class CarpentersBlocks {
		@Modid public static final String MODID = "CarpentersBlocks";
		
		// registry
		public static final String
		BLOCK_REGISTRY = "com.carpentersblocks.util.registry.BlockRegistry",
		ITEM_REGISTRY = "com.carpentersblocks.util.registry.ItemRegistry";
		
		// carpenter's tile
		public static final String
		ENABLE_TILE = "enableTile",
		ENTITY_CARPENTERS_TILE = "com.carpentersblocks.entity.item.EntityCarpentersTile",
		CARPENTERS_TILE = "CarpentersBlocks.CarpentersTile";
	}
	
	@Requirements({"1.7.10 = 4.2.0.0", "1.7.2 = 4.1.1.14"})
	public static class Thaumcraft {
		@Modid public static final String MODID = "Thaumcraft";
		
		public static final String
		ASPECT = "thaumcraft.api.aspects.Aspect",
		ASPECT_LIST = "thaumcraft.api.aspects.AspectList",
		ADD = "add",
		THAUMCRAFT_API = "thaumcraft.api.ThaumcraftApi",
		ENTITY_TAGS_NBT = THAUMCRAFT_API + "$EntityTagsNBT",
		REGISTER_ENTITY_TAG = "registerEntityTag",
		REGISTER_COMPLEX_OBJECT_TAG = "registerComplexObjectTag";
	}
	
}

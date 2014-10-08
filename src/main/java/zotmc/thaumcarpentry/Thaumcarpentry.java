package zotmc.thaumcarpentry;

import static zotmc.thaumcarpentry.data.ModData.Thaumcarpentrys.DEPENDENCIES;
import static zotmc.thaumcarpentry.data.ModData.Thaumcarpentrys.MC_STRING;
import static zotmc.thaumcarpentry.data.ModData.Thaumcarpentrys.MODID;
import static zotmc.thaumcarpentry.data.ModData.Thaumcarpentrys.NAME;
import static zotmc.thaumcarpentry.data.ModData.Thaumcarpentrys.VERSION;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import zotmc.thaumcarpentry.data.ModData;
import zotmc.thaumcarpentry.data.ModData.CarpentersBlocks;
import zotmc.thaumcarpentry.data.ModData.Thaumcraft;
import zotmc.thaumcarpentry.util.Utils;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.MissingModsException;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.versioning.ArtifactVersion;

@Mod(modid = MODID, name = NAME, version = VERSION, dependencies = DEPENDENCIES)
public class Thaumcarpentry {
	
	@Instance(MODID) public static Thaumcarpentry instance;
	
	final Logger log = LogManager.getFormatterLogger(NAME);
	
	
	@EventHandler public void onConstruct(FMLConstructionEvent event) {
		Set<ArtifactVersion> missing = Utils.checkRequirements(ModData.class, MC_STRING);
		if (!missing.isEmpty())
			throw new MissingModsException(missing);
	}
	
	@EventHandler public void preInit(FMLPreInitializationEvent event) {
		ModData.init(event.getModMetadata());
	}
	
	@EventHandler public void postInit(FMLPostInitializationEvent event) {
		if (Loader.isModLoaded(CarpentersBlocks.MODID) && Loader.isModLoaded(Thaumcraft.MODID)) {
			log.info("Adding Thaumcraft aspects for Carpenter's Blocks...");
			
			for (AspectHolder<?> h : Utils.enumValues(
					CarpentersBlock.class, CarpentersItem.class, CarpentersBlockAsItem.class, CarpentersTile.class))
				try {
					if (h.isEnabled())
						h.registerObjectTag();
					
				} catch (Throwable e) {
					log.error("Error while adding aspects for " + h, e);
				}
		}
	}
	
}

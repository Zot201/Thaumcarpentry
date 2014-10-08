package zotmc.thaumcarpentry;

import static com.google.common.base.Preconditions.checkNotNull;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import zotmc.thaumcarpentry.data.ModData.CarpentersBlocks;
import zotmc.thaumcarpentry.data.ModData.Thaumcraft;
import zotmc.thaumcarpentry.util.Dynamic;
import zotmc.thaumcarpentry.util.Klas;
import zotmc.thaumcarpentry.util.Registry;
import zotmc.thaumcarpentry.util.Utils;

import com.google.common.base.Supplier;

public enum CarpentersTile implements AspectHolder<Class<? extends Entity>> {
	INSTANCE;
	
	@Override public boolean isEnabled() {
		try {
			return Dynamic.<Boolean>refer(CarpentersBlocks.ITEM_REGISTRY, CarpentersBlocks.ENABLE_TILE).get();
		} catch (Throwable e) {
			Thaumcarpentry.instance.log.catching(e);
		}
		return true;
	}
	
	@Override public Class<? extends Entity> get() {
		return Registry.entities().query()
				.external((Klas<? extends Entity>) Klas.<Entity>ofName(CarpentersBlocks.ENTITY_CARPENTERS_TILE))
				.lookup(toString())
				.get();
	}
	
	@Override public void registerObjectTag() {
		Supplier<?> aspectList = Dynamic.construct(Thaumcraft.ASPECT_LIST)
				.invoke(Thaumcraft.ADD).via(Aspect.SENSES).viaInt(1)
				.invoke(Thaumcraft.ADD).via(Aspect.ORDER).viaInt(1);
		
		Dynamic.<Void>invoke(Thaumcraft.THAUMCRAFT_API, Thaumcraft.REGISTER_ENTITY_TAG)
			.via((String) checkNotNull(EntityList.classToStringMapping.get(get())))
			.via(Thaumcraft.ASPECT_LIST, aspectList)
			.via(Utils.newArray(Thaumcraft.ENTITY_TAGS_NBT, 0))
			.get();
	}
	
	@Override public String toString() {
		return CarpentersBlocks.CARPENTERS_TILE;
	}
	
}

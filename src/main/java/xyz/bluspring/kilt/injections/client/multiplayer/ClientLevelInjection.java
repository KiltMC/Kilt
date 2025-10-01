package xyz.bluspring.kilt.injections.client.multiplayer;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.neoforged.neoforge.entity.PartEntity;
import xyz.bluspring.kilt.util.KiltHelper;

public interface ClientLevelInjection {
    default Int2ObjectMap<PartEntity<?>> kilt$getPartEntitiesMap() {
        throw KiltHelper.createMixinException(ClientLevelInjection.class, "kilt$getPartEntitiesMap");
    }
}

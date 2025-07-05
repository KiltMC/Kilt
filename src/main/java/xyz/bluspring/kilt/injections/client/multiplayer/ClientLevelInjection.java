package xyz.bluspring.kilt.injections.client.multiplayer;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraftforge.entity.PartEntity;

public interface ClientLevelInjection {
    Int2ObjectMap<PartEntity<?>> kilt$getPartEntitiesMap();
}

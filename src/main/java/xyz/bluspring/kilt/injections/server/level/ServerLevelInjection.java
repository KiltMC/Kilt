package xyz.bluspring.kilt.injections.server.level;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.neoforged.neoforge.entity.PartEntity;

public interface ServerLevelInjection {
    Int2ObjectMap<PartEntity<?>> kilt$getEntityParts();
}

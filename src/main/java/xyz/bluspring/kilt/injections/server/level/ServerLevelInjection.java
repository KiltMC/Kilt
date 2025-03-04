package xyz.bluspring.kilt.injections.server.level;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraftforge.entity.PartEntity;

public interface ServerLevelInjection {
    default Int2ObjectMap<PartEntity<?>> kilt$getEntityParts() {
        throw new IllegalStateException();
    }
}

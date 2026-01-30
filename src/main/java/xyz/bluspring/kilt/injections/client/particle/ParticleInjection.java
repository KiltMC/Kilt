package xyz.bluspring.kilt.injections.client.particle;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import xyz.bluspring.kilt.util.KiltHelper;

public interface ParticleInjection {
    default AABB getRenderBoundingBox(float partialTicks) {
        throw KiltHelper.createMixinException(ParticleInjection.class, "getRenderBoundingBox");
    }

    default Vec3 getPos() {
        throw KiltHelper.createMixinException(ParticleInjection.class, "getPos");
    }
}

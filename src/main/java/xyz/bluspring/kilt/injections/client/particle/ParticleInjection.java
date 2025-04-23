package xyz.bluspring.kilt.injections.client.particle;

import io.github.fabricators_of_create.porting_lib.extensions.extensions.ParticleExtensions;
import net.minecraft.world.phys.Vec3;

public interface ParticleInjection extends ParticleExtensions {
    Vec3 getPos();
}

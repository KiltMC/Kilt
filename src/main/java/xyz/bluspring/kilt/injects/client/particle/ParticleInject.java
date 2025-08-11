package xyz.bluspring.kilt.injects.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.client.particle.ParticleInjection;

@Mixin(Particle.class)
public abstract class ParticleInject implements ParticleInjection {
    @Shadow protected double x;

    @Shadow protected double y;

    @Shadow protected double z;

    @Override
    public Vec3 getPos() {
        return new Vec3(this.x, this.y, this.z);
    }
}

package xyz.bluspring.kilt.injects.client.particle;

import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.world.phys.AABBInjection;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.MobAppearanceParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.AABB;

@Mixin(MobAppearanceParticle.class)
public abstract class MobAppearanceParticleInject extends Particle {
    protected MobAppearanceParticleInject(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
    }

    @Override
    public AABB getRenderBoundingBox(float partialTicks) {
        return AABBInjection.INFINITE;
    }
}

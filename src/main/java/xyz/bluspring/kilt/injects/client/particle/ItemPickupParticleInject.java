package xyz.bluspring.kilt.injects.client.particle;

import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.world.phys.AABBInjection;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.AABB;

@Mixin(ItemPickupParticle.class)
public abstract class ItemPickupParticleInject extends Particle {
    protected ItemPickupParticleInject(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
    }

    @Override
    public AABB getRenderBoundingBox(float partialTicks) {
        return AABBInjection.INFINITE;
    }
}

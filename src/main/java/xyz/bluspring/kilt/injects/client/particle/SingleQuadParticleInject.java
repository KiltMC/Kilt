package xyz.bluspring.kilt.injects.client.particle;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.world.phys.AABB;

@Mixin(SingleQuadParticle.class)
public abstract class SingleQuadParticleInject extends Particle {
    @Shadow
    public abstract float getQuadSize(float scaleFactor);

    protected SingleQuadParticleInject(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
    }

    @Override
    public AABB getRenderBoundingBox(float partialTicks) {
        float size = this.getQuadSize(partialTicks);
        return new AABB(this.x - size, this.y - size, this.z - size, this.x + size, this.y + size, this.z + size);
    }
}

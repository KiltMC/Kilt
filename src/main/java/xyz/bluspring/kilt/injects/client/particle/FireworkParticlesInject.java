package xyz.bluspring.kilt.injects.client.particle;

import net.minecraft.client.particle.FireworkParticles;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FireworkParticles.class)
public abstract class FireworkParticlesInject {
    @Mixin(FireworkParticles.Starter.class)
    public abstract static class StarterInject {
        // TODO: impl
        /*@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/FireworkParticles$Starter;createParticleBall(DILit/unimi/dsi/fastutil/ints/IntList;Lit/unimi/dsi/fastutil/ints/IntList;ZZ)V"))
        private void kilt$tryBuildFromFactory(FireworkParticles.Starter instance, double d, int i, IntList intList, IntList intList2, boolean bl, boolean bl2, Operation<Void> original, @Local FireworkRocketItem.Shape shape) {
            var factory = FireworkShapeFactoryRegistry.get(shape);

            if (factory != null) {
                factory.build((FireworkParticles.Starter) (Object) this, trail, twinkle, colours, fadeColours);
            } else {
                original.call(instance, speed, size, colours, fadeColours, trail, twinkle);
            }
        }*/
    }
}

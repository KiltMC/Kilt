package xyz.bluspring.kilt.forgeinjects.client.particle;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraftforge.client.FireworkShapeFactoryRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.world.item.FireworkRocketItemShapeInjection;

@Mixin(FireworkParticles.class)
public abstract class FireworkParticlesInject {
    @Mixin(FireworkParticles.Starter.class)
    public abstract static class StarterInject {
        @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/FireworkRocketItem$Shape;byId(I)Lnet/minecraft/world/item/FireworkRocketItem$Shape;"))
        private FireworkRocketItem.Shape kilt$tryGetForgeShape(int index, Operation<FireworkRocketItem.Shape> original, @Local CompoundTag tag) {
            if (tag.contains("forge:shape_type", Tag.TAG_STRING))
                return FireworkRocketItemShapeInjection.getShape(tag);

            return original.call(index);
        }

        @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/FireworkParticles$Starter;createParticleBall(DI[I[IZZ)V", ordinal = 0))
        private void kilt$tryBuildFromFactory(FireworkParticles.Starter instance, double speed, int size, int[] colours, int[] fadeColours, boolean trail, boolean twinkle, Operation<Void> original, @Local FireworkRocketItem.Shape shape) {
            var factory = FireworkShapeFactoryRegistry.get(shape);

            if (factory != null) {
                factory.build((FireworkParticles.Starter) (Object) this, trail, twinkle, colours, fadeColours);
            } else {
                original.call(instance, speed, size, colours, fadeColours, trail, twinkle);
            }
        }
    }
}

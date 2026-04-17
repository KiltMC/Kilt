package xyz.bluspring.kilt.injects.client.particle;

import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.client.particle.ParticleRenderTypeInjection;

import net.minecraft.client.particle.ParticleRenderType;

@Mixin(ParticleRenderType.class)
public interface ParticleRenderTypeInject extends ParticleRenderTypeInjection {
    @Mixin(targets = "net.minecraft.client.particle.ParticleRenderType$2")
    abstract class ParticleSheetOpaqueInject implements ParticleRenderTypeInjection {
        @Override
        public boolean isTranslucent() {
            return false;
        }
    }

    @Mixin(targets = "net.minecraft.client.particle.ParticleRenderType$4")
    abstract class ParticleSheetLitInject implements ParticleRenderTypeInjection {
        @Override
        public boolean isTranslucent() {
            return false;
        }
    }

    @Override
    default boolean isTranslucent() {
        return true;
    }
}

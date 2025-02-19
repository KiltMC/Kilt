package xyz.bluspring.kilt.forgeinjects.world.effect;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import net.minecraftforge.common.extensions.IForgeMobEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.render.RenderPropertiesInjection;
import xyz.bluspring.kilt.injections.world.effect.MobEffectInjection;

import java.util.function.Consumer;

@Mixin(MobEffect.class)
public class MobEffectInject implements RenderPropertiesInjection<IClientMobEffectExtensions>, IForgeMobEffect, MobEffectInjection {
    @Inject(at = @At("TAIL"), method = "<init>")
    public void kilt$initClient(MobEffectCategory mobEffectCategory, int i, CallbackInfo ci) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            this.initializeClient((extensionProperties) -> {
                effectRenderer = extensionProperties;
            });
        }
    }

    @Unique private Object effectRenderer;

    @Override
    public Object getEffectRendererInternal() {
        return effectRenderer;
    }

    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
    }
}

package xyz.bluspring.kilt.forgeinjects.world.effect;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.render.RenderPropertiesInjection;

@Mixin(MobEffect.class)
public class MobEffectInject implements RenderPropertiesInjection<IClientMobEffectExtensions> {
    @Unique
    private Object renderProperties;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void kilt$initClient(MobEffectCategory mobEffectCategory, int i, CallbackInfo ci) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            this.initializeClient((extensionProperties) -> {
                renderProperties = extensionProperties;
            });
        }
    }

    @Override
    public Object getRenderPropertiesInternal() {
        return renderProperties;
    }
}

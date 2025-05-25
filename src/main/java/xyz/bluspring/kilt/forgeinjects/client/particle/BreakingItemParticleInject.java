package xyz.bluspring.kilt.forgeinjects.client.particle;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BreakingItemParticle.class)
public abstract class BreakingItemParticleInject {
    @Redirect(method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDLnet/minecraft/world/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/BakedModel;getParticleIcon()Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"))
    private TextureAtlasSprite kilt$tryResolveOverrides(BakedModel instance, @Local(argsOnly = true) ClientLevel level, @Local(argsOnly = true) ItemStack stack) {
        return instance.getOverrides().resolve(instance, stack, level, null, 0).getParticleIcon(ModelData.EMPTY);
    }
}

package xyz.bluspring.kilt.forgeinjects.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.fml.ModLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(SkullBlockRenderer.class)
public abstract class SkullBlockRendererInject {
    @Inject(method = "createSkullRenderers", at = @At("TAIL"))
    private static void kilt$addForgeSkullRenderers(EntityModelSet entityModelSet, CallbackInfoReturnable<Map<SkullBlock.Type, SkullModelBase>> cir, @Local ImmutableMap.Builder<SkullBlock.Type, SkullModelBase> builder) {
        ModLoader.get().postEvent(new EntityRenderersEvent.CreateSkullModels(builder, entityModelSet));
    }
}

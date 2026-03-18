package xyz.bluspring.kilt.injects.client.renderer.entity;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.bluspring.kilt.injections.client.renderer.entity.ItemEntityRendererInjection;

import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererInject implements ItemEntityRendererInjection {
    @ModifyVariable(method = "render(Lnet/minecraft/world/entity/item/ItemEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/model/ItemTransforms;getTransform(Lnet/minecraft/world/item/ItemDisplayContext;)Lnet/minecraft/client/renderer/block/model/ItemTransform;", ordinal = 0), ordinal = 3)
    private float kilt$onlyBobIfRequired(float original, @Local ItemStack stack) {
        if (IClientItemExtensions.of(stack).shouldBobAsEntity(stack))
            return original;
        else
            return 0f;
    }

    @Definition(id = "m", local = @Local(type = int.class, ordinal = 2))
    @Expression("m > 0")
    @ModifyExpressionValue(method = "renderMultipleFromCount(Lnet/minecraft/client/renderer/entity/ItemRenderer;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/resources/model/BakedModel;ZLnet/minecraft/util/RandomSource;)V", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean kilt$checkShouldSpreadAsEntity(boolean original, @Local(argsOnly = true) ItemStack stack) {
        return original && IClientItemExtensions.of(stack).shouldSpreadAsEntity(stack);
    }
}

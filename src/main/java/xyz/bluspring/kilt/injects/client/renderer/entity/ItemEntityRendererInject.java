package xyz.bluspring.kilt.injects.client.renderer.entity;

import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import xyz.bluspring.kilt.injections.client.renderer.entity.ItemEntityRendererInjection;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererInject implements ItemEntityRendererInjection {
    @ModifyVariable(method = "render(Lnet/minecraft/world/entity/item/ItemEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/model/ItemTransforms;getTransform(Lnet/minecraft/world/item/ItemDisplayContext;)Lnet/minecraft/client/renderer/block/model/ItemTransform;", ordinal = 0), ordinal = 3)
    private float kilt$onlyBobIfRequired(float original) {
        if (shouldBob())
            return original;
        else
            return 0f;
    }

    // Kilt TODO: fix
    /*@ModifyArgs(method = "render(Lnet/minecraft/world/entity/item/ItemEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V", ordinal = 2))
    private void kilt$onlyTranslateIfSpread(Args args) {
        if (!shouldSpreadItems()) {
            args.set(0, 0f);
            args.set(1, 0f);
            args.set(2, 0f);
        }
    }

    @ModifyArgs(method = "render(Lnet/minecraft/world/entity/item/ItemEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V", ordinal = 3))
    private void kilt$onlyTranslateIfSprea2(Args args) {
        if (!shouldSpreadItems()) {
            args.set(0, 0f);
            args.set(1, 0f);
        }
    }*/

    // TODO: Forge removes the transform scales, do we need to handle that?

    @Override
    public boolean shouldSpreadItems() {
        return true;
    }

    @Override
    public boolean shouldBob() {
        return true;
    }
}

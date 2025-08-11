package xyz.bluspring.kilt.injects.client.renderer.entity.player;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererInject {
    @WrapOperation(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
    private <T extends LivingEntity> void kilt$callPlayerRenderEvent(PlayerRenderer instance, T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, Operation<Void> original) {
        if (NeoForge.EVENT_BUS.post(new RenderPlayerEvent.Pre((AbstractClientPlayer) entity, (PlayerRenderer) (Object) this, partialTicks, poseStack, buffer, packedLight)))
            return;

        //noinspection MixinExtrasOperationParameters
        original.call(instance, entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        NeoForge.EVENT_BUS.post(new RenderPlayerEvent.Post((AbstractClientPlayer) entity, (PlayerRenderer) (Object) this, partialTicks, poseStack, buffer, packedLight));
    }

    @WrapOperation(method = "getArmPose", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    private static boolean kilt$checkIsItemCrossbow(ItemStack instance, Item item, Operation<Boolean> original) {
        return original.call(instance, item) || instance.getItem() instanceof CrossbowItem;
    }

    @Inject(method = "getArmPose", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/HumanoidModel$ArmPose;ITEM:Lnet/minecraft/client/model/HumanoidModel$ArmPose;"), cancellable = true)
    private static void kilt$returnForgeArmPoseIfPossible(AbstractClientPlayer player, InteractionHand hand, CallbackInfoReturnable<HumanoidModel.ArmPose> cir, @Local ItemStack stack) {
        var forgeArmPose = IClientItemExtensions.of(stack)
            .getArmPose(player, hand, stack);

        if (forgeArmPose != null) {
            cir.setReturnValue(forgeArmPose);
        }
    }

    @WrapWithCondition(method = "renderRightHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/player/PlayerRenderer;renderHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;)V"))
    private boolean kilt$checkShouldRenderRightHand(PlayerRenderer instance, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, AbstractClientPlayer player, ModelPart rendererArm, ModelPart rendererArmwear) {
        return !ClientHooks.renderSpecificFirstPersonArm(poseStack, buffer, combinedLight, player, HumanoidArm.RIGHT);
    }

    @WrapWithCondition(method = "renderLeftHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/player/PlayerRenderer;renderHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;)V"))
    private boolean kilt$checkShouldRenderLeftHand(PlayerRenderer instance, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, AbstractClientPlayer player, ModelPart rendererArm, ModelPart rendererArmwear) {
        return !ClientHooks.renderSpecificFirstPersonArm(poseStack, buffer, combinedLight, player, HumanoidArm.LEFT);
    }

    @WrapOperation(method = "setupRotations(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;isInWater()Z"))
    private boolean kilt$checkCanSwimInFluidType(AbstractClientPlayer instance, Operation<Boolean> original) {
        return original.call(instance) || instance.isInFluidType((fluidType, height) -> instance.canSwimInFluidType(fluidType));
    }
}

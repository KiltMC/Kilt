// TRACKED HASH: b73c34d168c602ac81d45109572d392b4ad484f8
package xyz.bluspring.kilt.forgeinjects.client.renderer;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererInject {
    @Shadow @Final private Minecraft minecraft;

    @Shadow private ItemStack mainHandItem;

    @Shadow private ItemStack offHandItem;

    @Shadow private float mainHandHeight;

    @Shadow private float offHandHeight;

    @WrapWithCondition(method = "renderHandsWithItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
    private boolean kilt$cancelRenderIfEventCancelled(ItemInHandRenderer instance, AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand, float swingProgress, ItemStack stack, float equippedProgress, PoseStack poseStack, MultiBufferSource buffer, int combinedLight) {
        return !ForgeHooksClient.renderSpecificFirstPersonHand(hand, poseStack, buffer, combinedLight, partialTicks, pitch, swingProgress, equippedProgress, stack);
    }

    @WrapOperation(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;isUsingItem()Z", ordinal = 1))
    private boolean kilt$cancelIfForgeHandTransformApplied(AbstractClientPlayer instance, Operation<Boolean> original, @Local(argsOnly = true) ItemStack stack, @Local(argsOnly = true) PoseStack poseStack, @Local HumanoidArm arm, @Local(argsOnly = true, ordinal = 0) float partialTicks, @Local(argsOnly = true, ordinal = 2) float swingProgress, @Local(argsOnly = true, ordinal = 3) float equippedProgress) {
        return !IClientItemExtensions.of(stack).applyForgeHandTransform(poseStack, minecraft.player, arm, stack, partialTicks, equippedProgress, swingProgress) && original.call(instance);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F", shift = At.Shift.AFTER))
    private void kilt$initReequipAnim(CallbackInfo ci, @Share("reequipM") LocalBooleanRef reequipM, @Share("reequipO") LocalBooleanRef reequipO, @Local(ordinal = 0) ItemStack stack, @Local(ordinal = 1) ItemStack offhandStack, @Local LocalPlayer player) {
        reequipM.set(ForgeHooksClient.shouldCauseReequipAnimation(this.mainHandItem, stack, player.getInventory().selected));
        reequipO.set(ForgeHooksClient.shouldCauseReequipAnimation(this.offHandItem, offhandStack, -1));

        if (!reequipM.get() && this.mainHandItem != stack)
            this.mainHandItem = stack;

        if (!reequipO.get() && this.offHandItem != offhandStack)
            this.offHandItem = offhandStack;
    }

    // TODO: implement when ternaries are fixed in MixinExtras
    /*@Definition(id = "mainHandItem", field = "Lnet/minecraft/client/renderer/ItemInHandRenderer;mainHandItem:Lnet/minecraft/world/item/ItemStack;")
    @Definition(id = "itemStack", local = @Local)
    @Expression("this.mainHandItem == itemStack")
    @ModifyExpressionValue(method = "tick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$useReequipCheckForMainHand(boolean original) {

    }*/

    @WrapOperation(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z", ordinal = 1))
    private boolean kilt$useCrossbowInstanceOfCheck(ItemStack instance, Item item, Operation<Boolean> original) {
        return original.call(instance, item) || instance.getItem() instanceof CrossbowItem;
    }

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F", ordinal = 2), index = 0)
    private float kilt$useReequipCheckForMainHand(float original, @Share("reequipM") LocalBooleanRef reequipM, @Local LocalPlayer localPlayer) {
        float f = localPlayer.getAttackStrengthScale(1.0F);
        return (!reequipM.get() ? f * f * f : 0.0F) - this.mainHandHeight;
    }

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F", ordinal = 3), index = 0)
    private float kilt$useReequipCheckForOffHand(float original, @Share("reequipO") LocalBooleanRef reequipO) {
        return (!reequipO.get() ? 1.0F : 0.0F) - this.offHandHeight;
    }
}
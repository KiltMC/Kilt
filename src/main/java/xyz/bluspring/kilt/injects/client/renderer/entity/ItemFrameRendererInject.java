package xyz.bluspring.kilt.injects.client.renderer.entity;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.neoforged.neoforge.client.event.RenderItemInFrameEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

@Mixin(ItemFrameRenderer.class)
public abstract class ItemFrameRendererInject<T extends ItemFrame> {
    @Inject(method = "render(Lnet/minecraft/world/entity/decoration/ItemFrame;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/ItemFrame;getFramedMapId(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/level/saveddata/maps/MapId;", shift = At.Shift.AFTER))
    private void kilt$tryGetMapItemData(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci, @Share("mapData") LocalRef<MapItemSavedData> mapData, @Local ItemStack stack) {
        mapData.set(MapItem.getSavedData(stack, entity.level()));
    }

    @Definition(id = "mapId", local = @Local(type = MapId.class))
    @Expression("mapId != null")
    @WrapOperation(method = "render(Lnet/minecraft/world/entity/decoration/ItemFrame;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 0))
    private boolean kilt$allowRotationIfMapDataPresent(Object left, Object right, Operation<Boolean> original, @Share("mapData") LocalRef<MapItemSavedData> mapData) {
        return original.call(left, right) || mapData.get() != null;
    }

    @Definition(id = "mapId", local = @Local(type = MapId.class))
    @Expression("mapId != null")
    @WrapOperation(method = "render(Lnet/minecraft/world/entity/decoration/ItemFrame;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 1))
    private boolean kilt$checkCanRenderFrame(Object left, Object right, Operation<Boolean> original, @Share("mapData") LocalRef<MapItemSavedData> mapData, @Local(argsOnly = true) ItemFrame frame, @Local(argsOnly = true) PoseStack poseStack, @Local(argsOnly = true) MultiBufferSource bufferSource, @Local(argsOnly = true) int packedLight, @Cancellable CallbackInfo ci) {
        if (NeoForge.EVENT_BUS.post(new RenderItemInFrameEvent(frame, (ItemFrameRenderer<T>) (Object) this, poseStack, bufferSource, packedLight)).isCanceled()) {
            poseStack.popPose(); // We need to pop the pose so it closes correctly.
            ci.cancel();

            return false;
        }

        return original.call(left, right) || mapData.get() != null;
    }

    // Kilt: we don't have to use the getFramedMapId().getAsInt() arg patch, cuz we reetain the original OptionalInt from earlier.

    @WrapOperation(method = "render(Lnet/minecraft/world/entity/decoration/ItemFrame;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/MapItem;getSavedData(Lnet/minecraft/world/level/saveddata/maps/MapId;Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;"))
    private MapItemSavedData kilt$useAlreadyExistingMapData(MapId mapId, Level level, Operation<MapItemSavedData> original, @Share("mapData") LocalRef<MapItemSavedData> mapData) {
        if (mapData.get() != null)
            return mapData.get();

        return original.call(mapId, level);
    }

    @WrapOperation(method = "getFrameModelResourceLoc", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    private boolean kilt$checkIsMapItem(ItemStack instance, Item item, Operation<Boolean> original) {
        return original.call(instance, item) || instance.getItem() instanceof MapItem;
    }
}

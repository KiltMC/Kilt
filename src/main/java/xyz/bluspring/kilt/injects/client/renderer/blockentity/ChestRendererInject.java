// TRACKED HASH: 979c34bac4c2b61ba34abcb42332036ed82e63ee
package xyz.bluspring.kilt.injects.client.renderer.blockentity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.AABB;

@Mixin(ChestRenderer.class)
public abstract class ChestRendererInject<T extends BlockEntity> implements BlockEntityRenderer<T> {
    @Shadow private boolean xmasTextures;

    @Unique private boolean kilt$isDefault = false;

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Sheets;chooseMaterial(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/level/block/state/properties/ChestType;Z)Lnet/minecraft/client/resources/model/Material;"), method = "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V")
    public Material kilt$useForgeMaterial(BlockEntity blockEntity, ChestType chestType, boolean holiday, Operation<Material> original) {
        var material = this.getMaterial((T) blockEntity, chestType);

        if (kilt$isDefault) {
            return original.call(blockEntity, chestType, holiday);
        }

        return material;
    }

    protected Material getMaterial(T blockEntity, ChestType chestType) {
        kilt$isDefault = true;
        return Sheets.chooseMaterial(blockEntity, chestType, this.xmasTextures);
    }

    @Override
    public AABB getRenderBoundingBox(T blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return AABB.encapsulatingFullBlocks(pos.offset(-1, 0, -1), pos.offset(1, 1, 1));
    }
}
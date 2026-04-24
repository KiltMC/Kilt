package xyz.bluspring.kilt.injects.gametest.framework;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.world.level.block.entity.StructureBlockEntity;

@Mixin(GameTestInfo.class)
public abstract class GameTestInfoInject {
    @Shadow @Nullable private StructureBlockEntity structureBlockEntity;

    @Shadow
    public abstract String getTestName();

    @Inject(method = "prepareTestStructure", at = @At(value = "INVOKE", target = "Lnet/minecraft/gametest/framework/StructureUtils;addCommandBlockAndButtonToStartTest(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Rotation;Lnet/minecraft/server/level/ServerLevel;)V"))
    private void kilt$setMetadataToStructure(CallbackInfoReturnable<GameTestInfo> cir) {
        this.structureBlockEntity.setMetaData(this.getTestName());
    }
}

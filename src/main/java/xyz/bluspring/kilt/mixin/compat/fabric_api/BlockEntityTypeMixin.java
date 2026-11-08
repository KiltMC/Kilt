package xyz.bluspring.kilt.mixin.compat.fabric_api;

import java.util.Set;

import com.bawnorton.mixinsquared.TargetHandler;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

@Mixin(value = BlockEntityType.class, priority = 1050)
public abstract class BlockEntityTypeMixin<T extends BlockEntity> {
    @Shadow
    @Final
    private Set<Block> validBlocks;

    @Dynamic
    @TargetHandler(mixin = "net.fabricmc.fabric.mixin.object.builder.BlockEntityTypeMixin", name = "mutableBlocks", prefix = "handler")
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true)
    private void kilt$fabric_api$workaroundPlaceboStupidity(CallbackInfo ci) {
        // Placebo has a "DeferredSet" for storing DeferredHolders of a block entity type.
        // I fucking hate this deferred system.
        if (this.validBlocks.getClass().getSimpleName().equals("DeferredSet")) {
            ci.cancel();
        }
    }
}

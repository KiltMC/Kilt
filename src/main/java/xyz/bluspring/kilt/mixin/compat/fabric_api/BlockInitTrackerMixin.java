package xyz.bluspring.kilt.mixin.compat.fabric_api;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.fabricmc.fabric.impl.registry.sync.trackers.vanilla.BlockInitTracker;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = BlockInitTracker.class, remap = false)
public class BlockInitTrackerMixin {
    @WrapWithCondition(method = "onEntryAdded(ILnet/minecraft/resources/ResourceLocation;Lnet/minecraft/world/level/block/Block;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getLootTable()Lnet/minecraft/resources/ResourceLocation;"))
    private boolean kilt$avoidCallingLootEarly(Block instance) {
        return false;
    }
}

package xyz.bluspring.kilt.mixin.compat.fabric_api;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.impl.registry.sync.trackers.vanilla.BlockInitTracker;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = BlockInitTracker.class, remap = false)
public class BlockInitTrackerMixin {
    // Fabric api just randomly calls this for whatever reason? It causes forge mods that used Deferred Objects to error so uhh just ignore it lol
    @WrapOperation(method = "onEntryAdded(ILnet/minecraft/resources/ResourceLocation;Lnet/minecraft/world/level/block/Block;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getLootTable()Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation kilt$catchErrorOnForgeMods(Block instance, Operation<ResourceLocation> original) {
        try {
            return original.call(instance);
        } catch (Throwable throwable) {
            return null;
        }
    }
}

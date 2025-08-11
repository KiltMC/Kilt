package xyz.bluspring.kilt.injects.client.renderer.blockentity;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(BlockEntityRenderers.class)
public abstract class BlockEntityRenderersInject {
    @Shadow @Final @Mutable
    private static Map<BlockEntityType<?>, BlockEntityRendererProvider<?>> PROVIDERS;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void kilt$useConcurrentHashMap(CallbackInfo ci) {
        PROVIDERS = new ConcurrentHashMap<>(PROVIDERS);
    }
}

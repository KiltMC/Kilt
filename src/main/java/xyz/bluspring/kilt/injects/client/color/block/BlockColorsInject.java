// TRACKED HASH: 0ce404d106018ee2fbf70e284692f9e57382cddd
package xyz.bluspring.kilt.injects.client.color.block;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.client.color.block.BlockColorsInjection;

import java.util.HashMap;
import java.util.Map;

@Mixin(BlockColors.class)
public class BlockColorsInject implements BlockColorsInjection {
    @Unique
    private Map<Block, BlockColor> kilt$blockColors;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$createForgeBlockColorsWorkaround(CallbackInfo ci) {
        this.kilt$blockColors = new HashMap<>();
    }

    @Inject(at = @At("RETURN"), method = "createDefault")
    private static void kilt$initForgeBlockColors(CallbackInfoReturnable<BlockColors> cir) {
        ClientHooks.onBlockColorsInit(cir.getReturnValue());
    }

    @Override
    public Map<Block, BlockColor> kilt$getBlockColors() {
        return this.kilt$blockColors;
    }

    @Inject(method = "register", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/IdMapper;addMapping(Ljava/lang/Object;I)V"))
    private void kilt$registerBlockToForgeColor(BlockColor blockColor, Block[] blocks, CallbackInfo ci, @Local Block block) {
        this.kilt$blockColors.put(block, blockColor);
    }
}

// TRACKED HASH: 0ce404d106018ee2fbf70e284692f9e57382cddd
package xyz.bluspring.kilt.forgeinjects.client.color.block;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.client.color.block.BlockColorsInjection;
import xyz.bluspring.kilt.workarounds.IdMapperDelegate;

import java.util.Map;

@Mixin(BlockColors.class)
public class BlockColorsInject implements BlockColorsInjection {
    @Shadow @Final private IdMapper<BlockColor> blockColors;

    @Unique
    private Map<Holder.Reference<Block>, BlockColor> kilt$blockColors;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$createForgeBlockColorsWorkaround(CallbackInfo ci) {
        this.kilt$blockColors = new IdMapperDelegate<>(BuiltInRegistries.BLOCK, this.blockColors);
    }

    @Inject(at = @At("RETURN"), method = "createDefault")
    private static void kilt$initForgeBlockColors(CallbackInfoReturnable<BlockColors> cir) {
        ForgeHooksClient.onBlockColorsInit(cir.getReturnValue());
    }

    @Override
    public Map<Holder.Reference<Block>, BlockColor> kilt$getBlockColors() {
        return this.kilt$blockColors;
    }
}
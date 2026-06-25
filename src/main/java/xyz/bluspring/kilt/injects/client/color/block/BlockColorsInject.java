// TRACKED HASH: 0ce404d106018ee2fbf70e284692f9e57382cddd
package xyz.bluspring.kilt.injects.client.color.block;

import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.color.block.BlockColors;

@Mixin(BlockColors.class)
public abstract class BlockColorsInject {
    @Inject(at = @At("RETURN"), method = "createDefault")
    private static void kilt$initForgeBlockColors(CallbackInfoReturnable<BlockColors> cir) {
        ModLoader.postEvent(new RegisterColorHandlersEvent.BlockTintSources(cir.getReturnValue()));
    }
}

package xyz.bluspring.kilt.forgeinjects.world.item;

import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.bluspring.kilt.injections.world.item.CreativeModeTabInjection;

@Mixin(CreativeModeTab.class)
public class CreativeModeTabInject {
    @ModifyVariable(at = @At("HEAD"), method = "<init>(ILjava/lang/String;)V", ordinal = 0, argsOnly = true)
    private static int kilt$modifyIndex(int i) {
        return CreativeModeTabInjection.updateIndex(i);
    }
}

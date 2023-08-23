package xyz.bluspring.kilt.forgeinjects.world.item;

import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.item.CreativeModeTabInjection;

@Mixin(CreativeModeTab.class)
public abstract class CreativeModeTabInject implements CreativeModeTabInjection {
    @CreateStatic
    private static int getGroupCountSafe() {
        return CreativeModeTabInjection.getGroupCountSafe();
    }

    @CreateStatic
    private static int updateIndex(int i) {
        return CreativeModeTabInjection.updateIndex(i);
    }

    @ModifyVariable(at = @At("HEAD"), method = "<init>(ILjava/lang/String;)V", ordinal = 0, argsOnly = true)
    private static int kilt$modifyIndex(int i) {
        return CreativeModeTabInjection.updateIndex(i);
    }

    public CreativeModeTabInject(int i, String name) {}

    @CreateInitializer
    public CreativeModeTabInject(String name) {
        this(-1, name);
    }
}

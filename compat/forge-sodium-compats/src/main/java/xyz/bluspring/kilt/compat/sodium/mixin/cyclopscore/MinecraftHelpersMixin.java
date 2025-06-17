package xyz.bluspring.kilt.compat.sodium.mixin.cyclopscore;

import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = MinecraftHelpers.class, remap = false)
public class MinecraftHelpersMixin {
    /**
     * @author AlphaMode
     * @reason wtf
     */
    @Overwrite
    public static boolean isModdedEnvironment() {
        return true;
    }
}

package xyz.bluspring.kilt.compat.forge.mixin.cyclopscore;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;

@IfModLoaded("cyclopscore")
@Pseudo
@Mixin(targets = "org.cyclops.cyclopscore.helper.MinecraftHelpers", remap = false)
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

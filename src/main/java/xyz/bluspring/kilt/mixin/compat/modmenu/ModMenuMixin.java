package xyz.bluspring.kilt.mixin.compat.modmenu;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.ConfigScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.loader.KiltLoader;

@IfModLoaded("modmenu")
@Pseudo
@Mixin(targets = "com.terraformersmc.modmenu.ModMenu")
public abstract class ModMenuMixin {
    @ModifyReturnValue(method = "getConfigScreen", at = @At("RETURN"))
    private static Screen kilt$addForgeConfigScreenIfPossible(Screen original, @Local(argsOnly = true) String modId, @Local(argsOnly = true) Screen parent) {
        var kiltMod = KiltLoader.INSTANCE.getMod(modId);
        if (kiltMod != null) {
            var screenExtension = kiltMod.getContainer().getCustomExtension(ConfigScreenHandler.ConfigScreenFactory.class);

            if (screenExtension.isPresent()) {
                return screenExtension.get().screenFunction().apply(Minecraft.getInstance(), parent);
            }
        }

        return original;
    }
}

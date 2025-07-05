package xyz.bluspring.kilt.mixin.compat.modmenu;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.terraformersmc.modmenu.util.mod.fabric.FabricIconHandler;
import net.fabricmc.loader.api.ModContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.loader.KiltLoader;

@Mixin(FabricIconHandler.class)
public abstract class FabricIconHandlerMixin {
    @WrapWithCondition(method = "createIcon", at = @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/Validate;validState(ZLjava/lang/String;[Ljava/lang/Object;)V"))
    private static boolean kilt$useIconAnywayIfForge(boolean expression, String message, Object[] values, @Local(argsOnly = true) ModContainer iconSource) {
        return !KiltLoader.Companion.getInstance().hasMod(iconSource.getMetadata().getId());
    }
}

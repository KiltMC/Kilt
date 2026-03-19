package xyz.bluspring.kilt.compat.fabric.mixin.amecsapi;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.InputConstants;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import de.siphalor.amecs.impl.KeyBindingManager;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.List;
import java.util.Map;

@Pseudo
@IfModLoaded(value = "amecsapi", maxVersion = "1.7.0")
@Mixin(KeyBindingManager.class)
public abstract class KeyBindingManagerMixin {

    @Redirect(
            method = "register",
            at = @At(
                    value = "INVOKE",
                    target = "Lde/siphalor/amecs/impl/KeyBindingManager;addKeyBindingToListFromMap(Ljava/util/Map;Lnet/minecraft/client/KeyMapping;)Z"
            ),
            slice = @Slice(
                    from = @At(
                            value = "FIELD",
                            target = "Lde/siphalor/amecs/impl/KeyBindingManager;keysById:Ljava/util/Map;"
                    )
            )
    )
    private static boolean kilt$skipRegularKeybind(
            Map<InputConstants.Key, List<KeyMapping>> targetMap, KeyMapping keyBinding
    ) {
        return false;
    }
}

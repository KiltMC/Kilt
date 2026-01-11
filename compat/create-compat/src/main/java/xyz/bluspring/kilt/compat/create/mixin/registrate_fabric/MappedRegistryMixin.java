package xyz.bluspring.kilt.compat.create.mixin.registrate_fabric;

import net.minecraft.core.MappedRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

// TODO: REMOVE/FIX THIS BULLSHIT
@Mixin(MappedRegistry.class)
public class MappedRegistryMixin {
    @Redirect(
            method = "freeze",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;isEmpty()Z"
            )
    )
    private boolean kilt$makeEmptyNotEmptyHehe(Map instance) {
        return true;
    }
}

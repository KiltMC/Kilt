package xyz.bluspring.kilt.compat.forge.mixin.attributeslib;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import dev.shadowsoffire.attributeslib.asm.ALHooks;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@IfModLoaded("attributeslib")
@Mixin(EffectRenderingInventoryScreen.class)
public abstract class EffectRenderingInventoryScreenMixin {

    // Re-implements the following CoreMod.
    // https://github.com/Shadows-of-Fire/Apothic-Attributes/blob/1.20/src/main/resources/coremods/potion_gui_tooltips.js
    @ModifyExpressionValue(
        method = "renderEffects",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;of(Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/List;"
        )
    )
    private List<Component> kilt$attributeslib$getEffectTooltip(
        List<Component> original, @Local MobEffectInstance mobEffectInstance
    ) {
        return ALHooks.getEffectTooltip((EffectRenderingInventoryScreen<?>) (Object) this, mobEffectInstance, original);
    }

}

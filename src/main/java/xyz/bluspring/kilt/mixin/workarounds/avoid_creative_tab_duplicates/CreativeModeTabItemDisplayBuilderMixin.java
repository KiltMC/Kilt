package xyz.bluspring.kilt.mixin.workarounds.avoid_creative_tab_duplicates;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.Kilt;

@Mixin(CreativeModeTab.ItemDisplayBuilder.class)
public abstract class CreativeModeTabItemDisplayBuilderMixin {
    @Shadow @Final private CreativeModeTab tab;

    @Definition(id = "IllegalStateException", type = IllegalStateException.class)
    @Expression("throw new IllegalStateException(?)")
    @Inject(method = "accept", at = @At("MIXINEXTRAS:EXPRESSION"), cancellable = true)
    private void kilt$avoidCrashingStackAdd(ItemStack stack, CreativeModeTab.TabVisibility tabVisibility, CallbackInfo ci) {
        // because Neo actually ignores this unintentionally, fun fact.
        // see: https://github.com/neoforged/NeoForge/issues/3431
        ci.cancel();
        Kilt.Companion.getLogger().error("Accidentally adding the same item stack twice " + stack.getDisplayName().getString() + " to a Creative Mode Tab: " + this.tab.getDisplayName().getString());
        Kilt.Companion.getLogger().error("Kilt is working around this, because NeoForge mods actually rely on this behaviour.");
    }
}

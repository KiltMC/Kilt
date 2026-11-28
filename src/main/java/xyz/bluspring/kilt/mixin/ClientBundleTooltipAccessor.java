package xyz.bluspring.kilt.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
import net.minecraft.world.item.component.BundleContents;

@Mixin(ClientBundleTooltip.class)
public interface ClientBundleTooltipAccessor {
    @Accessor
    BundleContents getContents();
}

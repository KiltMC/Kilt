package xyz.bluspring.kilt.forgeinjects.client.gui.screens.inventory.tooltip;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraftforge.client.gui.ClientTooltipComponentManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientTooltipComponent.class)
public interface ClientTooltipComponentInject {
    @Inject(at = @At(value = "INVOKE", target = "Ljava/lang/IllegalArgumentException;<init>(Ljava/lang/String;)V", shift = At.Shift.BEFORE), method = "create(Lnet/minecraft/world/inventory/tooltip/TooltipComponent;)Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;", cancellable = true)
    private static void kilt$getForgeClientTooltip(TooltipComponent tooltipComponent, CallbackInfoReturnable<ClientTooltipComponent> cir) {
        var result = ClientTooltipComponentManager.createClientTooltipComponent(tooltipComponent);
        if (result != null)
            cir.setReturnValue(result);
    }
}

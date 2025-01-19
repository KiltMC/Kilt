package xyz.bluspring.kilt.forgeinjects.client;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.client.MouseHandlerInjection;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerInject implements MouseHandlerInjection {
    // Kilt: this should be handled by Architectury already.


    @Shadow private double accumulatedDX;

    @Shadow private double accumulatedDY;

    @Override
    public double getXVelocity() {
        return this.accumulatedDX;
    }

    @Override
    public double getYVelocity() {
        return this.accumulatedDY;
    }
}

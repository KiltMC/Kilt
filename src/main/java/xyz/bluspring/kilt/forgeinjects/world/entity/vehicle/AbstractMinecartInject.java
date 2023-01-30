package xyz.bluspring.kilt.forgeinjects.world.entity.vehicle;

import net.minecraft.world.entity.vehicle.AbstractMinecart;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.entity.AbstractMinecartInjection;

@Mixin(AbstractMinecart.class)
public class AbstractMinecartInject implements AbstractMinecartInjection {
}

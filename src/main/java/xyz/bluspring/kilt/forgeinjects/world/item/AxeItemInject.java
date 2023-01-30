package xyz.bluspring.kilt.forgeinjects.world.item;

import net.minecraft.world.item.AxeItem;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.item.AxeItemInjection;

@Mixin(AxeItem.class)
public class AxeItemInject implements AxeItemInjection {
}

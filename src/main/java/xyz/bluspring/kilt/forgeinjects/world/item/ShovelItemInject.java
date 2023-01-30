package xyz.bluspring.kilt.forgeinjects.world.item;

import net.minecraft.world.item.ShovelItem;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.item.ShovelItemInjection;

@Mixin(ShovelItem.class)
public class ShovelItemInject implements ShovelItemInjection {
}

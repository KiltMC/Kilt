package xyz.bluspring.kilt.forgeinjects.world.item;

import net.minecraft.world.item.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.world.item.DyeColorInjection;

@Mixin(DyeColor.class)
public class DyeColorInject implements DyeColorInjection {
}

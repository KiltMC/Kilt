package xyz.bluspring.kilt.forgeinjects.world.item.alchemy;

import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.common.extensions.IForgePotion;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Potion.class)
public class PotionInject implements IForgePotion {
}

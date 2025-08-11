// TRACKED HASH: be9d3477b05dd9f9293d4df2e007ebba088680e2
package xyz.bluspring.kilt.injects.world.item.alchemy;

import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.neoforge.common.extensions.IForgePotion;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Potion.class)
public class PotionInject implements IForgePotion {
}
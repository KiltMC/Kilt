package xyz.bluspring.kilt.forgeinjects.world.level.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.common.extensions.IForgeItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public abstract class ItemInject implements IForgeItem {

}

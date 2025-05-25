package xyz.bluspring.kilt.forgeinjects.world.item;

import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Items.class)
public abstract class ItemsInject {
    // Kilt: we don't need this
}

package xyz.bluspring.kilt.mixin.compat.porting_lib.extensions;

import io.github.fabricators_of_create.porting_lib.item.DamageableItem;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DamageableItem.class)
public interface DamageableItemMixin extends IItemExtension {
}

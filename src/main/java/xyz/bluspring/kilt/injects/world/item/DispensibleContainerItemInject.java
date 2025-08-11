// TRACKED HASH: eb3294cfe8207d695cc790cd2173da72510954e6
package xyz.bluspring.kilt.injects.world.item;

import net.minecraft.world.item.DispensibleContainerItem;
import net.neoforged.neoforge.common.extensions.IForgeDispensibleContainerItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DispensibleContainerItem.class)
public interface DispensibleContainerItemInject extends IForgeDispensibleContainerItem {
}
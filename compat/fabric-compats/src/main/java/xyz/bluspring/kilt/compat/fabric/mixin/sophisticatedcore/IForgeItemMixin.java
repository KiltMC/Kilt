package xyz.bluspring.kilt.compat.fabric.mixin.sophisticatedcore;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import net.p3pp3rf1y.sophisticatedcore.extensions.item.SophisticatedItem;
import org.spongepowered.asm.mixin.Mixin;

@IfModLoaded("sophisticatedcore")
@Mixin(IItemExtension.class)
public interface IForgeItemMixin extends SophisticatedItem {
}

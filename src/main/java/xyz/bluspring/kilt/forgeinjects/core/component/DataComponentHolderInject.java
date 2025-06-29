package xyz.bluspring.kilt.forgeinjects.core.component;

import net.minecraft.core.component.DataComponentHolder;
import net.neoforged.neoforge.common.extensions.IDataComponentHolderExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DataComponentHolder.class)
public interface DataComponentHolderInject extends IDataComponentHolderExtension {
}

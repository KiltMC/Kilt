package xyz.bluspring.kilt.forgeinjects.core;

import net.minecraft.core.HolderSet;
import net.minecraftforge.common.extensions.IForgeHolderSet;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(HolderSet.class)
public interface HolderSetInject<T> extends IForgeHolderSet<T> {

}

// TRACKED HASH: 1d41e3616afa3056987032c75858fb30258b2c0c
package xyz.bluspring.kilt.forgeinjects.core;

import net.minecraft.core.HolderSet;
import net.minecraftforge.common.extensions.IForgeHolderSet;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(HolderSet.class)
public interface HolderSetInject extends IForgeHolderSet {

}
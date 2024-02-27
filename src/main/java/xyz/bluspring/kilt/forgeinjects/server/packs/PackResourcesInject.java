// TRACKED HASH: 0a11cec4b35f7391f587273510ae508386f33218
package xyz.bluspring.kilt.forgeinjects.server.packs;

import net.minecraft.server.packs.PackResources;
import net.minecraftforge.common.extensions.IForgePackResources;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PackResources.class)
public interface PackResourcesInject extends IForgePackResources {
}
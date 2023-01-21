package xyz.bluspring.kilt.forgeinjects.server.packs;

import net.minecraft.server.packs.PackResources;
import net.minecraftforge.common.extensions.IForgePackResources;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PackResources.class)
public interface PackResourcesInject extends IForgePackResources {
}

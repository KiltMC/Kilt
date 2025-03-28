package xyz.bluspring.kilt.forgeinjects.world.level.portal;

import net.minecraft.world.level.portal.PortalForcer;
import net.minecraftforge.common.util.ITeleporter;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PortalForcer.class)
public abstract class PortalForcerInject implements ITeleporter {
}

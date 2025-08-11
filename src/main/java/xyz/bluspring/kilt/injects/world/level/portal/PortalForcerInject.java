package xyz.bluspring.kilt.injects.world.level.portal;

import net.minecraft.world.level.portal.PortalForcer;
import net.neoforged.neoforge.common.util.ITeleporter;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PortalForcer.class)
public abstract class PortalForcerInject implements ITeleporter {
}

package xyz.bluspring.kilt.mixin.compat.mkb;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraftforge.client.extensions.IForgeKeyMapping;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "committee.nova.mkb.api.IKeyBinding")
@IfModLoaded("mkb")
public interface IKeyBindingMixin extends IForgeKeyMapping {
}

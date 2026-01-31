package xyz.bluspring.kilt.compat.fabric.mixin.mkb;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.neoforged.neoforge.client.extensions.IKeyMappingExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "committee.nova.mkb.api.IKeyBinding")
@IfModLoaded("mkb")
public interface IKeyBindingMixin extends IKeyMappingExtension {
}

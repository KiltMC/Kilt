package xyz.bluspring.kilt.compat.fabric.mixin.mkb;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraftforge.client.extensions.IForgeKeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "committee.nova.mkb.api.IKeyBinding")
@IfModLoaded("mkb")
public interface IKeyBindingMixin extends IForgeKeyMapping {
}

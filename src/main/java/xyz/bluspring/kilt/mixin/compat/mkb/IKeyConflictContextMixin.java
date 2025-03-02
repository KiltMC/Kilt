package xyz.bluspring.kilt.mixin.compat.mkb;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraftforge.client.settings.IKeyConflictContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "committee.nova.mkb.api.IKeyConflictContext")
@IfModLoaded("mkb")
public interface IKeyConflictContextMixin extends IKeyConflictContext {
}

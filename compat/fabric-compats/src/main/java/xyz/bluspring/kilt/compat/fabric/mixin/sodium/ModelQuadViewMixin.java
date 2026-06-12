package xyz.bluspring.kilt.compat.fabric.mixin.sodium;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.compat.fabric.sodium.ModelQuadViewExtension;

@IfModLoaded("sodium")
@Mixin(ModelQuadView.class)
public interface ModelQuadViewMixin extends ModelQuadViewExtension {
}

package xyz.bluspring.kilt.compat.fabric.mixin.sodium;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuad;
import me.jellysquid.mods.sodium.client.util.ModelQuadUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.compat.fabric.sodium.ModelQuadViewExtension;

@IfModLoaded("sodium")
@Mixin(ModelQuad.class)
public abstract class ModelQuadMixin implements ModelQuadViewExtension {

    @Shadow
    @Final
    private int[] data;

    @Override
    public int getLight(int idx) {
        return this.data[ModelQuadUtil.vertexOffset(idx) + ModelQuadUtil.LIGHT_INDEX];
    }

    @Override
    public int getForgeNormal(int idx) {
        return this.data[ModelQuadUtil.vertexOffset(idx) + ModelQuadUtil.NORMAL_INDEX];
    }
}

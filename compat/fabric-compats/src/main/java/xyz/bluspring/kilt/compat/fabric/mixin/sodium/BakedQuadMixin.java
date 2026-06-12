package xyz.bluspring.kilt.compat.fabric.mixin.sodium;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import me.jellysquid.mods.sodium.client.util.ModelQuadUtil;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.compat.fabric.sodium.ModelQuadViewExtension;

@IfModLoaded("sodium")
@Mixin(BakedQuad.class)
public abstract class BakedQuadMixin implements ModelQuadViewExtension {

    @Shadow
    @Final
    protected int[] vertices;

    @Override
    public int getLight(int idx) {
        return this.vertices[ModelQuadUtil.vertexOffset(idx) + ModelQuadUtil.LIGHT_INDEX];
    }

    @Override
    public int getForgeNormal(int idx) {
        return this.vertices[ModelQuadUtil.vertexOffset(idx) + ModelQuadUtil.NORMAL_INDEX];
    }
}

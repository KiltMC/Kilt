// TRACKED HASH: 701e6411dae2a52635a7b1ef1493b50b798afa97
package xyz.bluspring.kilt.forgeinjects.client.renderer.chunk;

import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.IForgeBlockAndTintGetter;
import net.minecraftforge.client.model.data.ModelDataManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.client.renderer.chunk.RenderChunkRegionInjection;

@Mixin(RenderChunkRegion.class)
public class RenderChunkRegionInject implements RenderChunkRegionInjection {

    @Shadow @Final protected Level level;

    @Override
    public float getShade(float normalX, float normalY, float normalZ, boolean shade) {
        return ((IForgeBlockAndTintGetter) this.level).getShade(normalX, normalY, normalZ, shade);
    }

    @Override
    public ModelDataManager getModelDataManager() {
        return this.level.getModelDataManager();
    }
}
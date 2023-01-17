package xyz.bluspring.kilt.mixin;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ItemBlockRenderTypes.class)
public interface ItemBlockRenderTypesAccessor {
    @Accessor
    static boolean isRenderCutout() {
        throw new UnsupportedOperationException();
    }

    @Accessor("TYPE_BY_BLOCK")
    static Map<Block, RenderType> getTypeByBlock() {
        throw new UnsupportedOperationException();
    }

    @Accessor("TYPE_BY_FLUID")
    static Map<Fluid, RenderType> getTypeByFluid() {
        throw new UnsupportedOperationException();
    }
}

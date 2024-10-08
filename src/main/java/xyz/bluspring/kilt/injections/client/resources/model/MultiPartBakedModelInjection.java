package xyz.bluspring.kilt.injections.client.resources.model;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;
import java.util.List;

public interface MultiPartBakedModelInjection {
    default BitSet getSelectors(@Nullable BlockState state) {
        throw new IllegalStateException();
    }

    default List<BakedQuad> getQuads(@Nullable BlockState p_235050_, @Nullable Direction p_235051_, RandomSource p_235052_, ModelData modelData, @Nullable RenderType renderType) {
        throw new IllegalStateException();
    }
}

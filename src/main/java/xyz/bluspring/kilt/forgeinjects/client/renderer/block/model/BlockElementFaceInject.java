// TRACKED HASH: c651e93bacb2243f18c5b16ca365e199444ccfb3
package xyz.bluspring.kilt.forgeinjects.client.renderer.block.model;

import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.core.Direction;
import net.minecraftforge.client.model.ForgeFaceData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.client.renderer.block.model.BlockElementFaceInjection;
import xyz.bluspring.kilt.injections.client.renderer.block.model.BlockElementInjection;

@Mixin(BlockElementFace.class)
public class BlockElementFaceInject implements BlockElementFaceInjection {
    @Unique private ForgeFaceData faceData;
    @Unique private BlockElement parent;

    public BlockElementFaceInject(@Nullable Direction cullForDirection, int tintIndex, String texture, BlockFaceUV uv) {}
    @CreateInitializer
    public BlockElementFaceInject(@Nullable Direction cullForDirection, int tintIndex, String texture, BlockFaceUV uv, @Nullable ForgeFaceData faceData) {
        this(cullForDirection, tintIndex, texture, uv);
        this.faceData = faceData;
    }

    @Override
    public void kilt$setParent(BlockElement parent) {
        this.parent = parent;
    }

    @Override
    public ForgeFaceData getFaceData() {
        if (this.faceData != null)
            return this.faceData;
        else if (this.parent != null)
            return ((BlockElementInjection) this.parent).getFaceData();

        return ForgeFaceData.DEFAULT;
    }

    @Override
    public void kilt$setFaceData(ForgeFaceData faceData) {
        this.faceData = faceData;
    }
}
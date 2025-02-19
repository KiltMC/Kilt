package xyz.bluspring.kilt.injections.client.renderer.block.model;

import net.minecraftforge.client.model.geometry.BlockGeometryBakingContext;

public interface BlockModelInjection {
    BlockGeometryBakingContext kilt$getCustomData();
}

package xyz.bluspring.kilt.injections.client.renderer.block.model;

import net.minecraftforge.client.model.ForgeFaceData;

public interface BlockElementInjection {
    ForgeFaceData getFaceData();
    void setFaceData(ForgeFaceData faceData);
}

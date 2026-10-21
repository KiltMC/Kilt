package xyz.bluspring.kilt.injections.client.resources.model.cuboid;

import net.neoforged.neoforge.client.model.ExtraFaceData;
import xyz.bluspring.kilt.util.KiltHelper;

public interface CuboidModelElementInjection {
    default ExtraFaceData faceData() {
        throw KiltHelper.createMixinException(CuboidModelElementInjection.class, "faceData");
    }
}

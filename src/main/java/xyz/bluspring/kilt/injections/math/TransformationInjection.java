package xyz.bluspring.kilt.injections.math;

import com.mojang.math.Transformation;
import org.joml.Matrix3f;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;
import xyz.bluspring.kilt.util.KiltHelper;

@FabricInjectedInterface(Transformation.class)
public interface TransformationInjection {
    default Matrix3f getNormalMatrix() {
        throw KiltHelper.createMixinException(TransformationInjection.class, "getNormalMatrix");
    }
}

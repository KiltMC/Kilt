package xyz.bluspring.kilt.injections.math;

import com.mojang.math.Transformation;
import org.joml.Matrix3f;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;

@FabricInjectedInterface(Transformation.class)
public interface TransformationInjection {
    Matrix3f getNormalMatrix();
}

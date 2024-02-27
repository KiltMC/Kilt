// TRACKED HASH: 530ffd564fc9a3e6a9785a5c90873c529567a4cc
package xyz.bluspring.kilt.forgeinjects.math;

import com.mojang.math.Transformation;
import net.minecraftforge.common.extensions.IForgeTransformation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import xyz.bluspring.kilt.injections.math.TransformationInjection;

@Mixin(Transformation.class)
public class TransformationInject implements IForgeTransformation, TransformationInjection {
    @Shadow @Final private Matrix4f matrix;
    @Unique private Matrix3f normalTransform = null;

    public Matrix3f getNormalMatrix() {
        checkNormalTransform();
        return normalTransform;
    }

    private void checkNormalTransform() {
        if (normalTransform == null) {
            normalTransform = new Matrix3f(matrix);
            normalTransform.invert();
            normalTransform.transpose();
        }
    }
}
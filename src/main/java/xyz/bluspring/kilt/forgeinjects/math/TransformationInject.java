package xyz.bluspring.kilt.forgeinjects.math;

import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import net.minecraftforge.client.extensions.IForgeTransformation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Transformation.class)
public class TransformationInject implements IForgeTransformation {
    @Shadow @Final private Matrix4f matrix;
    @Unique
    private Matrix3f normalTransform = null;

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

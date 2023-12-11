package xyz.bluspring.kilt.forgeinjects.math;

import com.mojang.math.Matrix4f;
import io.github.fabricators_of_create.porting_lib.extensions.Matrix4fExtensions;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.math.Matrix4fInjection;

@Mixin(Matrix4f.class)
public class Matrix4fInject implements Matrix4fInjection, Matrix4fExtensions {
    @CreateInitializer
    public Matrix4fInject(float[] values) {
        this.fromFloatArray(values);
    }
}

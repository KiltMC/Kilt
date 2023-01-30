package xyz.bluspring.kilt.injections.math;

import com.mojang.math.Matrix4f;

public interface Matrix4fInjection {
    static Matrix4f of(float[] values) {
        var matrix = new Matrix4f();
        matrix.fromFloatArray(values);

        return matrix;
    }
}

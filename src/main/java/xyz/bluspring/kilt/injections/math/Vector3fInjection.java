package xyz.bluspring.kilt.injections.math;

import com.mojang.math.Vector3f;

public interface Vector3fInjection {
    static Vector3f of(float[] floats) {
        return new Vector3f(floats[0], floats[1], floats[2]);
    }
}

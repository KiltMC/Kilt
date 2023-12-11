package xyz.bluspring.kilt.forgeinjects.math;

import com.mojang.math.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.math.Vector3fInjection;

@Mixin(Vector3f.class)
public class Vector3fInject implements Vector3fInjection {
    @CreateInitializer
    public Vector3fInject(float[] floats) {
        this(floats[0], floats[1], floats[2]);
    }

    public Vector3fInject(float f, float g, float h) {}
}

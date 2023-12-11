package xyz.bluspring.kilt.forgeinjects.math;

import com.mojang.math.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Vector4f.class)
public class Vector4fInject {
    @Shadow private float x;

    @Shadow private float y;

    @Shadow private float z;

    @Shadow private float w;

    public void set(float[] values) {
        this.x = values[0];
        this.y = values[1];
        this.z = values[2];
        this.w = values[3];
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public void setW(float w) {
        this.w = w;
    }
}

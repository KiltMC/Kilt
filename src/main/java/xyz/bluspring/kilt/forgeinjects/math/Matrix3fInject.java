package xyz.bluspring.kilt.forgeinjects.math;

import com.mojang.math.Matrix3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Matrix3f.class)
public abstract class Matrix3fInject {
    @Shadow public abstract void load(Matrix3f other);

    public void multiplyBackward(Matrix3f other) {
        var copy = other.copy();
        copy.mul((Matrix3f) (Object) this);
        this.load(copy);
    }
}

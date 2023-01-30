package xyz.bluspring.kilt.forgeinjects.math;

import com.mojang.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.math.Matrix4fInjection;

@Mixin(Matrix4f.class)
public class Matrix4fInject implements Matrix4fInjection {
}

package xyz.bluspring.kilt.forgeinjects.math;

import com.mojang.math.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.math.Vector3fInjection;

@Mixin(Vector3f.class)
public class Vector3fInject implements Vector3fInjection {
}

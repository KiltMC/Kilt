package xyz.bluspring.kilt.forgeinjects.math;

import com.mojang.math.Transformation;
import net.minecraftforge.client.extensions.IForgeTransformation;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Transformation.class)
public class TransformationInject implements IForgeTransformation {
}

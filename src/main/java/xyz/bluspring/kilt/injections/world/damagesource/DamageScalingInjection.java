package xyz.bluspring.kilt.injections.world.damagesource;

import net.minecraft.world.damagesource.DamageScaling;
import net.neoforged.neoforge.common.damagesource.IScalingFunction;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;
import xyz.bluspring.kilt.util.KiltHelper;

@FabricInjectedInterface(DamageScaling.class)
public interface DamageScalingInjection {
    default IScalingFunction getScalingFunction() {
        throw KiltHelper.createMixinException(DamageScalingInjection.class, "getScalingFunction");
    }
}

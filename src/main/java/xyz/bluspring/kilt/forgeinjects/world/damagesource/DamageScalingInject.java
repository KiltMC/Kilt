package xyz.bluspring.kilt.forgeinjects.world.damagesource;

import net.minecraft.world.damagesource.DamageScaling;
import net.neoforged.fml.common.asm.enumextension.ExtensionInfo;
import net.neoforged.fml.common.asm.enumextension.IExtensibleEnum;
import net.neoforged.neoforge.common.damagesource.IScalingFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.damagesource.DamageScalingInjection;

@Mixin(DamageScaling.class)
public abstract class DamageScalingInject implements IExtensibleEnum, DamageScalingInjection {
    @Unique
    private IScalingFunction scaling = IScalingFunction.DEFAULT;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$useDefaultScalingFunction(String string, int i, String id, CallbackInfo ci) {
        this.scaling = IScalingFunction.DEFAULT;
    }

    private DamageScalingInject(String name, int ordinal, String id) {}

    @CreateInitializer
    private DamageScalingInject(String name, int ordinal, String id, IScalingFunction scaling) {
        this(name, ordinal, id);
        this.scaling = scaling;
    }

    @Override
    public IScalingFunction getScalingFunction() {
        return this.scaling;
    }

    @CreateStatic
    private static ExtensionInfo getExtensionInfo() {
        return ExtensionInfo.nonExtended((Class) DamageScaling.class);
    }
}

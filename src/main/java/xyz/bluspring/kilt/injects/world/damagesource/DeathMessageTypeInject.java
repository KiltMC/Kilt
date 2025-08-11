package xyz.bluspring.kilt.injects.world.damagesource;

import net.minecraft.world.damagesource.DeathMessageType;
import net.neoforged.fml.common.asm.enumextension.ExtensionInfo;
import net.neoforged.fml.common.asm.enumextension.IExtensibleEnum;
import net.neoforged.neoforge.common.damagesource.IDeathMessageProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.damagesource.DeathMessageTypeInjection;

@Mixin(DeathMessageType.class)
public abstract class DeathMessageTypeInject implements IExtensibleEnum, DeathMessageTypeInjection {
    @Unique
    private IDeathMessageProvider msgFunction = IDeathMessageProvider.DEFAULT;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$initDefaultMessageFunction(String string, int i, String id, CallbackInfo ci) {
        this.msgFunction = IDeathMessageProvider.DEFAULT;
    }

    private DeathMessageTypeInject(String name, int ordinal, String id) {}

    @CreateInitializer
    private DeathMessageTypeInject(String name, int ordinal, String id, IDeathMessageProvider msgFunction) {
        this(name, ordinal, id);
        this.msgFunction = msgFunction;
    }

    @Override
    public IDeathMessageProvider getMessageFunction() {
        return this.msgFunction;
    }

    @CreateStatic
    private static ExtensionInfo getExtensionInfo() {
        return ExtensionInfo.nonExtended((Class) DeathMessageType.class);
    }
}

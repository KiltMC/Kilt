package xyz.bluspring.kilt.injects.world.damagesource;

import java.util.function.Supplier;

import net.neoforged.fml.common.asm.enumextension.ExtensionInfo;
import net.neoforged.fml.common.asm.enumextension.IExtensibleEnum;
import net.neoforged.fml.common.asm.enumextension.NamedEnum;
import net.neoforged.fml.common.asm.enumextension.NetworkedEnum;
import net.neoforged.fml.common.asm.enumextension.ReservedConstructor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageEffects;

@NamedEnum
@NetworkedEnum(NetworkedEnum.NetworkCheck.CLIENTBOUND)
@Mixin(DamageEffects.class)
public abstract class DamageEffectsInject implements IExtensibleEnum {
    @Shadow @Final @Mutable private SoundEvent sound;
    @Unique private Supplier<SoundEvent> soundSupplier;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$initSoundSupplier(String string, int i, String id, SoundEvent sound, CallbackInfo ci) {
        this.soundSupplier = () -> sound;
    }

    @ReservedConstructor
    DamageEffectsInject(String name, int ordinal, String id, SoundEvent sound) {}

    @CreateInitializer
    DamageEffectsInject(String name, int ordinal, String id, Supplier<SoundEvent> soundSupplier) {
        this(name, ordinal, id, (SoundEvent) null);
        this.soundSupplier = soundSupplier;
        this.sound = null;
    }

    @Inject(method = "sound", at = @At("HEAD"), cancellable = true)
    private void kilt$useNeoSoundSupplier(CallbackInfoReturnable<SoundEvent> cir) {
        if (this.sound == null)
            cir.setReturnValue(this.soundSupplier.get());
    }

    @CreateStatic
    private static ExtensionInfo getExtensionInfo() {
        return ExtensionInfo.nonExtended(DamageEffects.class);
    }
}

package xyz.bluspring.kilt.injects.world.damagesource;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageEffects;
import net.neoforged.fml.common.asm.enumextension.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;

import java.util.function.Supplier;

@NamedEnum
@NetworkedEnum(NetworkedEnum.NetworkCheck.CLIENTBOUND)
@Mixin(DamageEffects.class)
public abstract class DamageEffectsInject implements IExtensibleEnum {
    @Shadow @Final @Mutable private String id;
    @Shadow @Final @Mutable private SoundEvent sound;
    @Unique private Supplier<SoundEvent> soundSupplier;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$initSoundSupplier(String string, int i, String id, SoundEvent sound, CallbackInfo ci) {
        this.soundSupplier = () -> sound;
    }

    @ReservedConstructor
    private DamageEffectsInject(String name, int ordinal, String id, SoundEvent sound) {}

    @CreateInitializer
    private DamageEffectsInject(String name, int ordinal, String id, Supplier<SoundEvent> soundSupplier) {
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

// TRACKED HASH: b77266267a96ca4470ac07d61818079630ba823d
package xyz.bluspring.kilt.forgeinjects.world.item;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.RecordItem;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.world.item.RecordItemInjection;

import java.util.function.Supplier;

@Mixin(RecordItem.class)
public abstract class RecordItemInject extends Item implements RecordItemInjection {
    @Mutable @Shadow @Final private int analogOutput;
    @Mutable @Shadow @Final private SoundEvent sound;
    @Mutable @Shadow @Final private int lengthInTicks;

    @Unique private Supplier<SoundEvent> soundSupplier;

    public RecordItemInject(Properties properties) {
        super(properties);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$addDelegateSoundEvent(int analogOutput, SoundEvent sound, Properties properties, int lengthInSeconds, CallbackInfo ci) {
        this.soundSupplier = ForgeRegistries.SOUND_EVENTS.getDelegateOrThrow(sound);
    }

    @CreateInitializer
    public RecordItemInject(int analogOutput, Supplier<SoundEvent> soundSupplier, Properties properties, int lengthInSeconds) {
        super(properties);
        this.analogOutput = analogOutput;
        this.sound = null;
        this.soundSupplier = soundSupplier;
        this.lengthInTicks = lengthInSeconds;
    }

    @ModifyReturnValue(method = "getSound", at = @At("RETURN"))
    private SoundEvent kilt$useForgeDelegatedSound(SoundEvent original) {
        if (original == null) {
            return this.soundSupplier.get();
        }

        return original;
    }
}
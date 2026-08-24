package xyz.bluspring.kilt.mixin.neoforge.events;

import net.neoforged.neoforge.event.entity.player.PlayerSpawnPhantomsEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.neoforge.event.KiltModifiedEventCheckInjection;

@Mixin(PlayerSpawnPhantomsEvent.class)
public abstract class PlayerSpawnPhantomsEventMixin implements KiltModifiedEventCheckInjection {
    @Unique private boolean kilt$wasModified;

    @Override
    public boolean kilt$wasModified() {
        return this.kilt$wasModified;
    }

    @Inject(method = "set*", at = @At("TAIL"))
    private void kilt$markAsModified(CallbackInfo ci) {
        this.kilt$wasModified = true;
    }
}

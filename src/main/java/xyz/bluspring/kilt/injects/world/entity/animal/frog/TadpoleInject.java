package xyz.bluspring.kilt.injects.world.entity.animal.frog;

import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.level.Level;

@Mixin(Tadpole.class)
public abstract class TadpoleInject extends AbstractFish {
    public TadpoleInject(EntityType<? extends AbstractFish> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "ageUp()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;create(Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/entity/Entity;"), cancellable = true)
    private void kilt$checkCanConvert(CallbackInfo ci) {
        if (!EventHooks.canLivingConvert(this, EntityType.FROG, timer -> {}))
            ci.cancel();
    }

    @Inject(method = "ageUp()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/frog/Frog;moveTo(DDDFF)V"))
    private void kilt$callConvertEvent(CallbackInfo ci, @Local Frog frog) {
        EventHooks.onLivingConvert(this, frog);
    }
}

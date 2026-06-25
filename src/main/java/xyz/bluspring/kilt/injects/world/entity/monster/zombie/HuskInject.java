package xyz.bluspring.kilt.injects.world.entity.monster.zombie;

import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;

@Mixin(Husk.class)
public abstract class HuskInject extends Zombie {
    public HuskInject(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "doUnderWaterConversion", at = @At("HEAD"), cancellable = true)
    private void kilt$checkCanConvert(CallbackInfo ci) {
        if (!EventHooks.canLivingConvert(this, EntityType.ZOMBIE, timer -> this.conversionTime = timer))
            ci.cancel();
    }
}

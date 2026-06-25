package xyz.bluspring.kilt.injects.world.entity.monster.piglin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.level.Level;

@Mixin(AbstractPiglin.class)
public abstract class AbstractPiglinInject extends Monster {
    @Shadow protected int timeInOverworld;

    protected AbstractPiglinInject(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Definition(id = "timeInOverworld", field = "Lnet/minecraft/world/entity/monster/piglin/AbstractPiglin;timeInOverworld:I")
    @Expression("this.timeInOverworld > 300")
    @ModifyExpressionValue(method = "customServerAiStep", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkCanLivingConvert(boolean original) {
        return original && EventHooks.canLivingConvert(this, EntityType.ZOGLIN, timer -> this.timeInOverworld = timer);
    }

    @Inject(method = "finishConversion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/ZombifiedPiglin;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z", shift = At.Shift.AFTER))
    private void kilt$callLivingConvertEvent(ServerLevel serverLevel, CallbackInfo ci, @Local ZombifiedPiglin ziglin) {
        EventHooks.onLivingConvert(this, ziglin);
    }
}

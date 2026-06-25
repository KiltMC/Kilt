package xyz.bluspring.kilt.injects.world.entity.animal.pig;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.level.Level;

@Mixin(Pig.class)
public abstract class PigInject extends Animal {
    protected PigInject(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Definition(id = "level", local = @Local(type = ServerLevel.class, argsOnly = true))
    @Definition(id = "getDifficulty", method = "Lnet/minecraft/server/level/ServerLevel;getDifficulty()Lnet/minecraft/world/Difficulty;")
    @Definition(id = "PEACEFUL", field = "Lnet/minecraft/world/Difficulty;PEACEFUL:Lnet/minecraft/world/Difficulty;")
    @Expression("level.getDifficulty() != PEACEFUL")
    @ModifyExpressionValue(method = "thunderHit", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkCanConvertEvent(boolean original) {
        return original && EventHooks.canLivingConvert(this, EntityType.ZOMBIFIED_PIGLIN, timer -> {});
    }

    @Inject(method = "thunderHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/zombie/ZombifiedPiglin;setPersistenceRequired()V", shift = At.Shift.AFTER))
    private void kilt$callLivingConvertEvent(ServerLevel level, LightningBolt lightning, CallbackInfo ci, @Local ZombifiedPiglin ziglin) {
        EventHooks.onLivingConvert(this, ziglin);
    }
}

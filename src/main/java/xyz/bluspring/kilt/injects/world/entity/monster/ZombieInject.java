package xyz.bluspring.kilt.injects.world.entity.monster;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;

@Mixin(Zombie.class)
public abstract class ZombieInject extends Monster {
    @Shadow
    public int conversionTime;

    protected ZombieInject(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "doUnderWaterConversion", at = @At("HEAD"), cancellable = true)
    private void kilt$checkLivingConvert(CallbackInfo ci) {
        if (!EventHooks.canLivingConvert(this, EntityType.DROWNED, timer -> this.conversionTime = timer))
            ci.cancel();
    }

    @WrapOperation(method = "convertToZombieType", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Zombie;setCanBreakDoors(Z)V")) // only way i'm able to do this
    private void kilt$callLivingConvert(Zombie instance, boolean canBreakDoors, Operation<Void> original) {
        original.call(instance, canBreakDoors);
        EventHooks.onLivingConvert(this, instance);
    }

    @Definition(id = "entity", local = @Local(type = LivingEntity.class, argsOnly = true))
    @Definition(id = "Villager", type = Villager.class)
    @Expression("entity instanceof Villager")
    @ModifyExpressionValue(method = "killedEntity", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkCanLivingConvertVillager(boolean original, @Local(argsOnly = true) LivingEntity entity) {
        return original && EventHooks.canLivingConvert(entity, EntityType.ZOMBIE_VILLAGER, timer -> {});
    }

    @Inject(method = "killedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/ZombieVillager;setVillagerXp(I)V", shift = At.Shift.AFTER))
    private void kilt$callLivingConvertVillager(ServerLevel level, LivingEntity entity, CallbackInfoReturnable<Boolean> cir, @Local ZombieVillager zombieVillager) {
        EventHooks.onLivingConvert(entity, zombieVillager);
    }
}

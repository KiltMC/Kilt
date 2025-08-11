package xyz.bluspring.kilt.injects.world.entity.monster;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.ZombieEvent;
import net.neoforged.bus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zombie.class)
public abstract class ZombieInject extends Monster {
    @Shadow private int conversionTime;

    protected ZombieInject(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Definition(id = "conversionTime", field = "Lnet/minecraft/world/entity/monster/Zombie;conversionTime:I")
    @Expression("this.conversionTime < 0")
    @ModifyExpressionValue(method = "tick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkCanLivingConvert(boolean original) {
        return original && EventHooks.canLivingConvert(this, EntityType.DROWNED, timer -> this.conversionTime = timer);
    }

    // Kilt: this is a WrapOperation because mixin @Inject couldn't detect the local properly
    @WrapOperation(method = "convertToZombieType", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Zombie;setCanBreakDoors(Z)V"))
    private void kilt$callLivingConvertEvent(Zombie instance, boolean canBreakDoors, Operation<Void> original) {
        original.call(instance, canBreakDoors);
        EventHooks.onLivingConvert(this, instance);
    }

    @Definition(id = "livingEntity", local = @Local(type = LivingEntity.class))
    @Expression("livingEntity != null")
    @ModifyExpressionValue(method = "hurt", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$handleZombieSummonAidEvent(boolean original, @Share("event") LocalRef<ZombieEvent.SummonAidEvent> eventRef, @Local LivingEntity entity, @Cancellable CallbackInfoReturnable<Boolean> cir) {
        var event = EventHooks.fireZombieSummonAid((Zombie) (Object) this, this.level(), Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()), entity, this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).getValue());
        eventRef.set(event);

        if (event.getResult() == Event.Result.DENY) {
            cir.setReturnValue(true);
            return false;
        }

        return event.getResult() == Event.Result.ALLOW || original;
    }

    @WrapOperation(method = "hurt", at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/entity/monster/Zombie;"))
    private Zombie kilt$useForgeSummonedAidZombie(Level level, Operation<Zombie> original, @Share("event") LocalRef<ZombieEvent.SummonAidEvent> eventRef) {
        var event = eventRef.get();
        return event.getCustomSummonedAid() != null && event.getResult() == Event.Result.ALLOW ? event.getCustomSummonedAid() : original.call(level);
    }

    @WrapWithCondition(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Zombie;setTarget(Lnet/minecraft/world/entity/LivingEntity;)V"))
    private boolean kilt$checkEntityExists(Zombie instance, LivingEntity entity) {
        return entity != null;
    }

    @Definition(id = "entity", local = @Local(type = LivingEntity.class, argsOnly = true))
    @Definition(id = "Villager", type = Villager.class)
    @Expression("entity instanceof Villager")
    @ModifyExpressionValue(method = "killedEntity", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkCanConvertToZombieVillager(boolean original, @Local(argsOnly = true) LivingEntity entity) {
        return original && EventHooks.canLivingConvert(entity, EntityType.ZOMBIE_VILLAGER, $ -> {});
    }

    @Inject(method = "killedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/ZombieVillager;setVillagerXp(I)V", shift = At.Shift.AFTER))
    private void kilt$callLivingConvertEvent(ServerLevel level, LivingEntity entity, CallbackInfoReturnable<Boolean> cir, @Local ZombieVillager zombieVillager) {
        EventHooks.onLivingConvert(entity, zombieVillager);
    }

    // Kilt: I feel like some other mod should handle these chance values...
}

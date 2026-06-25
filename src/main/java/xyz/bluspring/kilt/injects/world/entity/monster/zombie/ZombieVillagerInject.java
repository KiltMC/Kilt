package xyz.bluspring.kilt.injects.world.entity.monster.zombie;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.Level;

@Mixin(ZombieVillager.class)
public abstract class ZombieVillagerInject extends Zombie {
    @Shadow private int villagerConversionTime;

    public ZombieVillagerInject(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    @Definition(id = "villagerConversionTime", field = "Lnet/minecraft/world/entity/monster/zombie/ZombieVillager;villagerConversionTime:I")
    @Expression("this.villagerConversionTime <= 0")
    @ModifyExpressionValue(method = "tick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkCanConvertToZombieVillager(boolean original) {
        return original && EventHooks.canLivingConvert(this, EntityType.VILLAGER, timer -> this.villagerConversionTime = timer);
    }

    @Inject(method = "lambda$finishConversion$0", at = @At("TAIL"))
    private void kilt$callLivingConvertEvent(ServerLevel level, Villager villager, CallbackInfo ci) {
        EventHooks.onLivingConvert(this, villager);
    }
}

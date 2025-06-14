package xyz.bluspring.kilt.forgeinjects.world.entity.monster;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ZombieVillager.class)
public abstract class ZombieVillagerInject extends Zombie {
    @Shadow private int villagerConversionTime;

    public ZombieVillagerInject(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    @Definition(id = "villagerConversionTime", field = "Lnet/minecraft/world/entity/monster/ZombieVillager;villagerConversionTime:I")
    @Expression("this.villagerConversionTime <= 0")
    @ModifyExpressionValue(method = "tick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkCanConvertToZombieVillager(boolean original) {
        return original && ForgeEventFactory.canLivingConvert(this, EntityType.VILLAGER, timer -> this.villagerConversionTime = timer);
    }

    @Inject(method = "finishConversion", at = @At("TAIL"))
    private void kilt$callLivingConvertEvent(ServerLevel serverLevel, CallbackInfo ci, @Local Villager villager) {
        ForgeEventFactory.onLivingConvert(this, villager);
    }
}

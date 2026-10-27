package xyz.bluspring.kilt.injects.world.entity.monster.piglin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
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

    @WrapOperation(method = "finishConversion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/piglin/AbstractPiglin;convertTo(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/ConversionParams;Lnet/minecraft/world/entity/ConversionParams$AfterConversion;)Lnet/minecraft/world/entity/Mob;"))
    private Mob kilt$callLivingConvertEvent(AbstractPiglin instance, EntityType<ZombifiedPiglin> entityType, ConversionParams conversionParams, ConversionParams.AfterConversion<ZombifiedPiglin> afterConversion, Operation<Mob> original) {
        return original.call(instance, entityType, conversionParams, (ConversionParams.AfterConversion<ZombifiedPiglin>) ziglin -> {
            afterConversion.finalizeConversion(ziglin);
            EventHooks.onLivingConvert(instance, ziglin);
        });
    }
}

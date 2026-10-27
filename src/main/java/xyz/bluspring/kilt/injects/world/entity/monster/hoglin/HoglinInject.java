package xyz.bluspring.kilt.injects.world.entity.monster.hoglin;

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
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.level.Level;

@Mixin(Hoglin.class)
public abstract class HoglinInject extends Animal {
    @Shadow private int timeInOverworld;

    protected HoglinInject(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Definition(id = "timeInOverworld", field = "Lnet/minecraft/world/entity/monster/hoglin/Hoglin;timeInOverworld:I")
    @Expression("this.timeInOverworld > 300")
    @ModifyExpressionValue(method = "customServerAiStep", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkCanLivingConvert(boolean original) {
        return original && EventHooks.canLivingConvert(this, EntityType.ZOGLIN, timer -> this.timeInOverworld = timer);
    }

    @WrapOperation(method = "finishConversion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/hoglin/Hoglin;convertTo(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/ConversionParams;Lnet/minecraft/world/entity/ConversionParams$AfterConversion;)Lnet/minecraft/world/entity/Mob;"))
    private Mob kilt$callLivingConvertEvent(Hoglin instance, EntityType<Zoglin> entityType, ConversionParams conversionParams, ConversionParams.AfterConversion<Zoglin> afterConversion, Operation<Mob> original) {
        return original.call(instance, entityType, conversionParams, (ConversionParams.AfterConversion<Zoglin>) zoglin -> {
            afterConversion.finalizeConversion(zoglin);
            EventHooks.onLivingConvert(instance, zoglin);
        });
    }
}

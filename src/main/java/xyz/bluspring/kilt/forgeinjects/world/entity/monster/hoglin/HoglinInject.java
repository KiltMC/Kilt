package xyz.bluspring.kilt.forgeinjects.world.entity.monster.hoglin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
        return original && ForgeEventFactory.canLivingConvert(this, EntityType.ZOGLIN, timer -> this.timeInOverworld = timer);
    }

    @Inject(method = "finishConversion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Zoglin;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z", shift = At.Shift.AFTER))
    private void kilt$callLivingConvertEvent(ServerLevel serverLevel, CallbackInfo ci, @Local Zoglin zoglin) {
        ForgeEventFactory.onLivingConvert(this, zoglin);
    }
}

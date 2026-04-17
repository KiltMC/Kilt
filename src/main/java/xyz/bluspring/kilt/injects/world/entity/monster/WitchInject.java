package xyz.bluspring.kilt.injects.world.entity.monster;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;

@Mixin(Witch.class)
public abstract class WitchInject extends Raider {
    protected WitchInject(EntityType<? extends Raider> entityType, Level level) {
        super(entityType, level);
    }

    @Definition(id = "damageAmount", local = @Local(type = float.class, argsOnly = true))
    @Expression("damageAmount = 0.0")
    @Inject(method = "getDamageAfterMagicAbsorb", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void kilt$addReductionToContainer(DamageSource damageSource, float damageAmount, CallbackInfoReturnable<Float> cir) {
        this.kilt$getDamageContainers().peek().setReduction(DamageContainer.Reduction.INNATE_RESISTANCE, damageAmount);
    }
}

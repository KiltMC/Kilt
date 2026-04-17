package xyz.bluspring.kilt.injects.world.entity.monster;

import java.util.function.BooleanSupplier;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

@Mixin(MagmaCube.class)
public abstract class MagmaCubeInject extends Slime {
    public MagmaCubeInject(EntityType<? extends Slime> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "jumpFromGround", at = @At("TAIL"))
    private void kilt$callJumpEvent(CallbackInfo ci) {
        CommonHooks.onLivingJump(this);
    }

    @Unique private BooleanSupplier kilt$isLava;
    @Unique private Runnable kilt$onSuper;

    @Unique
    private void jumpInLiquidInternal(BooleanSupplier isLava, Runnable onSuper) {
        this.kilt$isLava = isLava;
        this.kilt$onSuper = onSuper;

        this.jumpInLiquid(FluidTags.LAVA);

        this.kilt$isLava = null;
        this.kilt$onSuper = null;
    }

    @Definition(id = "fluidTag", local = @Local(type = TagKey.class, argsOnly = true))
    @Definition(id = "LAVA", field = "Lnet/minecraft/tags/FluidTags;LAVA:Lnet/minecraft/tags/TagKey;")
    @Expression("fluidTag == LAVA")
    @ModifyExpressionValue(method = "jumpInLiquid", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkIsLava(boolean original) {
        if (kilt$isLava != null)
            return kilt$isLava.getAsBoolean();

        return original;
    }

    @WrapOperation(method = "jumpInLiquid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Slime;jumpInLiquid(Lnet/minecraft/tags/TagKey;)V"))
    private void kilt$tryCallSuper(MagmaCube instance, TagKey<Fluid> tagKey, Operation<Void> original) {
        if (kilt$onSuper != null)
            kilt$onSuper.run();
        else
            original.call(instance, tagKey);
    }

    @Override
    public void jumpInFluid(FluidType type) {
        this.jumpInLiquidInternal(() -> type == NeoForgeMod.LAVA_TYPE.value(), () -> super.jumpInFluid(type));
    }
}

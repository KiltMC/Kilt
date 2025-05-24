package xyz.bluspring.kilt.forgeinjects.world.entity;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbInject extends Entity {
    @Shadow protected abstract BlockPos getBlockPosBelowThatAffectsMyMovement();

    public ExperienceOrbInject(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getFriction()F"))
    private float kilt$useForgeGetFriction(Block instance, Operation<Float> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), Block.class, "getFriction", BlockState.class, LevelReader.class, BlockPos.class, Entity.class)) {
            var pos = this.getBlockPosBelowThatAffectsMyMovement();
            return this.level().getBlockState(pos).getFriction(this.level(), pos, this);
        }

        return original.call(instance);
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void kilt$fixMc53850(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (this.level().isClientSide() || this.isRemoved())
            cir.setReturnValue(false);
    }

    @Definition(id = "player", local = @Local(type = Player.class, argsOnly = true))
    @Definition(id = "takeXpDelay", field = "Lnet/minecraft/world/entity/player/Player;takeXpDelay:I")
    @Expression("player.takeXpDelay = ?")
    @Inject(method = "playerTouch", at = @At("MIXINEXTRAS:EXPRESSION"), cancellable = true)
    private void kilt$callPickupExpEvent(Player player, CallbackInfo ci) {
        if (MinecraftForge.EVENT_BUS.post(new PlayerXpEvent.PickupXp(player, (ExperienceOrb) (Object) this)))
            ci.cancel();
    }

    @Redirect(method = "repairPlayerItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ExperienceOrb;xpToDurability(I)I"))
    private int kilt$tryUseXpRepairRatio(ExperienceOrb instance, int xp, @Local ItemStack stack) {
        return (int) (xp * stack.getXpRepairRatio()); // TODO: how do we make this more mod-compatible
    }
}

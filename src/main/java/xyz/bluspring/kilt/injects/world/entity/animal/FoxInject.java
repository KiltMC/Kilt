package xyz.bluspring.kilt.injects.world.entity.animal;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Fox.class)
public abstract class FoxInject extends Animal {
    protected FoxInject(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    // TODO: How do we go about the dropEquipment patch?

    @Mixin(targets = "net.minecraft.world.entity.animal.Fox$FoxBreedGoal")
    public abstract static class FoxBreedGoalInject extends BreedGoal {
        public FoxBreedGoalInject(Animal animal, double speedModifier) {
            super(animal, speedModifier);
        }

        @Definition(id = "fox", local = @Local(type = Fox.class))
        @Expression("fox != null")
        @Inject(method = "breed", at = @At("MIXINEXTRAS:EXPRESSION"), cancellable = true)
        private void kilt$handleBabySpawnEvent(CallbackInfo ci, @Local LocalRef<Fox> child) {
            var event = new BabyEntitySpawnEvent(this.animal, this.partner, child.get());
            var cancelled = NeoForge.EVENT_BUS.post(event).isCanceled();
            child.set((Fox) event.getChild());

            if (cancelled) {
                this.animal.setAge(6000);
                this.partner.setAge(6000);
                this.animal.resetLove();
                this.partner.resetLove();

                ci.cancel();
            }
        }
    }

    @Mixin(Fox.FoxEatBerriesGoal.class)
    public abstract static class FoxEatBerriesGoalInject extends MoveToBlockGoal {
        @Shadow @Final Fox field_17975;

        public FoxEatBerriesGoalInject(PathfinderMob mob, double speedModifier, int searchRange) {
            super(mob, speedModifier, searchRange);
        }

        @ModifyExpressionValue(method = "onReachedTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
        private boolean kilt$checkMobGriefingEvent(boolean original) {
            return original && EventHooks.canEntityGrief(field_17975.level(), field_17975);
        }
    }

    @Mixin(targets = "net.minecraft.world.entity.animal.Fox$FoxFloatGoal")
    public abstract static class FoxFloatGoalInject extends FloatGoal {
        @Shadow @Final private Fox field_17976;

        public FoxFloatGoalInject(Mob mob) {
            super(mob);
        }

        @ModifyExpressionValue(method = "canUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Fox;isInLava()Z"))
        private boolean kilt$checkIsInFluidType(boolean original) {
            var fox = field_17976;
            return original || fox.isInFluidType((fluidType, height) -> fox.canSwimInFluidType(fluidType) && height > 0.25);
        }
    }
}

package xyz.bluspring.kilt.injects.world.entity.animal.fox;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.level.Level;

@Mixin(Fox.class)
public abstract class FoxInject extends Animal {
    protected FoxInject(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    // TODO: How do we go about the dropEquipment patch?

    @Mixin(targets = "net.minecraft.world.entity.animal.fox.Fox$FoxBreedGoal")
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
        @Shadow @Final Fox this$0;

        public FoxEatBerriesGoalInject(PathfinderMob mob, double speedModifier, int searchRange) {
            super(mob, speedModifier, searchRange);
        }

        @Definition(id = "Boolean", type = Boolean.class)
        @Definition(id = "get", method = "Lnet/minecraft/world/level/gamerules/GameRules;get(Lnet/minecraft/world/level/gamerules/GameRule;)Ljava/lang/Object;")
        @Definition(id = "MOB_GRIEFING", field = "Lnet/minecraft/world/level/gamerules/GameRules;MOB_GRIEFING:Lnet/minecraft/world/level/gamerules/GameRule;")
        @Expression("(Boolean) ?.get(MOB_GRIEFING)")
        @ModifyExpressionValue(method = "onReachedTarget", at = @At("MIXINEXTRAS:EXPRESSION"))
        private Boolean kilt$checkMobGriefingEvent(Boolean original) {
            return original && EventHooks.canEntityGrief((ServerLevel) this$0.level(), this$0);
        }
    }
}

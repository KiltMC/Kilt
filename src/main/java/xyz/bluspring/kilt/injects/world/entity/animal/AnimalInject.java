package xyz.bluspring.kilt.injects.world.entity.animal;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Animal.class)
public abstract class AnimalInject extends AgeableMob {
    @Shadow public abstract void resetLove();

    protected AnimalInject(EntityType<? extends AgeableMob> entityType, Level level) {
        super(entityType, level);
    }

    @Definition(id = "getBreedOffspring", method = "Lnet/minecraft/world/entity/animal/Animal;getBreedOffspring(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/AgeableMob;)Lnet/minecraft/world/entity/AgeableMob;")
    @Expression("? = ?.getBreedOffspring(?, ?)")
    @Inject(method = "spawnChildFromBreeding", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER), cancellable = true)
    private void kilt$handleBabySpawnEvent(ServerLevel level, Animal mate, CallbackInfo ci, @Local LocalRef<AgeableMob> child) {
        var event = new BabyEntitySpawnEvent(this, mate, child.get());
        var cancelled = NeoForge.EVENT_BUS.post(event).isCanceled();
        child.set(event.getChild());

        if (cancelled) {
            this.setAge(6000);
            mate.setAge(6000);
            this.resetLove();
            mate.resetLove();

            ci.cancel();
        }
    }
}

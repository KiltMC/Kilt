package xyz.bluspring.kilt.injects.world.entity.animal;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Ocelot.class)
public abstract class OcelotInject extends Animal {
    protected OcelotInject(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Definition(id = "random", field = "Lnet/minecraft/world/entity/animal/Ocelot;random:Lnet/minecraft/util/RandomSource;")
    @Definition(id = "nextInt", method = "Lnet/minecraft/util/RandomSource;nextInt(I)I")
    @Expression("this.random.nextInt(?) == ?")
    @ModifyExpressionValue(method = "mobInteract", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkAnimalTameEvent(boolean original, @Local(argsOnly = true) Player player) {
        return original && !EventHooks.onAnimalTame(this, player);
    }
}

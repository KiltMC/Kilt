package xyz.bluspring.kilt.injects.world.entity.monster;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Shulker.class)
public abstract class ShulkerInject extends AbstractGolem {
    protected ShulkerInject(EntityType<? extends AbstractGolem> entityType, Level level) {
        super(entityType, level);
    }

    @Definition(id = "direction", local = @Local(type = Direction.class))
    @Expression("direction != null")
    @ModifyExpressionValue(method = "teleportSomewhere", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$tryEnderTeleportEvent(boolean original, @Local(ordinal = 1) LocalRef<BlockPos> pos1Ref) {
        if (original) {
            var pos1 = pos1Ref.get();
            var event = EventHooks.onEnderTeleport(this, pos1.getX(), pos1.getY(), pos1.getZ());

            pos1Ref.set(BlockPos.containing(event.getTargetX(), event.getTargetY(), event.getTargetZ()));

            if (event.isCanceled())
                return false;
        }

        return original;
    }
}

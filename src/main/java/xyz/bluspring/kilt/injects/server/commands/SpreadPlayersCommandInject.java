package xyz.bluspring.kilt.injects.server.commands;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.commands.SpreadPlayersCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;

@Mixin(SpreadPlayersCommand.class)
public abstract class SpreadPlayersCommandInject {
    @WrapOperation(method = "setPlayerPositions", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FF)Z"))
    private static boolean kilt$callSpreadPlayersEvent(Entity instance, ServerLevel level, double x, double y, double z, Set<RelativeMovement> relativeMovements, float yRot, float xRot, Operation<Boolean> original) {
        var event = EventHooks.onEntityTeleportSpreadPlayersCommand(instance, x, y, z);

        if (!event.isCanceled())
            return original.call(instance, level, event.getTargetX(), event.getTargetY(), event.getTargetZ(), relativeMovements, yRot, xRot);

        return false;
    }
}

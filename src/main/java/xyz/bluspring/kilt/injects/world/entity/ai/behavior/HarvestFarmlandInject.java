package xyz.bluspring.kilt.injects.world.entity.ai.behavior;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.HarvestFarmland;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.IPlantable;
import net.neoforged.neoforge.common.PlantType;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(HarvestFarmland.class)
public abstract class HarvestFarmlandInject {
    @Shadow private @Nullable BlockPos aboveFarmlandPos;

    @ModifyExpressionValue(method = "checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
    private boolean kilt$checkCanMobsGrief(boolean original, @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) Villager villager) {
        return original || EventHooks.getMobGriefingEvent(level, villager);
    }

    @ModifyVariable(method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;J)V", at = @At("LOAD"))
    private boolean kilt$checkIsPlantable(boolean original, @Local ItemStack stack, @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) Villager villager) {
        if (!original) {
            if (stack.getItem() instanceof IPlantable plantable) {
                if (plantable.getPlantType(level, aboveFarmlandPos) == PlantType.CROP) {
                    level.setBlock(aboveFarmlandPos, plantable.getPlant(level, aboveFarmlandPos), 3);
                    return true;
                }
            }
        }

        return original;
    }
}

package xyz.bluspring.kilt.injects.world.entity.ai.behavior;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.HarvestFarmland;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmBlock;
import net.neoforged.neoforge.common.SpecialPlantable;
import net.neoforged.neoforge.common.Tags;
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
        return original || EventHooks.canEntityGrief(level, villager);
    }

    @Definition(id = "block", local = @Local(type = Block.class, ordinal = 1))
    @Definition(id = "FarmBlock", type = FarmBlock.class)
    @Expression("block instanceof FarmBlock")
    @ModifyExpressionValue(method = "validPos", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkIsVillagerFarmland(boolean original, @Local(ordinal = 1) Block block) {
        return original || block.builtInRegistryHolder().is(Tags.Blocks.VILLAGER_FARMLANDS);
    }

    @Definition(id = "block", local = @Local(type = Block.class, ordinal = 1))
    @Definition(id = "FarmBlock", type = FarmBlock.class)
    @Expression("block instanceof FarmBlock")
    @ModifyExpressionValue(method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;J)V", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkIsVillagerFarmland2(boolean original, @Local(ordinal = 1) Block block) {
        return original || block.builtInRegistryHolder().is(Tags.Blocks.VILLAGER_FARMLANDS);
    }

    @ModifyVariable(method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;J)V", at = @At("LOAD"))
    private boolean kilt$checkIsPlantable(boolean original, @Local ItemStack stack, @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) Villager villager) {
        if (!original) {
            if (stack.getItem() instanceof SpecialPlantable plantable) {
                if (plantable.villagerCanPlantItem(villager)) {
                    plantable.spawnPlantAtPosition(stack, level, aboveFarmlandPos, Direction.DOWN);
                    return true;
                }
            }
        }

        return original;
    }
}

package xyz.bluspring.kilt.injects.world.entity.ai.behavior;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.common.SpecialPlantable;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.HarvestFarmland;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmlandBlock;

@Mixin(HarvestFarmland.class)
public abstract class HarvestFarmlandInject {
    @Shadow private @Nullable BlockPos aboveFarmlandPos;

    @Definition(id = "get", method = "Lnet/minecraft/world/level/gamerules/GameRules;get(Lnet/minecraft/world/level/gamerules/GameRule;)Ljava/lang/Object;")
    @Definition(id = "MOB_GRIEFING", field = "Lnet/minecraft/world/level/gamerules/GameRules;MOB_GRIEFING:Lnet/minecraft/world/level/gamerules/GameRule;")
    @Definition(id = "Boolean", type = Boolean.class)
    @Expression("(Boolean) ?.get(MOB_GRIEFING)")
    @ModifyExpressionValue(method = "checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/villager/Villager;)Z", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Boolean kilt$checkCanMobsGrief(Boolean original, @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) Villager villager) {
        return original || EventHooks.canEntityGrief(level, villager);
    }

    @Definition(id = "block", local = @Local(type = Block.class, ordinal = 1))
    @Definition(id = "FarmBlock", type = FarmlandBlock.class)
    @Expression("block instanceof FarmBlock")
    @ModifyExpressionValue(method = "validPos", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkIsVillagerFarmland(boolean original, @Local(ordinal = 1) Block block) {
        return original || block.builtInRegistryHolder().is(Tags.Blocks.VILLAGER_FARMLANDS);
    }

    @Definition(id = "block", local = @Local(type = Block.class, ordinal = 1))
    @Definition(id = "FarmBlock", type = FarmlandBlock.class)
    @Expression("block instanceof FarmBlock")
    @ModifyExpressionValue(method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/villager/Villager;J)V", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkIsVillagerFarmland2(boolean original, @Local(ordinal = 1) Block block) {
        return original || block.builtInRegistryHolder().is(Tags.Blocks.VILLAGER_FARMLANDS);
    }

    @ModifyVariable(method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/villager/Villager;J)V", at = @At("LOAD"))
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

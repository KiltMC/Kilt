package xyz.bluspring.kilt.injects.world.entity.monster.piglin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(Piglin.class)
public abstract class PiglinInject extends AbstractPiglin {
    public PiglinInject(EntityType<? extends AbstractPiglin> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(method = "getArmPose", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/piglin/Piglin;isHolding(Lnet/minecraft/world/item/Item;)Z"))
    private boolean kilt$checkIsHoldingCrossbow(Piglin instance, Item item, Operation<Boolean> original) {
        return original.call(instance, item) || instance.isHolding(is -> is.getItem() instanceof CrossbowItem);
    }

    @Definition(id = "itemStack", local = @Local(type = ItemStack.class, name = "itemStack", argsOnly = true))
    @Definition(id = "is", method = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z")
    @Definition(id = "BARTERING_ITEM", field = "Lnet/minecraft/world/entity/monster/piglin/PiglinAi;BARTERING_ITEM:Lnet/minecraft/world/item/Item;")
    @Expression("itemStack.is(BARTERING_ITEM)")
    @ModifyExpressionValue(method = "holdInOffHand", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkIsPiglinCurrency(boolean original, @Local(argsOnly = true) ItemStack stack) {
        return original || stack.isPiglinCurrency();
    }

    @Definition(id = "Boolean", type = Boolean.class)
    @Definition(id = "get", method = "Lnet/minecraft/world/level/gamerules/GameRules;get(Lnet/minecraft/world/level/gamerules/GameRule;)Ljava/lang/Object;")
    @Definition(id = "MOB_GRIEFING", field = "Lnet/minecraft/world/level/gamerules/GameRules;MOB_GRIEFING:Lnet/minecraft/world/level/gamerules/GameRule;")
    @Expression("(Boolean) ?.get(MOB_GRIEFING)")
    @ModifyExpressionValue(method = "wantsToPickUp", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Boolean kilt$checkMobGriefingEvent(Boolean original, @Local(argsOnly = true) ServerLevel level) {
        return original && EventHooks.canEntityGrief(level, this);
    }
}

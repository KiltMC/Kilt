package xyz.bluspring.kilt.forgeinjects.world.entity.monster;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Pillager.class)
public abstract class PillagerInject extends AbstractIllager {
    protected PillagerInject(EntityType<? extends AbstractIllager> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(method = "getArmPose", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Pillager;isHolding(Lnet/minecraft/world/item/Item;)Z"))
    private boolean kilt$checkIsHoldingCrossbow(Pillager instance, Item item, Operation<Boolean> original) {
        return original.call(instance, item) || instance.isHolding(is -> is.getItem() instanceof CrossbowItem);
    }
}

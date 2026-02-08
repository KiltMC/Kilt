package xyz.bluspring.kilt.injects.world.entity.animal;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(Wolf.class)
public abstract class WolfInject extends TamableAnimal {
    protected WolfInject(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Definition(id = "itemStack", local = @Local(type = ItemStack.class))
    @Definition(id = "get", method = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;")
    @Definition(id = "FOOD", field = "Lnet/minecraft/core/component/DataComponents;FOOD:Lnet/minecraft/core/component/DataComponentType;")
    @Expression("itemStack.get(FOOD)")
    @WrapOperation(method = "mobInteract", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Object kilt$tryUseForgeFoodProperties(ItemStack instance, DataComponentType dataComponentType, Operation<Object> original, @Local ItemStack stack) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), Item.class, "getFoodProperties", ItemStack.class, LivingEntity.class)) {
            return instance.getFoodProperties(this);
        }

        return original.call(instance, dataComponentType);
    }

    // Kilt: I don't think we need to implement the game event?


    @Definition(id = "itemStack", local = @Local(type = ItemStack.class))
    @Definition(id = "is", method = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z")
    @Definition(id = "SHEARS", field = "Lnet/minecraft/world/item/Items;SHEARS:Lnet/minecraft/world/item/Item;")
    @Expression("itemStack.is(SHEARS)")
    @WrapOperation(method = "mobInteract", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkCanRemoveArmor(ItemStack instance, Item item, Operation<Boolean> original) {
        return original.call(instance, item) || instance.canPerformAction(ItemAbilities.SHEARS_REMOVE_ARMOR);
    }

    @Definition(id = "random", field = "Lnet/minecraft/world/entity/animal/Wolf;random:Lnet/minecraft/util/RandomSource;")
    @Definition(id = "nextInt", method = "Lnet/minecraft/util/RandomSource;nextInt(I)I")
    @Expression("this.random.nextInt(?) == ?")
    @ModifyExpressionValue(method = "tryToTame", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkAnimalTameEvent(boolean original, @Local(argsOnly = true) Player player) {
        return original && !EventHooks.onAnimalTame(this, player);
    }
}

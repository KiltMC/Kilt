package xyz.bluspring.kilt.forgeinjects.world.item;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.world.item.SpawnEggItemInjection;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(SpawnEggItem.class)
public class SpawnEggItemInject implements SpawnEggItemInjection {
    @Shadow @Final private EntityType<?> defaultType;

    @WrapOperation(method = "getType", at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/SpawnEggItem;defaultType:Lnet/minecraft/world/entity/EntityType;", opcode = 0))
    private EntityType<?> kilt$useForgeDefaultType(SpawnEggItem instance, Operation<EntityType<?>> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), SpawnEggItem.class, "getDefaultType"))
            return this.getDefaultType();

        return original.call(instance);
    }

    @ModifyReturnValue(method = "getType", at = @At("RETURN"))
    private EntityType<?> kilt$returnForgeDefaultType(EntityType<?> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), SpawnEggItem.class, "getDefaultType"))
            return this.getDefaultType();

        return original;
    }

    @ModifyReceiver(method = "requiredFeatures", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;requiredFeatures()Lnet/minecraft/world/flag/FeatureFlagSet;"))
    private EntityType<?> kilt$useForgeDefaultTypeForFeatures(EntityType<?> instance) {
        if (KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), SpawnEggItem.class, "getDefaultType"))
            return this.getDefaultType();

        return instance;
    }

    @Override
    public EntityType<?> getDefaultType() {
        return this.defaultType;
    }
}
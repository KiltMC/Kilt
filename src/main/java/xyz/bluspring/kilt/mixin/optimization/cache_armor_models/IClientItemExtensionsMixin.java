package xyz.bluspring.kilt.mixin.optimization.cache_armor_models;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.CacheOptimizationStorage;
import xyz.bluspring.kilt.util.KiltHelper;
import xyz.bluspring.kilt.util.Quadruple;

import java.util.concurrent.ExecutionException;

@Mixin(IClientItemExtensions.class)
public interface IClientItemExtensionsMixin {
    // Incredibly risky optimization that caches armor models to avoid heavy performance degradation under many MCreator mods that override this method.
    // MCreator mods such as Apocalypse Now override this method and allocate a new HumanoidModel every single frame, which results in heavy performance degradation and increased memory usage.
    // This mixin is not mainlined into source due to how risky this optimization is, and may need to be disabled if any issues arise.
    // TODO: If this causes any problems later down the line, disable this mixin.
    @WrapOperation(method = "getGenericArmorModel", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/extensions/common/IClientItemExtensions;getHumanoidArmorModel(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/client/model/HumanoidModel;)Lnet/minecraft/client/model/HumanoidModel;"))
    private @NotNull HumanoidModel<?> kilt$opt$cacheArmorModels(IClientItemExtensions instance, LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> originalModel, Operation<HumanoidModel<?>> original) throws ExecutionException {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), IClientItemExtensions.class, "getHumanoidArmorModel", LivingEntity.class, ItemStack.class, EquipmentSlot.class, HumanoidModel.class)) {
            // Quadruple creates a hash code based on the hash code values of A, B and C, and only actually stores references, so we can use this as a key.
            var key = new Quadruple<>((IClientItemExtensions) this, livingEntity, itemStack, equipmentSlot);
            return CacheOptimizationStorage.ARMOR_MODEL_CACHE.get(key, () -> original.call(instance, livingEntity, itemStack, equipmentSlot, originalModel));
        }

        return original.call(instance, livingEntity, itemStack, equipmentSlot, originalModel);
    }
}

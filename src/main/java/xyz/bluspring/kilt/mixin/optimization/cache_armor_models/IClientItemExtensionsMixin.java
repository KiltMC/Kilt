package xyz.bluspring.kilt.mixin.optimization.cache_armor_models;

//import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
//import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//import net.minecraft.client.model.HumanoidModel;
//import net.minecraft.world.entity.EquipmentSlot;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
//import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import xyz.bluspring.kilt.helpers.CacheOptimizationStorage;
//import xyz.bluspring.kilt.util.KiltHelper;
//import xyz.bluspring.kilt.util.Quadruple;
//
//import java.util.concurrent.ExecutionException;

@Mixin(IClientItemExtensions.class)
public interface IClientItemExtensionsMixin {
    // Incredibly risky optimization that caches armor models to avoid heavy performance degradation under many MCreator mods that override this method.
    // MCreator mods such as Apocalypse Now override this method and allocate a new HumanoidModel every single frame, which results in heavy performance degradation and increased memory usage.
    // This mixin is not mainlined into source due to how risky this optimization is, and may need to be disabled if any issues arise.
    // If this causes any problems later down the line, disable this mixin. // UPDATE: it caused problems.
    /*@WrapOperation(method = "getGenericArmorModel", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/extensions/common/IClientItemExtensions;getHumanoidArmorModel(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/client/model/HumanoidModel;)Lnet/minecraft/client/model/HumanoidModel;"))
    private @NotNull HumanoidModel<?> kilt$opt$cacheArmorModels(IClientItemExtensions instance, LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> originalModel, Operation<HumanoidModel<?>> original) throws ExecutionException {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), IClientItemExtensions.class, "getHumanoidArmorModel", LivingEntity.class, ItemStack.class, EquipmentSlot.class, HumanoidModel.class)) {
            // Quadruple creates a hash code based on the hash code values of A, B and C, and only actually stores references, so we can use this as a key.
            var key = new Quadruple<>((IClientItemExtensions) this, livingEntity, itemStack, equipmentSlot);

            // Render once first so we get an initial representation of things.
            // Otherwise, we may be inaccurate due to the way GeckoLib prepares render data.
            if (CacheOptimizationStorage.ARMOR_MODEL_CACHE.getIfPresent(key) == null) {
                return CacheOptimizationStorage.ARMOR_MODEL_CACHE.get(key, () -> original.call(instance, livingEntity, itemStack, equipmentSlot, originalModel));
            }

            // In the next frame, we should check if the value is the exact same as before, if we haven't yet
            // determined that we should be caching this.
            Boolean shouldBeCached = CacheOptimizationStorage.SHOULD_ACTUALLY_BE_CACHED.getIfPresent(key);

            if (shouldBeCached == null) {
                var previousModel = CacheOptimizationStorage.ARMOR_MODEL_CACHE.getIfPresent(key);

                if (previousModel != null) {
                    var currentModel = original.call(instance, livingEntity, itemStack, equipmentSlot, originalModel);

                    // If the model matches, we should not allow this to be cached.
                    if (previousModel == currentModel) {
                        CacheOptimizationStorage.SHOULD_ACTUALLY_BE_CACHED.put(key, false);
                        return currentModel;
                    } else { // Otherwise, we can safely allow it to cache.
                        CacheOptimizationStorage.SHOULD_ACTUALLY_BE_CACHED.put(key, true);
                        return previousModel;
                    }
                }
            } else if (shouldBeCached) {
                // In subsequent frames, if we are actually allowed to cache the data, we should prioritize that by all means.
                return CacheOptimizationStorage.ARMOR_MODEL_CACHE.get(key, () -> original.call(instance, livingEntity, itemStack, equipmentSlot, originalModel));
            }
        }

        return original.call(instance, livingEntity, itemStack, equipmentSlot, originalModel);
    }*/
}

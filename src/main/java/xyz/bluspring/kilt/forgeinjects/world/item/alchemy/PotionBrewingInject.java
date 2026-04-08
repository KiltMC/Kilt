// TRACKED HASH: 6c2af6a881a23ea04d8f49bf80f6e56938c38e8d
package xyz.bluspring.kilt.forgeinjects.world.item.alchemy;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.world.item.alchemy.PotionBrewingMixInjection;

@Mixin(PotionBrewing.class)
public abstract class PotionBrewingInject {
    @WrapOperation(method = {"isBrewablePotion", "mix"}, at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/alchemy/PotionBrewing$Mix;to:Ljava/lang/Object;"))
    private static <T> T kilt$useForgeDelegateIfPossibleForBrewable(PotionBrewing.Mix<T> instance, Operation<T> original) {
        var result = original.call(instance);

        if (result == null) {
            return ((PotionBrewingMixInjection<T>) instance).kilt$getTo().value();
        }

        return result;
    }

    @WrapOperation(method = {"hasContainerMix", "hasPotionMix", "mix"}, at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/alchemy/PotionBrewing$Mix;from:Ljava/lang/Object;"))
    private static <T> T kilt$useForgeDelegateIfPossibleForContainerMix(PotionBrewing.Mix<T> instance, Operation<T> original) {
        var result = original.call(instance);

        if (result == null) {
            return ((PotionBrewingMixInjection<T>) instance).kilt$getFrom().value();
        }

        return result;
    }

    @Mixin(PotionBrewing.Mix.class)
    public static abstract class MixInject<T> implements PotionBrewingMixInjection<T> {
        @Shadow @Final @Mutable T from;
        @Shadow @Final @Mutable T to;

        @Shadow @Final @Mutable public Ingredient ingredient;
        @Unique public Holder.Reference<T> kilt$from;
        @Unique public Holder.Reference<T> kilt$to;

        @Override
        public Holder.Reference<T> kilt$getFrom() {
            return kilt$from;
        }

        @Override
        public Holder.Reference<T> kilt$getTo() {
            return kilt$to;
        }

        @Override
        public void kilt$setFrom(Holder.Reference<T> from) {
            this.kilt$from = from;
        }

        @Override
        public void kilt$setTo(Holder.Reference<T> to) {
            this.kilt$to = to;
        }

        @Inject(method = "<init>", at = @At("TAIL"))
        private void kilt$getHolderValuesFromRegistry(T from, Ingredient ingredient, T to, CallbackInfo ci) {
            if (from instanceof Potion fromPotion && to instanceof Potion toPotion) {
                // if a mod doesn't register ahead of time, yell at them. (glaring at you, Additional Additions)
                this.kilt$from = (Holder.Reference<T>) ForgeRegistries.POTIONS.getDelegateOrThrow(fromPotion);
                this.kilt$to = (Holder.Reference<T>) ForgeRegistries.POTIONS.getDelegateOrThrow(toPotion);
            } else if (from instanceof Item fromItem && to instanceof Item toItem) {
                // we can actually do this here, unlike with Potion.
                this.kilt$from = (Holder.Reference<T>) ForgeRegistries.ITEMS.getDelegate(fromItem).orElseGet(() -> BuiltInRegistries.ITEM.createIntrusiveHolder(fromItem));
                this.kilt$to = (Holder.Reference<T>) ForgeRegistries.ITEMS.getDelegate(toItem).orElseGet(() -> BuiltInRegistries.ITEM.createIntrusiveHolder(toItem));
            } else {
                throw new IllegalStateException("Oh boy!");
            }
        }

        @CreateInitializer
        public MixInject(IForgeRegistry<T> registry, T from, Ingredient ingredient, T to) {
            this.kilt$from = registry.getDelegateOrThrow(from);
            this.kilt$to = registry.getDelegateOrThrow(to);
            this.from = null;
            this.to = null;
            this.ingredient = ingredient;
        }
    }
}
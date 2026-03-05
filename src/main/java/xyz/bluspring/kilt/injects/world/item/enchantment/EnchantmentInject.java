package xyz.bluspring.kilt.injects.world.item.enchantment;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.bluspring.kilt.injections.world.item.enchantment.EnchantmentBuilderInjection;

import java.util.function.UnaryOperator;

@Mixin(Enchantment.class)
public abstract class EnchantmentInject {
    @Mixin(Enchantment.Builder.class)
    public abstract static class BuilderInject implements EnchantmentBuilderInjection {
        @Unique protected UnaryOperator<MutableComponent> nameFactory = UnaryOperator.identity();

        @Override
        public Enchantment.Builder withCustomName(UnaryOperator<MutableComponent> nameFactory) {
            this.nameFactory = nameFactory;
            return (Enchantment.Builder) (Object) this;
        }

        @ModifyArg(method = "build", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;<init>(Lnet/minecraft/network/chat/Component;Lnet/minecraft/world/item/enchantment/Enchantment$EnchantmentDefinition;Lnet/minecraft/core/HolderSet;Lnet/minecraft/core/component/DataComponentMap;)V"))
        private Component kilt$tryCreateFromCustomNameFactory(Component description) {
            return this.nameFactory.apply(description.copy());
        }
    }
}

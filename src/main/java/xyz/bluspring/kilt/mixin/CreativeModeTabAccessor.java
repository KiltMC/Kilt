package xyz.bluspring.kilt.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Supplier;

@Mixin(CreativeModeTab.class)
public interface CreativeModeTabAccessor {
    @Invoker("<init>")
    static CreativeModeTab createCreativeModeTab(CreativeModeTab.Row row, int column, CreativeModeTab.Type type, Component displayName, Supplier<ItemStack> iconGenerator, CreativeModeTab.DisplayItemsGenerator displayItemGenerator) {
        throw new UnsupportedOperationException();
    }
}

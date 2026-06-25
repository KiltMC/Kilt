package xyz.bluspring.kilt.mixin;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

@Mixin(CreativeModeTab.class)
public interface CreativeModeTabAccessor {
    @Invoker("<init>")
    static CreativeModeTab createCreativeModeTab(CreativeModeTab.Row row, int column, CreativeModeTab.Type type, Component displayName, Supplier<ItemStack> iconGenerator, CreativeModeTab.DisplayItemsGenerator displayItemGenerator) {
        throw new UnsupportedOperationException();
    }

    @Accessor("DEFAULT_BACKGROUND")
    static Identifier getDefaultBackground() {
        throw new UnsupportedOperationException();
    }
}

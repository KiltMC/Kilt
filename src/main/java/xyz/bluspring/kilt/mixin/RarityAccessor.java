package xyz.bluspring.kilt.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Rarity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Rarity.class)
public interface RarityAccessor {
    @Invoker("<init>")
    static Rarity createRarity(String name, int id, ChatFormatting chatFormatting) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("MixinAnnotationTarget")
    @Accessor("$VALUES")
    static Rarity[] getValues() {
        throw new IllegalStateException();
    }

    @SuppressWarnings("MixinAnnotationTarget")
    @Accessor("$VALUES")
    @Mutable
    static void setValues(Rarity[] values) {
        throw new IllegalStateException();
    }
}

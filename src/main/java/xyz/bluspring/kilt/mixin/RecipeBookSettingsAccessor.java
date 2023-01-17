package xyz.bluspring.kilt.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.stats.RecipeBookSettings;
import net.minecraft.world.inventory.RecipeBookType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(RecipeBookSettings.class)
public interface RecipeBookSettingsAccessor {
    @Accessor("TAG_FIELDS")
    static Map<RecipeBookType, Pair<String, String>> getTagFields() {
        throw new UnsupportedOperationException();
    }
}

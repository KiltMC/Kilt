package xyz.bluspring.kilt.injections.stats;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.inventory.RecipeBookType;
import xyz.bluspring.kilt.mixin.RecipeBookSettingsAccessor;

public interface RecipeBookSettingsInjection {
    static void addTagsForType(RecipeBookType type, String openTag, String filteringTag) {
        RecipeBookSettingsAccessor.getTagFields().put(type, new Pair<>(openTag, filteringTag));
    }
}

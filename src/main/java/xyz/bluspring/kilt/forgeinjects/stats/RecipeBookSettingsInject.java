package xyz.bluspring.kilt.forgeinjects.stats;

import com.mojang.datafixers.util.Pair;
import net.minecraft.stats.RecipeBookSettings;
import net.minecraft.world.inventory.RecipeBookType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.stats.RecipeBookSettingsInjection;

import java.util.HashMap;
import java.util.Map;

@Mixin(RecipeBookSettings.class)
public class RecipeBookSettingsInject implements RecipeBookSettingsInjection {
    @CreateStatic
    private static void addTagsForType(RecipeBookType type, String openTag, String filteringTag) {
        RecipeBookSettingsInjection.addTagsForType(type, openTag, filteringTag);
    }

    @CreateStatic
    private static Map<RecipeBookType, Pair<String, String>> getTagFields() {
        return TAG_FIELDS;
    }

    @Shadow private static Map<RecipeBookType, Pair<String, String>> TAG_FIELDS;

    // there was, in fact, a better way.
    @Inject(at = @At("TAIL"), method = "<clinit>")
    private static void kilt$makeTagFieldsMutable(CallbackInfo ci) {
        TAG_FIELDS = new HashMap<>(TAG_FIELDS);
    }
}

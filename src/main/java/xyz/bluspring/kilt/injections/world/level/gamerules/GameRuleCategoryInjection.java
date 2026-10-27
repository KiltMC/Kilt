package xyz.bluspring.kilt.injections.world.level.gamerules;

import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.event.RegisterGameRuleCategoryEvent;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.world.level.gamerules.GameRuleCategory;

public interface GameRuleCategoryInjection {
    @ApiStatus.Internal
    static void registerModdedCategories() {
        ModLoader.postEvent(new RegisterGameRuleCategoryEvent(category ->
            GameRuleCategory.register(category.id())));
    }
}

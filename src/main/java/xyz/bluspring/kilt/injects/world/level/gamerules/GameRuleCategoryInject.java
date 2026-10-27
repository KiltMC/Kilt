package xyz.bluspring.kilt.injects.world.level.gamerules;

import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.level.gamerules.GameRuleCategoryInjection;

import net.minecraft.world.level.gamerules.GameRuleCategory;

@Mixin(GameRuleCategory.class)
public abstract class GameRuleCategoryInject {
    @CreateStatic
    private static void registerModdedCategories() {
        GameRuleCategoryInjection.registerModdedCategories();
    }
}

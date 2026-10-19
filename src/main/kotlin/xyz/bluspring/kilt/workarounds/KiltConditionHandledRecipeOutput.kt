package xyz.bluspring.kilt.workarounds

import net.minecraft.advancements.AdvancementHolder
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.crafting.Recipe
import net.neoforged.neoforge.common.conditions.ICondition

interface KiltConditionHandledRecipeOutput : RecipeOutput {
    // Kilt: Make sure a default exists that just points back to the extension, because that's how Neo handles stuff I guess.
    override fun accept(id: ResourceKey<Recipe<*>>, recipe: Recipe<*>, advancement: AdvancementHolder?) {
        this.accept(id, recipe, advancement, *emptyArray<ICondition>())
    }
}

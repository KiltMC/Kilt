package xyz.bluspring.kilt.injections.world.item.crafting;

import com.mojang.serialization.Codec;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.WithConditions;

import java.util.Optional;

public interface RecipeInjection {
    Codec<Optional<WithConditions<Recipe<?>>>> CONDITIONAL_CODEC = ConditionalOps.createConditionalCodecWithConditions(Recipe.CODEC);
}

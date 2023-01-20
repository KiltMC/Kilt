/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.RecipeProvider;

public final class ForgeRecipeProvider extends RecipeProvider
{
    // the funny outcome is that this is entirely fucking useless.
    // because it can just have a mixin.
    public ForgeRecipeProvider(DataGenerator generatorIn)
    {
        super(generatorIn);
    }
}

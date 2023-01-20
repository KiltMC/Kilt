/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common.data;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.ForgeMod;
import xyz.bluspring.kilt.remaps.data.tags.TagsProviderRemap;

import static net.minecraftforge.common.Tags.Fluids.MILK;

public final class ForgeFluidTagsProvider extends TagsProviderRemap<Fluid>
{
    public ForgeFluidTagsProvider(DataGenerator gen, ExistingFileHelper existingFileHelper)
    {
        super(gen, Registry.FLUID, "forge", existingFileHelper);
    }

    @Override
    public void addTags()
    {
        tag(MILK).addOptional(ForgeMod.MILK.getId()).addOptional(ForgeMod.FLOWING_MILK.getId());
    }
}

/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.registries;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public interface IForgeRegistryInternal<V> extends IForgeRegistry<V>
{
    void setSlaveMap(ResourceLocation name, @Nullable Object obj);
}

/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforgespi.coremod;

import cpw.mods.modlauncher.api.ITransformer;

/**
 * Provide using the Java {@link java.util.ServiceLoader} mechanism.
 */
public interface ICoreMod {
    Iterable<? extends ITransformer<?>> getTransformers();
}

/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common.capabilities;

import net.minecraftforge.common.util.INBTSerializable;
import net.minecraft.nbt.Tag;

//Just a mix of the two, useful in patches to lower the size.
public interface ICapabilitySerializable<T extends Tag> extends ICapabilityProvider, INBTSerializable<T> {}

package net.minecraftforge.common.capabilities

import net.minecraft.nbt.Tag
import net.minecraftforge.common.util.INBTSerializable

interface ICapabilitySerializable<T : Tag> : ICapabilityProvider, INBTSerializable<T>
package net.minecraftforge.entity

import net.minecraft.world.entity.Entity

abstract class PartEntity<T : Entity>(parent: T) : io.github.fabricators_of_create.porting_lib.entity.PartEntity<T>(parent)
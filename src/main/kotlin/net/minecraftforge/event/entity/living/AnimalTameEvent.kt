package net.minecraftforge.event.entity.living

import net.minecraft.world.entity.animal.Animal
import net.minecraft.world.entity.player.Player
import net.minecraftforge.eventbus.api.Cancelable

@Cancelable
class AnimalTameEvent(val animal: Animal, val tamer: Player) : LivingEvent(animal) {
}
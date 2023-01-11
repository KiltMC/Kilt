package net.minecraftforge.event.entity.player

import net.minecraft.world.entity.ExperienceOrb
import net.minecraft.world.entity.player.Player
import net.minecraftforge.eventbus.api.Cancelable

open class PlayerXpEvent(player: Player) : PlayerEvent(player) {

    @Cancelable
    class PickupXp(player: Player, val orb: ExperienceOrb) : PlayerXpEvent(player)

    @Cancelable
    class XpChange(player: Player, var amount: Int) : PlayerXpEvent(player)

    @Cancelable
    class LevelChange(player: Player, var levels: Int) : PlayerXpEvent(player)
}
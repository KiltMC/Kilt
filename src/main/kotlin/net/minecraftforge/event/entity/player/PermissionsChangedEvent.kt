package net.minecraftforge.event.entity.player

import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.eventbus.api.Cancelable

@Cancelable
class PermissionsChangedEvent(player: ServerPlayer, val newLevel: Int, val oldLevel: Int) : PlayerEvent(player) {
}
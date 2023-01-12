package net.minecraftforge.event.level

import net.minecraft.server.level.ServerLevel

class SleepFinishedTimeEvent(level: ServerLevel, newTime: Long, private val minTime: Long) : LevelEvent(level) {
    var newTime = newTime
        private set

    fun setTimeAddition(newTimeIn: Long): Boolean {
        if (minTime > newTimeIn)
            return false

        newTime = newTimeIn
        return true
    }
}
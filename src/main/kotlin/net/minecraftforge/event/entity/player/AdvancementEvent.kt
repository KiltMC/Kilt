package net.minecraftforge.event.entity.player

import net.minecraft.advancements.Advancement
import net.minecraft.advancements.AdvancementProgress
import net.minecraft.world.entity.player.Player

class AdvancementEvent(player: Player, val advancement: Advancement) : PlayerEvent(player) {
    class AdvancementEarnEvent(player: Player, earned: Advancement) : PlayerEvent(player) {
        val advancement = earned
    }

    class AdvancementProgressEvent(player: Player, progressed: Advancement, val advancementProgress: AdvancementProgress, val criterionName: String, val progressType: ProgressType) : PlayerEvent(player) {
        val advancement = progressed

        enum class ProgressType {
            GRANT, REVOKE
        }
    }
}
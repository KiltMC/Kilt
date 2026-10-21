package xyz.bluspring.kilt.client.workarounds

import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import net.minecraft.resources.Identifier
import net.neoforged.neoforge.client.gui.VanillaGuiLayers

object HudElementIdRemapper {
    private val neoToFabricMappings = mapOf(
        VanillaGuiLayers.CAMERA_OVERLAYS to VanillaHudElements.MISC_OVERLAYS,
        VanillaGuiLayers.CROSSHAIR to VanillaHudElements.CROSSHAIR,
        VanillaGuiLayers.AFTER_CAMERA_DECORATIONS to VanillaHudElements.SPECTATOR_MENU,
        VanillaGuiLayers.HOTBAR to VanillaHudElements.HOTBAR,
        VanillaGuiLayers.PLAYER_HEALTH to VanillaHudElements.HEALTH_BAR,
        VanillaGuiLayers.ARMOR_LEVEL to VanillaHudElements.ARMOR_BAR,
        VanillaGuiLayers.FOOD_LEVEL to VanillaHudElements.FOOD_BAR,
        VanillaGuiLayers.VEHICLE_HEALTH to VanillaHudElements.MOUNT_HEALTH,
        VanillaGuiLayers.AIR_LEVEL to VanillaHudElements.AIR_BAR,
        VanillaGuiLayers.CONTEXTUAL_INFO_BAR_BACKGROUND to VanillaHudElements.INFO_BAR, // TODO: this should probably be different.
        VanillaGuiLayers.EXPERIENCE_LEVEL to VanillaHudElements.EXPERIENCE_LEVEL,
        VanillaGuiLayers.CONTEXTUAL_INFO_BAR to VanillaHudElements.INFO_BAR,
        VanillaGuiLayers.SELECTED_ITEM_NAME to VanillaHudElements.HELD_ITEM_TOOLTIP,
        VanillaGuiLayers.SPECTATOR_TOOLTIP to VanillaHudElements.SPECTATOR_TOOLTIP,
        VanillaGuiLayers.EFFECTS to VanillaHudElements.MOB_EFFECTS,
        VanillaGuiLayers.BOSS_OVERLAY to VanillaHudElements.BOSS_BAR,
        VanillaGuiLayers.SLEEP_OVERLAY to VanillaHudElements.SLEEP,
        VanillaGuiLayers.DEMO_OVERLAY to VanillaHudElements.DEMO_TIMER,
        VanillaGuiLayers.SCOREBOARD_SIDEBAR to VanillaHudElements.SCOREBOARD,
        VanillaGuiLayers.OVERLAY_MESSAGE to VanillaHudElements.OVERLAY_MESSAGE,
        VanillaGuiLayers.TITLE to VanillaHudElements.TITLE_AND_SUBTITLE,
        VanillaGuiLayers.CHAT to VanillaHudElements.CHAT,
        VanillaGuiLayers.TAB_LIST to VanillaHudElements.PLAYER_LIST,
        VanillaGuiLayers.SUBTITLE_OVERLAY to VanillaHudElements.SUBTITLES,
    )

    @JvmStatic
    fun tryRemap(id: Identifier): Identifier {
        return this.neoToFabricMappings.getOrDefault(id, id)
    }
}

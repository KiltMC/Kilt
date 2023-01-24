package xyz.bluspring.kilt.remaps.client.gui.screens

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.MenuScreens
import net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor
import net.minecraft.core.Registry
import net.minecraft.network.chat.Component
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.MenuType
import org.apache.logging.log4j.LogManager
import java.util.*

object MenuScreensRemap : MenuScreens() {
    private val logger = LogManager.getLogger()

    // Copied from Minecraft, with Forge's patches added on.
    // Apparently literally none of the params are actually used by Forge. What the hell.
    @JvmStatic
    fun <T : AbstractContainerMenu> getScreenFactory(menuType: MenuType<T>?, minecraft: Minecraft, i: Int, component: Component): Optional<ScreenConstructor<T, *>> {
        if (menuType == null) {
            logger.warn("Trying to open invalid screen with name: {}", component.string)
        } else {
            val screenConstructor = MenuScreens.getConstructor(menuType)
            if (screenConstructor == null) {
                logger.warn("Failed to create screen for menu type: {}", Registry.MENU.getKey(menuType))
            } else {
                return Optional.of(screenConstructor)
            }
        }

        return Optional.empty()
    }
}
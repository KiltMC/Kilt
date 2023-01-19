package net.minecraftforge.fml

import org.apache.logging.log4j.Marker
import org.apache.logging.log4j.MarkerManager

object Logging {
    @JvmField val CORE: Marker = MarkerManager.getMarker("CORE")
    @JvmField val LOADING: Marker = MarkerManager.getMarker("LOADING")
    @JvmField val SCAN: Marker = MarkerManager.getMarker("SCAN")
    @JvmField val SPLASH: Marker = MarkerManager.getMarker("SPLASH")
    @JvmField val CAPABILITIES: Marker = MarkerManager.getMarker("CAPABILITIES")
    @JvmField val MODELLOADING: Marker = MarkerManager.getMarker("MODELLOADING")
    @JvmField val FORGEMOD: Marker = MarkerManager.getMarker("FORGEMOD").addParents(LOADING)
}
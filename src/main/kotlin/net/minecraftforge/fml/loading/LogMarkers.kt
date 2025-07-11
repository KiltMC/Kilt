package net.minecraftforge.fml.loading

import org.slf4j.Marker
import org.slf4j.MarkerFactory

object LogMarkers {
    @JvmField val CORE: Marker = MarkerFactory.getMarker("CORE")
    @JvmField val LOADING: Marker = MarkerFactory.getMarker("LOADING")
    @JvmField val SCAN: Marker = MarkerFactory.getMarker("SCAN")
    @JvmField val SPLASH: Marker = MarkerFactory.getMarker("SPLASH")
}
package xyz.bluspring.kilt.loader

import net.minecraftforge.fml.common.Mod.EventBusSubscriber

data class ForgeMod(
    val modContainer: Any,
    val modId: String,
    val events: MutableList<EventBusSubscriber>
)

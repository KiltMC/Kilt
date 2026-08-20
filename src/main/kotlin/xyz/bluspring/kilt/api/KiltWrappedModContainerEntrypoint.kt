package xyz.bluspring.kilt.api

import net.neoforged.fml.ModContainer

/**
 * This is an entrypoint that allows Fabric mods to have access to their own NeoForge-based mod container,
 * which allows them to access mod bus events from NeoForge.
 *
 * [ModContainer.getEventBus] is nullable in FML, but the actual provided event bus itself is never nullable here.
 */
@Deprecated(message = "Migrated to Twill", replaceWith = ReplaceWith("WrappedModContainerEntrypoint", "xyz.bluspring.twill.api.WrappedModContainerEntrypoint"), level = DeprecationLevel.WARNING)
interface KiltWrappedModContainerEntrypoint {
    fun onLoadModContainer(container: ModContainer)

    companion object {
        const val ENTRYPOINT = "kilt:mod_container"
    }
}

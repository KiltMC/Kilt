package xyz.bluspring.kilt.compat.fabric.rei

import me.shedaniel.rei.api.common.plugins.REIPlugin
import me.shedaniel.rei.api.common.plugins.REIPluginProvider
import xyz.bluspring.kilt.loader.mod.NeoForgeMod

class KiltREIPluginProvider<P : REIPlugin<*>>(private val plugin: REIPluginProvider<P>, private val mod: NeoForgeMod) : REIPluginProvider<P> {
    override fun provide(): Collection<P?>? {
        return plugin.provide()
    }

    override fun getPluginProviderClass(): Class<P?>? {
        return plugin.pluginProviderClass
    }

    override fun getPluginProviderName(): String? {
        return "${plugin.pluginProviderName} [${mod.definition.id}]"
    }
}
package xyz.bluspring.kilt.gradle.loom

import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.PluginAware

class KiltLoomPlugin : Plugin<PluginAware> {
    override fun apply(target: PluginAware) {
        if (target is Project)
            applyProject(target)
    }

    fun applyProject(project: Project) {
        project.logger.lifecycle("Applying Kilt Jar transformers")

        val loomExtension = project.extensions.getByName("loom") as LoomGradleExtensionAPI

//        loomExtension.addMinecraftJarProcessor(AccessTransformerProcessor::class.java)
    }
}

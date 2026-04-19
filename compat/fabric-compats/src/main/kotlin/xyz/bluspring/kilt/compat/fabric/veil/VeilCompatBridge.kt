package xyz.bluspring.kilt.compat.fabric.veil

import foundry.veil.api.event.VeilRenderLevelStageEvent
import foundry.veil.fabric.event.*
import foundry.veil.forge.event.*
import net.neoforged.neoforge.common.NeoForge

object VeilCompatBridge {
    fun init() {
        FabricFreeNativeResourcesEvent.EVENT.register { NeoForge.EVENT_BUS.post(ForgeFreeNativeResourcesEvent()) }
        FabricVeilAddShaderPreProcessorsEvent.EVENT.register { provider, registry -> NeoForge.EVENT_BUS.post(ForgeVeilAddShaderProcessorsEvent(provider, registry)) }
        FabricVeilDynamicBuffersChangedEvent.EVENT.register { change -> NeoForge.EVENT_BUS.post(ForgeVeilDynamicBuffersChangedEvent(change)) }
        FabricVeilPostProcessingEvent.PRE.register { location, pipeline, context -> NeoForge.EVENT_BUS.post(ForgeVeilPostProcessingEvent.Pre(location, pipeline, context)) }
        FabricVeilPostProcessingEvent.POST.register { location, pipeline, context -> NeoForge.EVENT_BUS.post(ForgeVeilPostProcessingEvent.Post(location, pipeline, context)) }
        FabricVeilRegisterBlockLayersEvent.EVENT.register { registry -> NeoForge.EVENT_BUS.post(ForgeVeilRegisterBlockLayersEvent(registry)) }
        FabricVeilRegisterFixedBuffersEvent.EVENT.register { registry ->
            NeoForge.EVENT_BUS.post(ForgeVeilRegisterFixedBuffersEvent { stage, type ->
                registry.registerFixedBuffer(VeilRenderLevelStageEvent.Stage.valueOf(stage.toString().uppercase()), type)
            })
        }
        FabricVeilRegisterGlobalControllersEvent.EVENT.register { registry -> NeoForge.EVENT_BUS.post(ForgeVeilRegisterGlobalControllersEvent(registry)) }
        FabricVeilRendererAvailableEvent.EVENT.register { renderer -> NeoForge.EVENT_BUS.post(ForgeVeilRendererAvailableEvent(renderer)) }
        FabricVeilShaderCompileEvent.EVENT.register { shaderManager, map -> NeoForge.EVENT_BUS.post(ForgeVeilShaderCompileEvent(shaderManager, map)) }
    }
}

package xyz.bluspring.kilt.client

import com.google.common.collect.ImmutableMap
import io.github.fabricators_of_create.porting_lib.event.client.ClientWorldEvents
import io.github.fabricators_of_create.porting_lib.event.client.TextureStitchCallback
import io.github.fabricators_of_create.porting_lib.models.geometry.RegisterGeometryLoadersCallback
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.fabricmc.loader.DependencyException
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.screens.Screen
import net.minecraftforge.client.ForgeHooksClient
import net.minecraftforge.client.event.*
import net.minecraftforge.client.gui.overlay.ForgeGui
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.ForgeEventFactory
import net.minecraftforge.event.level.LevelEvent
import net.minecraftforge.fml.ModLoader
import xyz.bluspring.kilt.Kilt.Companion.loader
import xyz.bluspring.kilt.mixin.GeometryLoaderManagerAccessor
import xyz.bluspring.kilt.mixin.LevelRendererAccessor
import xyz.bluspring.kilt.mixin.ScreenAccessor
import xyz.bluspring.knit.loader.KnitLoader
import java.util.function.Consumer

@Suppress("removal")
class KiltClient : ClientModInitializer {
    override fun onInitializeClient() {
        val fabricLoader = FabricLoader.getInstance()
        val kiltErrorMessage = "Detected Flywheel Forge, please use either Create Fabric or Flywheel Fabric via Vanillin!"

        if ((loader.hasMod("quartz") || loader.hasMod("simpleclouds")) && !fabricLoader.isModLoaded("threatengl")) {
            KnitLoader.instance.displayErrorGUI(kiltErrorMessage, DependencyException("Mods requiring newer OpenGL detected, please install the ThreatenGL mod to fix this error!"))
            return
        }

        registerFabricEvents()

        hasInitialized = true
    }

    private fun registerFabricEvents() {
        val mc = Minecraft.getInstance()

        /*ParticleManagerRegistrationCallback.EVENT.register {
            Kilt.loader.postEvent(RegisterParticleProvidersEvent(Minecraft.getInstance().particleEngine))
        }*/

        ItemTooltipCallback.EVENT.register { stack, flag, components ->
            ForgeEventFactory.onItemTooltip(stack, null, components, flag)
        }

        /*ClientGuiEvent.RENDER_PRE.register { screen, poseStack, x, y, delta ->
            if (MinecraftForge.EVENT_BUS.post(ScreenEvent.Render.Pre(screen, poseStack, x, y, delta)))
                EventResult.interruptFalse()
            else
                EventResult.pass()
        }*/

        HudRenderCallback.EVENT.register { guiGraphics, delta ->
            forgeGui.render(guiGraphics, delta)
        }

        /*ClientGuiEvent.RENDER_POST.register { screen, poseStack, x, y, delta ->
            if (screen != null)
                MinecraftForge.EVENT_BUS.post(ScreenEvent.Render.Post(screen, poseStack, x, y, delta))
        }*/

        TextureStitchCallback.POST.register { atlas ->
            ModLoader.get().postEvent(TextureStitchEvent.Post(atlas))
        }

        /*WorldRenderEvents.AFTER_ENTITIES.register {
            postRenderLevelStage(RenderLevelStageEvent.Stage.AFTER_ENTITIES, it)
            postRenderLevelStage(RenderLevelStageEvent.Stage.AFTER_PARTICLES, it)
        }

        WorldRenderEvents.AFTER_TRANSLUCENT.register {
            postRenderLevelStage(RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS, it)
        }

        WorldRenderEvents.AFTER_SETUP.register {
            postRenderLevelStage(RenderLevelStageEvent.Stage.AFTER_SKY, it)
        }

        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register { context, hitResult ->
            if (hitResult == null)
                return@register false

            when (hitResult.type) {
                HitResult.Type.BLOCK -> {
                    if (hitResult !is BlockHitResult)
                        return@register false

                    return@register !MinecraftForge.EVENT_BUS.post(RenderHighlightEvent.Block(context.worldRenderer(), context.camera(), hitResult, context.tickDelta(), context.matrixStack(), context.consumers()))
                }

                HitResult.Type.ENTITY -> {
                    if (hitResult !is EntityHitResult)
                        return@register false

                    return@register !MinecraftForge.EVENT_BUS.post(RenderHighlightEvent.Entity(context.worldRenderer(), context.camera(), hitResult, context.tickDelta(), context.matrixStack(), context.consumers()))
                }

                else -> return@register false
            }
        }*/

        RegisterGeometryLoadersCallback.EVENT.register { map ->
            shouldPostGeoLoaders = true

            ModLoader.get().kiltPostEventWrappingMods(ModelEvent.RegisterGeometryLoaders(map))
        }

        ScreenEvents.AFTER_INIT.register { client, screen, width, height ->
            // Handled in MouseHandlerInject
            /*ScreenMouseEvents.allowMouseClick(screen).register { _, mouseX, mouseY, button ->
                !ForgeHooksClient.onScreenMouseClickedPre(screen, mouseX, mouseY, button)
            }

            ScreenMouseEvents.afterMouseClick(screen).register { _, mouseX, mouseY, button ->
                ForgeHooksClient.onScreenMouseClickedPost(screen, mouseX, mouseY, button, true) // TODO: set handled
            }

            ScreenMouseEvents.allowMouseRelease(screen).register { _, mouseX, mouseY, button ->
                !ForgeHooksClient.onScreenMouseReleasedPre(screen, mouseX, mouseY, button)
            }

            ScreenMouseEvents.afterMouseRelease(screen).register { _, mouseX, mouseY, button ->
                ForgeHooksClient.onScreenMouseReleasedPost(screen, mouseX, mouseY, button, true) // TODO: set handled
            }

            ScreenMouseEvents.allowMouseScroll(screen).register { _, mouseX, mouseY, scrollX, scrollY ->
                !ForgeHooksClient.onScreenMouseScrollPre(Minecraft.getInstance().mouseHandler, screen, scrollY)
            }

            ScreenMouseEvents.afterMouseScroll(screen).register { _, mouseX, mouseY, scrollX, scrollY ->
                ForgeHooksClient.onScreenMouseScrollPost(Minecraft.getInstance().mouseHandler, screen, scrollY)
            }*/

            ScreenKeyboardEvents.allowKeyPress(screen).register { _, key, scanCode, modifiers ->
                !ForgeHooksClient.onScreenKeyPressedPre(screen, key, scanCode, modifiers)
            }

            ScreenKeyboardEvents.afterKeyPress(screen).register { _, key, scanCode, modifiers ->
                ForgeHooksClient.onScreenKeyPressedPost(screen, key, scanCode, modifiers) // TODO: set handled
            }

            ScreenKeyboardEvents.allowKeyRelease(screen).register { _, key, scanCode, modifiers ->
                !ForgeHooksClient.onScreenKeyReleasedPre(screen, key, scanCode, modifiers)
            }

            ScreenKeyboardEvents.afterKeyRelease(screen).register { _, key, scanCode, modifiers ->
                ForgeHooksClient.onScreenKeyReleasedPost(screen, key, scanCode, modifiers) // TODO: set handled
            }
        }

        /*RenderHandCallback.EVENT.register { event ->
            val forgeEvent = RenderHandEvent(event.hand, event.poseStack, event.multiBufferSource, event.packedLight, event.partialTicks, event.pitch, event.swingProgress, event.equipProgress, event.itemStack)
            MinecraftForge.EVENT_BUS.post(forgeEvent)

            if (forgeEvent.isCanceled)
                event.isCanceled = true
        }*/

        ClientTickEvents.START_CLIENT_TICK.register {
            ForgeEventFactory.onPreClientTick()
        }

        ClientTickEvents.END_CLIENT_TICK.register {
            ForgeEventFactory.onPostClientTick()
        }

        ClientTickEvents.START_WORLD_TICK.register {
            ForgeEventFactory.onPreLevelTick(it) { true }
        }

        ClientTickEvents.END_WORLD_TICK.register {
            ForgeEventFactory.onPostLevelTick(it) { true }
        }

        /*ClientWorldEvents.LOAD.register { client, level ->
            MinecraftForge.EVENT_BUS.post(LevelEvent.Load(level))
        }*/

        ClientWorldEvents.UNLOAD.register { client, level ->
            MinecraftForge.EVENT_BUS.post(LevelEvent.Unload(level))
        }
    }

    private fun postRenderLevelStage(stage: RenderLevelStageEvent.Stage, context: WorldRenderContext) {
        MinecraftForge.EVENT_BUS.post(RenderLevelStageEvent(stage, context.worldRenderer(), context.matrixStack(), context.projectionMatrix(), (context.worldRenderer() as LevelRendererAccessor).ticks, context.tickDelta(), context.camera(), context.frustum()))
    }

    companion object {
        var hasInitialized = false
            private set

        lateinit var forgeGui: ForgeGui
        private var shouldPostGeoLoaders = false

        fun lateRegisterEvents() {
            if (shouldPostGeoLoaders) {
                val map = GeometryLoaderManagerAccessor.getLoaders().toMutableMap()
                ModLoader.get().kiltPostEventWrappingMods(ModelEvent.RegisterGeometryLoaders(map))

                GeometryLoaderManagerAccessor.setLoaders(ImmutableMap.copyOf(map))
                GeometryLoaderManagerAccessor.setLoaderList(map.keys.joinToString(", ") { it.toString() })
            }
        }
    }
}
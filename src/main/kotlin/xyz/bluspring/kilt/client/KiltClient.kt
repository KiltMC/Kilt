package xyz.bluspring.kilt.client

import com.google.common.collect.ImmutableMap
import dev.architectury.event.EventResult
import dev.architectury.event.events.client.ClientGuiEvent
import dev.architectury.event.events.client.ClientRawInputEvent
import io.github.fabricators_of_create.porting_lib.event.client.TextureAtlasStitchedEvent
import io.github.fabricators_of_create.porting_lib.models.geometry.RegisterGeometryLoadersCallback
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.screens.Screen
import net.neoforged.fml.ModLoader
import net.neoforged.neoforge.client.ClientHooks
import net.neoforged.neoforge.client.event.ContainerScreenEvent
import net.neoforged.neoforge.client.event.ModelEvent
import net.neoforged.neoforge.client.event.ScreenEvent
import net.neoforged.neoforge.common.CommonHooks
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.EventHooks
import xyz.bluspring.kilt.mixin.GeometryLoaderManagerAccessor
import xyz.bluspring.kilt.mixin.LevelRendererAccessor
import xyz.bluspring.kilt.mixin.ScreenAccessor
import java.util.function.Consumer

@Suppress("removal")
class KiltClient : ClientModInitializer {
    override fun onInitializeClient() {
        registerFabricEvents()

        hasInitialized = true
    }

    private fun registerFabricEvents() {
        val mc = Minecraft.getInstance()

        /*ParticleManagerRegistrationCallback.EVENT.register {
            Kilt.loader.postEvent(RegisterParticleProvidersEvent(Minecraft.getInstance().particleEngine))
        }*/

        ItemTooltipCallback.EVENT.register { stack, context, flags, components ->
            EventHooks.onItemTooltip(stack, null, components, flags, context)
        }

        val add = mutableMapOf<Screen, Consumer<GuiEventListener>>()

        ClientGuiEvent.INIT_PRE.register { screen, access ->
            add[screen] = Consumer<GuiEventListener> {
                if (it is Renderable)
                    access.renderables.add(it)

                if (it is NarratableEntry)
                    access.narratables.add(it)

                (screen as ScreenAccessor).children.add(it)
            }

            if (NeoForge.EVENT_BUS.post(ScreenEvent.Init.Pre(screen, (screen as ScreenAccessor).children, add[screen]!!, screen::callRemoveWidget)).isCanceled) {
                add.remove(screen)
                EventResult.interruptFalse()
            } else EventResult.pass()
        }

        ClientGuiEvent.INIT_POST.register { screen, _ ->
            NeoForge.EVENT_BUS.post(ScreenEvent.Init.Post(screen, (screen as ScreenAccessor).children, add[screen]!!, screen::callRemoveWidget))
            add.remove(screen)
        }

        ClientGuiEvent.RENDER_CONTAINER_BACKGROUND.register { screen, poseStack, x, y, _ ->
            NeoForge.EVENT_BUS.post(ContainerScreenEvent.Render.Background(screen, poseStack, x, y))
        }

        ClientGuiEvent.RENDER_CONTAINER_FOREGROUND.register { screen, poseStack, x, y, _ ->
            NeoForge.EVENT_BUS.post(ContainerScreenEvent.Render.Foreground(screen, poseStack, x, y))
        }

        /*ClientGuiEvent.RENDER_PRE.register { screen, poseStack, x, y, delta ->
            if (NeoForge.EVENT_BUS.post(ScreenEvent.Render.Pre(screen, poseStack, x, y, delta)))
                EventResult.interruptFalse()
            else
                EventResult.pass()
        }*/

        /*ClientGuiEvent.RENDER_POST.register { screen, poseStack, x, y, delta ->
            if (screen != null)
                NeoForge.EVENT_BUS.post(ScreenEvent.Render.Post(screen, poseStack, x, y, delta))
        }*/

        TextureAtlasStitchedEvent.EVENT.register { event ->
            val forgeEvent = net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent(event.atlas)
            ModLoader.postEventWrapContainerInModOrder(forgeEvent)
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

                    return@register !NeoForge.EVENT_BUS.post(RenderHighlightEvent.Block(context.worldRenderer(), context.camera(), hitResult, context.tickDelta(), context.matrixStack(), context.consumers()))
                }

                HitResult.Type.ENTITY -> {
                    if (hitResult !is EntityHitResult)
                        return@register false

                    return@register !NeoForge.EVENT_BUS.post(RenderHighlightEvent.Entity(context.worldRenderer(), context.camera(), hitResult, context.tickDelta(), context.matrixStack(), context.consumers()))
                }

                else -> return@register false
            }
        }*/

        RegisterGeometryLoadersCallback.EVENT.register { map ->
            shouldPostGeoLoaders = true

            ModLoader.postEventWrapContainerInModOrder(ModelEvent.RegisterGeometryLoaders(map))
        }

        ScreenEvents.BEFORE_INIT.register { client, screen, width, height ->
            ScreenMouseEvents.allowMouseClick(screen).register { _, mouseX, mouseY, button ->
                !ClientHooks.onScreenMouseClickedPre(screen, mouseX, mouseY, button)
            }

            ScreenMouseEvents.afterMouseClick(screen).register { _, mouseX, mouseY, button ->
                ClientHooks.onScreenMouseClickedPost(screen, mouseX, mouseY, button, true) // TODO: set handled
            }

            ScreenMouseEvents.allowMouseRelease(screen).register { _, mouseX, mouseY, button ->
                !ClientHooks.onScreenMouseReleasedPre(screen, mouseX, mouseY, button)
            }

            ScreenMouseEvents.afterMouseRelease(screen).register { _, mouseX, mouseY, button ->
                ClientHooks.onScreenMouseReleasedPost(screen, mouseX, mouseY, button, true) // TODO: set handled
            }

            ScreenMouseEvents.allowMouseScroll(screen).register { _, mouseX, mouseY, scrollX, scrollY ->
                !ClientHooks.onScreenMouseScrollPre(Minecraft.getInstance().mouseHandler, screen, scrollX, scrollY)
            }

            ScreenMouseEvents.afterMouseScroll(screen).register { _, mouseX, mouseY, scrollX, scrollY ->
                ClientHooks.onScreenMouseScrollPost(Minecraft.getInstance().mouseHandler, screen, scrollX, scrollY)
            }

            ScreenKeyboardEvents.allowKeyPress(screen).register { _, key, scanCode, modifiers ->
                !ClientHooks.onScreenKeyPressedPre(screen, key, scanCode, modifiers)
            }

            ScreenKeyboardEvents.afterKeyPress(screen).register { _, key, scanCode, modifiers ->
                ClientHooks.onScreenKeyPressedPost(screen, key, scanCode, modifiers)
            }

            ScreenKeyboardEvents.allowKeyRelease(screen).register { _, key, scanCode, modifiers ->
                !ClientHooks.onScreenKeyReleasedPre(screen, key, scanCode, modifiers)
            }

            ScreenKeyboardEvents.afterKeyRelease(screen).register { _, key, scanCode, modifiers ->
                ClientHooks.onScreenKeyReleasedPost(screen, key, scanCode, modifiers)
            }
        }

        ClientRawInputEvent.MOUSE_SCROLLED.register { client, xScroll, yScroll ->
            if (ClientHooks.onMouseScroll(client.mouseHandler, xScroll, yScroll)) {
                EventResult.interruptTrue()
            }
            EventResult.pass()
        }

        /*RenderHandCallback.EVENT.register { event ->
            val forgeEvent = RenderHandEvent(event.hand, event.poseStack, event.multiBufferSource, event.packedLight, event.partialTicks, event.pitch, event.swingProgress, event.equipProgress, event.itemStack)
            NeoForge.EVENT_BUS.post(forgeEvent)

            if (forgeEvent.isCanceled)
                event.isCanceled = true
        }*/

        ClientTickEvents.START_CLIENT_TICK.register {
            ClientHooks.fireClientTickPre()
        }

        ClientTickEvents.END_CLIENT_TICK.register {
            ClientHooks.fireClientTickPost()
        }

        ClientTickEvents.START_WORLD_TICK.register {
            EventHooks.fireLevelTickPre(it) { true }
        }

        ClientTickEvents.END_WORLD_TICK.register {
            EventHooks.fireLevelTickPost(it) { true }
        }

        /*ClientWorldEvents.LOAD.register { client, level ->
            NeoForge.EVENT_BUS.post(LevelEvent.Load(level))
        }*/

        /*ClientWorldEvents.UNLOAD.register { client, level ->
            NeoForge.EVENT_BUS.post(LevelEvent.Unload(level))
        }*/
    }

    companion object {
        var hasInitialized = false
            private set

        private var shouldPostGeoLoaders = false

        fun lateRegisterEvents() {
            if (shouldPostGeoLoaders) {
                val map = GeometryLoaderManagerAccessor.getLoaders().toMutableMap()
                ModLoader.postEventWrapContainerInModOrder(ModelEvent.RegisterGeometryLoaders(map))

                GeometryLoaderManagerAccessor.setLoaders(ImmutableMap.copyOf(map))
                GeometryLoaderManagerAccessor.setLoaderList(map.keys.joinToString(", ") { it.toString() })
            }
        }
    }
}
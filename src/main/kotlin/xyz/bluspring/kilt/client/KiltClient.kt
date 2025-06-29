package xyz.bluspring.kilt.client

import com.google.common.collect.ImmutableMap
import dev.architectury.event.EventResult
import dev.architectury.event.events.client.ClientGuiEvent
import dev.architectury.event.events.client.ClientRawInputEvent
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

        ItemTooltipCallback.EVENT.register { stack, context, type, components ->
            ForgeEventFactory.onItemTooltip(stack, null, components, flag)
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

            if (MinecraftForge.EVENT_BUS.post(ScreenEvent.Init.Pre(screen, (screen as ScreenAccessor).children, add[screen]!!, screen::callRemoveWidget))) {
                add.remove(screen)
                EventResult.interruptFalse()
            } else EventResult.pass()
        }

        ClientGuiEvent.INIT_POST.register { screen, _ ->
            MinecraftForge.EVENT_BUS.post(ScreenEvent.Init.Post(screen, (screen as ScreenAccessor).children, add[screen]!!, screen::callRemoveWidget))
            add.remove(screen)
        }

        ClientGuiEvent.RENDER_CONTAINER_BACKGROUND.register { screen, poseStack, x, y, _ ->
            MinecraftForge.EVENT_BUS.post(ContainerScreenEvent.Render.Background(screen, poseStack, x, y))
        }

        ClientGuiEvent.RENDER_CONTAINER_FOREGROUND.register { screen, poseStack, x, y, _ ->
            MinecraftForge.EVENT_BUS.post(ContainerScreenEvent.Render.Foreground(screen, poseStack, x, y))
        }

        /*ClientGuiEvent.RENDER_PRE.register { screen, poseStack, x, y, delta ->
            if (MinecraftForge.EVENT_BUS.post(ScreenEvent.Render.Pre(screen, poseStack, x, y, delta)))
                EventResult.interruptFalse()
            else
                EventResult.pass()
        }*/

        ClientGuiEvent.RENDER_HUD.register { guiGraphics, delta ->
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

        ScreenEvents.BEFORE_INIT.register { client, screen, width, height ->
            ScreenMouseEvents.allowMouseClick(screen).register { _, mouseX, mouseY, button ->
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
            }

            ScreenKeyboardEvents.allowKeyPress(screen).register { _, key, scanCode, modifiers ->
                !ForgeHooksClient.onScreenKeyPressedPre(screen, key, scanCode, modifiers)
            }

            ScreenKeyboardEvents.afterKeyPress(screen).register { _, key, scanCode, modifiers ->
                ForgeHooksClient.onScreenKeyPressedPost(screen, key, scanCode, modifiers)
            }

            ScreenKeyboardEvents.allowKeyRelease(screen).register { _, key, scanCode, modifiers ->
                !ForgeHooksClient.onScreenKeyReleasedPre(screen, key, scanCode, modifiers)
            }

            ScreenKeyboardEvents.afterKeyRelease(screen).register { _, key, scanCode, modifiers ->
                ForgeHooksClient.onScreenKeyReleasedPost(screen, key, scanCode, modifiers)
            }
        }

        ClientRawInputEvent.MOUSE_SCROLLED.register { client, amount ->
            if (ForgeHooksClient.onMouseScroll(client.mouseHandler, amount)) {
                EventResult.interruptTrue()
            }
            EventResult.pass()
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
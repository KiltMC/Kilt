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

        ItemTooltipCallback.EVENT.register { stack, context, flags, components ->
            EventHooks.onItemTooltip(stack, null, components, flags, context)
        }

        HudRenderCallback.EVENT.register { guiGraphics, delta ->
            forgeGui.render(guiGraphics, delta)
        }

        TextureAtlasStitchedEvent.EVENT.register { event ->
            val forgeEvent = net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent(event.atlas)
            ModLoader.postEventWrapContainerInModOrder(forgeEvent)
        }

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
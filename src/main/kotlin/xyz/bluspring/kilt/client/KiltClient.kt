package xyz.bluspring.kilt.client

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer
import net.minecraft.client.gui.components.debug.DebugScreenEntries
import net.minecraft.client.gui.components.debug.DebugScreenEntry
import net.minecraft.world.level.Level
import net.minecraft.world.level.chunk.LevelChunk
import net.neoforged.neoforge.client.ClientHooks
import net.neoforged.neoforge.event.EventHooks
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.twill.TwillClient

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

        DebugScreenEntries.register(Kilt.id("version"), object : DebugScreenEntry {
            override fun display(displayer: DebugScreenDisplayer, serverOrClientLevel: Level?, clientChunk: LevelChunk?, serverChunk: LevelChunk?) {
                val version = FabricLoader.getInstance().getModContainer("kilt")
                    .orElseThrow().metadata.version.friendlyString

                val color = if (version.endsWith("-local"))
                    "§c"
                else if (version.contains("+build."))
                    "§6"
                else
                    "§b"

                displayer.addToGroup(TwillClient.id("info"), "Kilt ${color}v$version")
            }

            override fun isAllowed(reducedDebugInfo: Boolean): Boolean {
                return true
            }
        })

//        TextureAtlasStitchedEvent.EVENT.register { event ->
//            val forgeEvent = net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent(event.atlas)
//            ModLoader.postEventWrapContainerInModOrder(forgeEvent)
//        }
//
//        RegisterGeometryLoadersCallback.EVENT.register { map ->
//            shouldPostGeoLoaders = true
//
//            val neoMap = mutableMapOf<ResourceLocation, IGeometryLoader<*>>()
//            ModLoader.postEventWrapContainerInModOrder(ModelEvent.RegisterGeometryLoaders(neoMap))
//
//            // Convert, because it just works.
//            map.putAll(neoMap)
//        }

        ScreenEvents.BEFORE_INIT.register { client, screen, width, height ->
            ScreenMouseEvents.allowMouseClick(screen).register { _, event ->
                !ClientHooks.onScreenMouseClickedPre(screen, event, false) // TODO: set double click
            }

            ScreenMouseEvents.afterMouseClick(screen).register { _, event, handled ->
                ClientHooks.onScreenMouseClickedPost(screen, event, false, handled) // TODO: set double click
            }

            ScreenMouseEvents.allowMouseRelease(screen).register { _, event ->
                !ClientHooks.onScreenMouseReleasedPre(screen, event)
            }

            ScreenMouseEvents.afterMouseRelease(screen).register { _, event, handled ->
                ClientHooks.onScreenMouseReleasedPost(screen, event, handled)
            }

            ScreenMouseEvents.allowMouseScroll(screen).register { _, mouseX, mouseY, scrollX, scrollY ->
                !ClientHooks.onScreenMouseScrollPre(Minecraft.getInstance().mouseHandler, screen, scrollX, scrollY)
            }

            ScreenMouseEvents.afterMouseScroll(screen).register { _, mouseX, mouseY, scrollX, scrollY, handled ->
                ClientHooks.onScreenMouseScrollPost(Minecraft.getInstance().mouseHandler, screen, scrollX, scrollY)
                handled
            }

            ScreenKeyboardEvents.allowKeyPress(screen).register { _, event ->
                !ClientHooks.onScreenKeyPressedPre(screen, event)
            }

            ScreenKeyboardEvents.afterKeyPress(screen).register { _, event ->
                ClientHooks.onScreenKeyPressedPost(screen, event)
            }

            ScreenKeyboardEvents.allowKeyRelease(screen).register { _, event ->
                !ClientHooks.onScreenKeyReleasedPre(screen, event)
            }

            ScreenKeyboardEvents.afterKeyRelease(screen).register { _, event ->
                ClientHooks.onScreenKeyReleasedPost(screen, event)
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

        ClientTickEvents.START_LEVEL_TICK.register {
            EventHooks.fireLevelTickPre(it) { true }
        }

        ClientTickEvents.END_LEVEL_TICK.register {
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
//            if (shouldPostGeoLoaders) {
//                val map = GeometryLoaderManagerAccessor.getLoaders().toMutableMap()
//                val neoMap = mutableMapOf<Identifier, IGeometryLoader<*>>()
//                ModLoader.postEventWrapContainerInModOrder(ModelEvent.RegisterGeometryLoaders(neoMap))
//
//                map.putAll(neoMap)
//                GeometryLoaderManagerAccessor.setLoaders(ImmutableMap.copyOf(map))
//                GeometryLoaderManagerAccessor.setLoaderList(map.keys.joinToString(", ") { it.toString() })
//            }
        }
    }
}

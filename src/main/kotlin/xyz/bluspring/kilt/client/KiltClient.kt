package xyz.bluspring.kilt.client

import com.mojang.blaze3d.systems.RenderSystem
import dev.architectury.event.EventResult
import dev.architectury.event.events.client.ClientGuiEvent
import dev.architectury.event.events.client.ClientTooltipEvent
import io.github.fabricators_of_create.porting_lib.event.client.ParticleManagerRegistrationCallback
import io.github.fabricators_of_create.porting_lib.event.client.TextureStitchCallback
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Widget
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.client.event.*
import net.minecraftforge.client.gui.overlay.ForgeGui
import net.minecraftforge.client.gui.overlay.GuiOverlayManager
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.ForgeEventFactory
import net.minecraftforge.fml.ModLoader
import net.minecraftforge.fml.ModLoadingStage
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.mixin.ScreenAccessor
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer

class KiltClient : ClientModInitializer {
    override fun onInitializeClient() {
        registerFabricEvents()

        hasInitialized = true

        Kilt.loader.mods.forEach { mod ->
            mod.eventBus.post(FMLClientSetupEvent(mod, ModLoadingStage.SIDED_SETUP))
        }
    }

    private fun registerFabricEvents() {
        val mc = Minecraft.getInstance()

        ParticleManagerRegistrationCallback.EVENT.register {
            // i would call ForgeHooksClient.onRegisterParticleProviders,
            // but that doesn't work. i don't know why. but it just doesn't.
            Kilt.loader.postEvent(RegisterParticleProvidersEvent(Minecraft.getInstance().particleEngine))
        }

        ItemTooltipCallback.EVENT.register { stack, flag, components ->
            ForgeEventFactory.onItemTooltip(stack, null, components, flag)
        }

        ClientTooltipEvent.RENDER_MODIFY_COLOR.register { poseStack, x, y, ctx ->
            //val event = RenderTooltipEvent.Color(ItemStack.EMPTY, poseStack, x, y, null, ctx.backgroundColor, ctx.outlineGradientBottomColor, ctx.outlineGradientTopColor, null)
        }

        val add = AtomicReference<Consumer<GuiEventListener>>()

        ClientGuiEvent.INIT_PRE.register { screen, access ->
            add.set(Consumer<GuiEventListener> {
                if (it is Widget)
                    access.renderables.add(it)

                if (it is NarratableEntry)
                    access.narratables.add(it)

                (screen as ScreenAccessor).children.add(it)
            })

            if (MinecraftForge.EVENT_BUS.post(ScreenEvent.Init.Pre(screen, (screen as ScreenAccessor).children, add.get(), screen::callRemoveWidget)))
                EventResult.interruptFalse()
            else EventResult.pass()
        }

        ClientGuiEvent.INIT_POST.register { screen, _ ->
            MinecraftForge.EVENT_BUS.post(ScreenEvent.Init.Post(screen, (screen as ScreenAccessor).children, add.get(), screen::callRemoveWidget))
            add.set(null)
        }

        ClientGuiEvent.RENDER_CONTAINER_BACKGROUND.register { screen, poseStack, x, y, _ ->
            MinecraftForge.EVENT_BUS.post(ContainerScreenEvent.Render.Background(screen, poseStack, x, y))
        }

        ClientGuiEvent.RENDER_CONTAINER_FOREGROUND.register { screen, poseStack, x, y, _ ->
            MinecraftForge.EVENT_BUS.post(ContainerScreenEvent.Render.Foreground(screen, poseStack, x, y))
        }

        ClientGuiEvent.RENDER_PRE.register { screen, poseStack, x, y, delta ->
            if (MinecraftForge.EVENT_BUS.post(ScreenEvent.Render.Pre(screen, poseStack, x, y, delta)))
                EventResult.interruptFalse()
            else
                EventResult.pass()
        }

        // Have the Forge GUI sitting here, because one of the methods depends on it.
        // we're not using it properly though.
        val forgeGui = ForgeGui(mc)
        val window = mc.window

        ClientGuiEvent.RENDER_HUD.register { poseStack, delta ->
            val overlays = GuiOverlayManager.getOverlays()

            if (overlays.isEmpty())
                return@register

            forgeGui.screenWidth = window.screenWidth
            forgeGui.screenHeight = window.screenHeight
            forgeGui.random.setSeed(forgeGui.tickCount * 312871L)

            overlays.forEach { entry ->
                try {
                    val overlay = entry.overlay
                    if (MinecraftForge.EVENT_BUS.post(RenderGuiOverlayEvent.Pre(window, poseStack, delta, entry)))
                        return@forEach

                    overlay.render(forgeGui, poseStack, delta, forgeGui.screenWidth, forgeGui.screenHeight)

                    MinecraftForge.EVENT_BUS.post(RenderGuiOverlayEvent.Post(window, poseStack, delta, entry))
                } catch (e: Exception) {
                    Kilt.logger.error("Failed to render overlay ${entry.id}")
                    e.printStackTrace()
                }
            }

            RenderSystem.setShaderColor(1F, 1F, 1F, 1F)
        }

        ClientGuiEvent.RENDER_POST.register { screen, poseStack, x, y, delta ->
            if (screen != null)
                MinecraftForge.EVENT_BUS.post(ScreenEvent.Render.Post(screen, poseStack, x, y, delta))
        }

        TextureStitchCallback.PRE.register { atlas, consumer ->
            val map = mutableSetOf<ResourceLocation>()
            ModLoader.get().postEvent(TextureStitchEvent.Pre(atlas, map))

            map.forEach {
                consumer.accept(it)
            }
        }

        TextureStitchCallback.POST.register { atlas ->
            ModLoader.get().postEvent(TextureStitchEvent.Post(atlas))
        }
    }

    companion object {
        var hasInitialized = false
            private set
    }
}
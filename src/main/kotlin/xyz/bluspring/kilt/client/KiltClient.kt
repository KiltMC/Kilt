package xyz.bluspring.kilt.client

import dev.architectury.event.EventResult
import dev.architectury.event.events.client.ClientGuiEvent
import dev.architectury.event.events.client.ClientScreenInputEvent
import dev.architectury.event.events.client.ClientTooltipEvent
import io.github.fabricators_of_create.porting_lib.event.client.ParticleManagerRegistrationCallback
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Widget
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.world.item.ItemStack
import net.minecraftforge.client.event.RegisterParticleProvidersEvent
import net.minecraftforge.client.event.RenderTooltipEvent
import net.minecraftforge.client.event.ScreenEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.ForgeEventFactory
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.ModLoadingStage
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.mixin.ScreenAccessor
import xyz.bluspring.kilt.workarounds.ForgeHooksClientWorkaround
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

            if (!MinecraftForge.EVENT_BUS.post(ScreenEvent.Init.Pre(screen, (screen as ScreenAccessor).children, add.get(), screen::callRemoveWidget)))
                EventResult.interruptFalse()
            else EventResult.pass()
        }

        ClientGuiEvent.INIT_POST.register { screen, _ ->
            MinecraftForge.EVENT_BUS.post(ScreenEvent.Init.Post(screen, (screen as ScreenAccessor).children, add.get(), screen::callRemoveWidget))
            add.set(null)
        }

        ClientGuiEvent.RENDER_CONTAINER_BACKGROUND.register { screen, poseStack, _, _, _ ->
            MinecraftForge.EVENT_BUS.post(ScreenEvent.BackgroundRendered(screen, poseStack))
        }

        /*ClientGuiEvent.RENDER_CONTAINER_FOREGROUND.register { screen, poseStack, _, _, _ ->
            MinecraftForge.EVENT_BUS.post(ScreenEvent.BackgroundRendered(screen, poseStack))
        }*/

        ClientGuiEvent.RENDER_PRE.register { screen, poseStack, x, y, delta ->
            if (MinecraftForge.EVENT_BUS.post(ScreenEvent.Render.Pre(screen, poseStack, x, y, delta)))
                EventResult.interruptFalse()
            else
                EventResult.pass()
        }

        ClientGuiEvent.RENDER_POST.register { screen, poseStack, x, y, delta ->
            MinecraftForge.EVENT_BUS.post(ScreenEvent.Render.Post(screen, poseStack, x, y, delta))
        }
    }

    companion object {
        var hasInitialized = false
            private set
    }
}
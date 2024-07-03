package xyz.bluspring.kilt.client

import com.google.common.collect.ImmutableMap
import com.mojang.datafixers.util.Pair
import dev.architectury.event.EventResult
import dev.architectury.event.events.client.ClientGuiEvent
import dev.architectury.event.events.client.ClientTooltipEvent
import io.github.fabricators_of_create.porting_lib.event.client.InteractEvents
import io.github.fabricators_of_create.porting_lib.event.client.ParticleManagerRegistrationCallback
import io.github.fabricators_of_create.porting_lib.event.client.RenderHandCallback
import io.github.fabricators_of_create.porting_lib.event.client.TextureStitchCallback
import io.github.fabricators_of_create.porting_lib.models.geometry.RegisterGeometryLoadersCallback
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.renderer.ShaderInstance
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.InteractionResult
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import net.minecraftforge.client.ForgeHooksClient
import net.minecraftforge.client.event.*
import net.minecraftforge.client.gui.overlay.ForgeGui
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.ForgeEventFactory
import net.minecraftforge.fml.ModLoader
import net.minecraftforge.fml.ModLoadingContext
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.mixin.GeometryLoaderManagerAccessor
import xyz.bluspring.kilt.mixin.LevelRendererAccessor
import xyz.bluspring.kilt.mixin.ScreenAccessor
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer

@Suppress("removal")
class KiltClient : ClientModInitializer {
    override fun onInitializeClient() {
        registerFabricEvents()

        hasInitialized = true
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
                if (it is Renderable)
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
        forgeGui = ForgeGui(mc)

        ClientGuiEvent.RENDER_HUD.register { guiGraphics, delta ->
            forgeGui.render(guiGraphics, delta)
        }

        ClientGuiEvent.RENDER_POST.register { screen, poseStack, x, y, delta ->
            if (screen != null)
                MinecraftForge.EVENT_BUS.post(ScreenEvent.Render.Post(screen, poseStack, x, y, delta))
        }

        TextureStitchCallback.POST.register { atlas ->
            ModLoader.get().postEvent(TextureStitchEvent.Post(atlas))
        }

        WorldRenderEvents.AFTER_ENTITIES.register {
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
        }

        InteractEvents.USE.register { minecraft, hit, hand ->
            val player = minecraft.player

            when (hit.type) {
                HitResult.Type.BLOCK -> {
                    val result = ForgeHooks.onRightClickBlock(player, hand, (hit as BlockHitResult).blockPos, hit)

                    result.cancellationResult
                }

                HitResult.Type.MISS -> {
                    ForgeHooks.onItemRightClick(player, hand) ?: InteractionResult.PASS
                }

                HitResult.Type.ENTITY -> {
                    ForgeHooks.onInteractEntity(player, (hit as EntityHitResult).entity, hand) ?: InteractionResult.PASS
                }

                else -> throw IllegalStateException("this should be impossible.")
            }
        }

        InteractEvents.ATTACK.register { minecraft, hit ->
            val player = minecraft.player

            when (hit.type) {
                HitResult.Type.BLOCK -> {
                    val result = ForgeHooks.onLeftClickBlock(player, (hit as BlockHitResult).blockPos, hit.direction)

                    result.cancellationResult
                }

                HitResult.Type.ENTITY -> {
                    val result = ForgeHooks.onPlayerAttackTarget(minecraft.player, (hit as EntityHitResult).entity)

                    if (!result)
                        InteractionResult.FAIL
                    else
                        InteractionResult.PASS
                }

                HitResult.Type.MISS -> {
                    ForgeHooks.onEmptyLeftClick(player)

                    InteractionResult.PASS
                }

                else -> throw IllegalStateException("impossible")
            }
        }

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

        CoreShaderRegistrationCallback.EVENT.register {
            for (mod in Kilt.loader.mods) {
                ModLoadingContext.kiltActiveModId = mod.modId

                val shaderList = mutableListOf<Pair<ShaderInstance, Consumer<ShaderInstance>>>()
                val event = RegisterShadersEvent(Minecraft.getInstance().resourceManager, shaderList)
                mod.eventBus.post(event)

                for (pair in shaderList) {
                    val shader = pair.first
                    val consumer = pair.second

                    it.register(ResourceLocation.tryParse(shader.name)!!, shader.vertexFormat, consumer)
                }

                ModLoadingContext.kiltActiveModId = null
            }
        }

        RenderHandCallback.EVENT.register { event ->
            val forgeEvent = RenderHandEvent(event.hand, event.poseStack, event.multiBufferSource, event.packedLight, event.partialTicks, event.pitch, event.swingProgress, event.equipProgress, event.itemStack)
            MinecraftForge.EVENT_BUS.post(forgeEvent)

            if (forgeEvent.isCanceled)
                event.isCanceled = true
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
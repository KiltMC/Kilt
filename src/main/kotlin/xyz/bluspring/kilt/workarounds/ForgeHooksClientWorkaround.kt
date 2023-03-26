package xyz.bluspring.kilt.workarounds

import com.mojang.datafixers.util.Either
import net.minecraft.FileUtil
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.client.model.HumanoidModel
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.texture.TextureAtlas
import net.minecraft.client.resources.model.Material
import net.minecraft.locale.Language
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.inventory.tooltip.TooltipComponent
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.client.event.RenderTooltipEvent.GatherComponents
import net.minecraftforge.client.extensions.IForgeBakedModel
import net.minecraftforge.client.extensions.common.IClientItemExtensions
import net.minecraftforge.client.model.data.ModelData
import net.minecraftforge.common.MinecraftForge
import java.util.*
import java.util.function.Consumer
import java.util.stream.Stream


// this class pretty much has the ForgeHooksClient class ported to Kotlin
// whenever it's needed, because for some reason ForgeHooksClient itself doesn't actually function.
object ForgeHooksClientWorkaround {
    private val guiLayers = Stack<Screen>()

    @JvmStatic
    fun resizeGuiLayers(minecraft: Minecraft, width: Int, height: Int) {
        guiLayers.forEach(Consumer { screen: Screen ->
            screen.resize(
                minecraft,
                width,
                height
            )
        })
    }

    @JvmStatic
    fun clearGuiLayers(minecraft: Minecraft) {
        while (guiLayers.size > 0)
            popGuiLayerInternal(minecraft)
    }

    private fun popGuiLayerInternal(minecraft: Minecraft) {
        if (minecraft.screen != null) minecraft.screen!!.removed()
        minecraft.screen = guiLayers.pop()
    }

    @JvmStatic
    fun pushGuiLayer(minecraft: Minecraft, screen: Screen) {
        if (minecraft.screen != null)
            guiLayers.push(minecraft.screen)
        minecraft.screen = Objects.requireNonNull(screen)
        screen.init(minecraft, minecraft.window.guiScaledWidth, minecraft.window.guiScaledHeight)
        minecraft.narrator.sayNow(screen.narrationMessage)
    }

    @JvmStatic
    fun popGuiLayer(minecraft: Minecraft) {
        if (guiLayers.size == 0) {
            minecraft.setScreen(null)
            return
        }
        popGuiLayerInternal(minecraft)
        if (minecraft.screen != null) minecraft.narrator.sayNow(minecraft.screen!!.narrationMessage)
    }

    @JvmStatic
    fun getGuiFarPlane(): Float {
        // 1000 units for the overlay background,
        // and 2000 units for each layered Screen,
        return 1000.0f + 2000.0f * (1 + guiLayers.size)
    }

    @JvmStatic
    fun <T : LivingEntity?> copyModelProperties(original: HumanoidModel<T>, replacement: HumanoidModel<*>) {
        // this function does not make use of the <T> generic, so the unchecked cast should be safe
        original.copyPropertiesTo(replacement as HumanoidModel<T>)
        replacement.head.visible = original.head.visible
        replacement.hat.visible = original.hat.visible
        replacement.body.visible = original.body.visible
        replacement.rightArm.visible = original.rightArm.visible
        replacement.leftArm.visible = original.leftArm.visible
        replacement.rightLeg.visible = original.rightLeg.visible
        replacement.leftLeg.visible = original.leftLeg.visible
    }

    @JvmStatic
    fun isBlockInSolidLayer(state: BlockState): Boolean {
        val model = Minecraft.getInstance().blockRenderer.getBlockModel(state) as IForgeBakedModel
        return model.getRenderTypes(state, RandomSource.create(), ModelData.EMPTY).contains(RenderType.solid())
    }

    @JvmStatic
    fun gatherTooltipComponents(
        stack: ItemStack?,
        textElements: List<FormattedText>,
        mouseX: Int,
        screenWidth: Int,
        screenHeight: Int,
        forcedFont: Font?,
        fallbackFont: Font
    ): List<ClientTooltipComponent?>? {
        return gatherTooltipComponents(
            stack,
            textElements,
            Optional.empty(),
            mouseX,
            screenWidth,
            screenHeight,
            forcedFont,
            fallbackFont
        )
    }

    @JvmStatic
    fun gatherTooltipComponents(
        stack: ItemStack?,
        textElements: List<FormattedText>,
        itemComponent: Optional<TooltipComponent>,
        mouseX: Int,
        screenWidth: Int,
        screenHeight: Int,
        forcedFont: Font?,
        fallbackFont: Font
    ): List<ClientTooltipComponent> {
        val font: Font = getTooltipFont(forcedFont, stack!!, fallbackFont)
        val elements = textElements.map { Either.left<FormattedText, TooltipComponent>(it) }.toMutableList()

        itemComponent.ifPresent { c -> elements.add(1, Either.right(c)) }
        val event = GatherComponents(stack, screenWidth, screenHeight, elements, -1)
        MinecraftForge.EVENT_BUS.post(event)
        if (event.isCanceled) return listOf()

        // text wrapping
        var tooltipTextWidth = event.tooltipElements.stream()
            .mapToInt { either: Either<FormattedText, TooltipComponent> ->
                either.map(font::width
                ) { _ -> 0 }
            }
            .max()
            .orElse(0)

        var needsWrap = false
        var tooltipX = mouseX + 12
        if (tooltipX + tooltipTextWidth + 4 > screenWidth) {
            tooltipX = mouseX - 16 - tooltipTextWidth
            if (tooltipX < 4) // if the tooltip doesn't fit on the screen
            {
                tooltipTextWidth = if (mouseX > screenWidth / 2) mouseX - 12 - 8 else screenWidth - 16 - mouseX
                needsWrap = true
            }
        }
        if (event.maxWidth in 1 until tooltipTextWidth) {
            tooltipTextWidth = event.maxWidth
            needsWrap = true
        }
        val tooltipTextWidthF = tooltipTextWidth
        return if (needsWrap) {
            event.tooltipElements.flatMap { either ->
                either.map({ text ->
                    font.split(text, tooltipTextWidthF).map { ClientTooltipComponent.create(it) }.toList()
                }, {
                    Stream.of(ClientTooltipComponent.create(it)).toList()
                })
            }.toList()
        } else {
            event.tooltipElements
                .map { either ->
                    either.map(
                        { text: FormattedText ->
                            ClientTooltipComponent.create(
                                if (text is Component)
                                    text.visualOrderText
                                else
                                    Language.getInstance().getVisualOrder(text)
                            )
                        }
                    ) { tooltipComponent ->
                        ClientTooltipComponent.create(tooltipComponent)
                    }
                }
                .toList()
        }
    }

    @JvmStatic
    fun getTooltipFont(forcedFont: Font?, stack: ItemStack, fallbackFont: Font): Font {
        if (forcedFont != null)
            return forcedFont

        val stackFont = IClientItemExtensions.of(stack).getFont(stack, IClientItemExtensions.FontContext.TOOLTIP)
        return stackFont ?: fallbackFont
    }

    @JvmStatic
    fun getShaderImportLocation(basePath: String, isRelative: Boolean, importPath: String): ResourceLocation {
        val loc = ResourceLocation(importPath)
        val normalized = FileUtil.normalizeResourcePath(
            "${if (isRelative) basePath else "shaders/include/"}${loc.path}"
        )

        return ResourceLocation(loc.namespace, normalized)
    }

    @JvmField
    var forgeStatusLine = ""

    @JvmStatic
    fun getBlockMaterial(loc: ResourceLocation): Material {
        return Material(TextureAtlas.LOCATION_BLOCKS, loc)
    }
}
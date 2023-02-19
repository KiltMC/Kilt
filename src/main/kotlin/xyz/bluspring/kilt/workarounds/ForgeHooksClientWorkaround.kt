package xyz.bluspring.kilt.workarounds

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.model.HumanoidModel
import net.minecraft.client.renderer.RenderType
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.client.extensions.IForgeBakedModel
import net.minecraftforge.client.model.data.ModelData
import java.util.*
import java.util.function.Consumer

// this class pretty much has the entire
object ForgeHooksClientWorkaround {
    private val guiLayers = Stack<Screen>()

    @JvmStatic
    fun resizeGuiLayers(minecraft: Minecraft?, width: Int, height: Int) {
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
}
package xyz.bluspring.kilt.compat.fabric.geckolib

import net.minecraft.client.model.HumanoidModel
import net.minecraft.client.model.Model
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions
import software.bernie.geckolib.animatable.client.RenderProvider
import java.util.*

class ForgeRenderProvider(private val extensions: IClientItemExtensions) : RenderProvider {
    override fun getCustomRenderer(): BlockEntityWithoutLevelRenderer {
        return extensions.customRenderer
    }

    override fun getGenericArmorModel(
        livingEntity: LivingEntity?,
        itemStack: ItemStack?,
        equipmentSlot: EquipmentSlot?,
        original: HumanoidModel<LivingEntity?>?
    ): Model? {
        return extensions.getGenericArmorModel(livingEntity, itemStack, equipmentSlot, original)
    }

    override fun getHumanoidArmorModel(
        livingEntity: LivingEntity?,
        itemStack: ItemStack?,
        equipmentSlot: EquipmentSlot?,
        original: HumanoidModel<LivingEntity?>?
    ): HumanoidModel<LivingEntity> {
        return extensions.getHumanoidArmorModel(livingEntity, itemStack, equipmentSlot, original) as HumanoidModel<LivingEntity>
    }

    companion object {
        val forgeRenderProviders: MutableMap<IClientItemExtensions, ForgeRenderProvider> = Collections.synchronizedMap(mutableMapOf())

        fun get(extensions: IClientItemExtensions): ForgeRenderProvider {
            return forgeRenderProviders.computeIfAbsent(extensions) { ForgeRenderProvider(it) }
        }
    }
}
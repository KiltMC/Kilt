package xyz.bluspring.kilt.interop.transfer

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import team.reborn.energy.api.EnergyStorage
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.interop.transfer.energy.FabricEnergyItemStorageCapabilityProvider
import xyz.bluspring.kilt.interop.transfer.energy.FabricEnergyStorageCapability
import xyz.bluspring.kilt.interop.transfer.energy.FabricEnergyStorageCapabilityProvider
import xyz.bluspring.kilt.interop.transfer.energy.ForgeEnergyStorage
import xyz.bluspring.kilt.interop.transfer.fluid.*
import xyz.bluspring.kilt.interop.transfer.item.FabricItemStorageCapability
import xyz.bluspring.kilt.interop.transfer.item.FabricItemStorageCapabilityProvider
import xyz.bluspring.kilt.interop.transfer.item.ForgeSlottedStorage

object TransferInterop {
    fun init() {
        MinecraftForge.EVENT_BUS.register(::onAttachBlockEntityCapabilities)
        MinecraftForge.EVENT_BUS.register(::onAttachItemStackCapabilities)

        ItemStorage.SIDED.registerFallback { world, pos, state, blockEntity, direction ->
            if (blockEntity == null)
                return@registerFallback null

            val itemHandlerCapability = (blockEntity as ICapabilityProvider).getCapability(ForgeCapabilities.ITEM_HANDLER, direction)

            if (itemHandlerCapability.isPresent) {
                val handler = itemHandlerCapability.resolve().get()
                if (handler !is FabricItemStorageCapability) {
                    return@registerFallback ForgeSlottedStorage(handler)
                }
            }

            null
        }

        FluidStorage.SIDED.registerFallback { world, pos, state, blockEntity, direction ->
            if (blockEntity == null)
                return@registerFallback null

            val fluidHandlerCapability = (blockEntity as ICapabilityProvider).getCapability(ForgeCapabilities.FLUID_HANDLER, direction)

            if (fluidHandlerCapability.isPresent) {
                val handler = fluidHandlerCapability.resolve().get()
                if (handler !is FabricFluidStorageCapability) {
                    return@registerFallback ForgeFluidStorage(handler)
                }
            }

            null
        }

        FluidStorage.ITEM.registerFallback { itemStack, context ->
            if (itemStack == null || itemStack.isEmpty)
                return@registerFallback null

            val fluidHandlerCapability = (itemStack as ICapabilityProvider).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)

            if (fluidHandlerCapability.isPresent) {
                val handler = fluidHandlerCapability.resolve().get()
                if (handler !is FabricFluidItemStorageCapability) {
                    return@registerFallback ForgeFluidStorage(handler)
                }
            }

            null
        }

        EnergyStorage.SIDED.registerFallback { world, pos, state, blockEntity, direction ->
            if (blockEntity == null)
                return@registerFallback null

            val energyHandlerCapability = (blockEntity as ICapabilityProvider).getCapability(ForgeCapabilities.ENERGY, direction)

            if (energyHandlerCapability.isPresent) {
                val handler = energyHandlerCapability.resolve().get()
                if (handler !is FabricEnergyStorageCapability) {
                    return@registerFallback ForgeEnergyStorage(handler)
                }
            }

            null
        }

        EnergyStorage.ITEM.registerFallback { itemStack, context ->
            if (itemStack == null || itemStack.isEmpty)
                return@registerFallback null

            val energyHandlerCapability = (itemStack as ICapabilityProvider).getCapability(ForgeCapabilities.ENERGY)

            if (energyHandlerCapability.isPresent) {
                val handler = energyHandlerCapability.resolve().get()
                if (handler !is FabricEnergyStorageCapability) {
                    return@registerFallback ForgeEnergyStorage(handler)
                }
            }

            null
        }
    }

    @SubscribeEvent
    fun onAttachBlockEntityCapabilities(event: AttachCapabilitiesEvent<BlockEntity>) {
        val blockEntity = event.`object`
        event.addCapability(ResourceLocation(Kilt.MOD_ID, "fabric_item_storage"), FabricItemStorageCapabilityProvider(blockEntity))
        event.addCapability(ResourceLocation(Kilt.MOD_ID, "fabric_fluid_storage"), FabricFluidStorageCapabilityProvider(blockEntity))
        event.addCapability(ResourceLocation(Kilt.MOD_ID, "fabric_energy_storage"), FabricEnergyStorageCapabilityProvider(blockEntity))
    }

    @SubscribeEvent
    fun onAttachItemStackCapabilities(event: AttachCapabilitiesEvent<ItemStack>) {
        val stack = event.`object`
        event.addCapability(ResourceLocation(Kilt.MOD_ID, "fabric_fluid_item_storage"), FabricFluidItemStorageCapabilityProvider(stack))
        event.addCapability(ResourceLocation(Kilt.MOD_ID, "fabric_energy_item_storage"), FabricEnergyItemStorageCapabilityProvider(stack))
    }
}
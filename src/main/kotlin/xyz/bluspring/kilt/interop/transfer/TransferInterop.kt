package xyz.bluspring.kilt.interop.transfer

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.injections.capabilities.BlockEntityCapabilityProviderImpl
import xyz.bluspring.kilt.injections.capabilities.ItemStackCapabilityProviderImpl
import xyz.bluspring.kilt.interop.transfer.fluid.FabricFluidItemStorageCapabilityProvider
import xyz.bluspring.kilt.interop.transfer.fluid.FabricFluidStorageCapability
import xyz.bluspring.kilt.interop.transfer.fluid.FabricFluidStorageCapabilityProvider
import xyz.bluspring.kilt.interop.transfer.fluid.ForgeFluidStorage
import xyz.bluspring.kilt.interop.transfer.item.FabricItemStorageCapability
import xyz.bluspring.kilt.interop.transfer.item.FabricItemStorageCapabilityProvider
import xyz.bluspring.kilt.interop.transfer.item.ForgeSlottedStorage

object TransferInterop {
    fun init() {
        MinecraftForge.EVENT_BUS.register(::onAttachBlockEntityItemCapabilities)
        MinecraftForge.EVENT_BUS.register(::onAttachBlockEntityFluidCapabilities)
        MinecraftForge.EVENT_BUS.register(::onAttachItemStackFluidCapabilities)

        ItemStorage.SIDED.registerFallback { world, pos, state, blockEntity, direction ->
            if (blockEntity == null)
                return@registerFallback null

            val itemHandlerCapability = (blockEntity as BlockEntityCapabilityProviderImpl).getCapability(ForgeCapabilities.ITEM_HANDLER, direction)

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

            val fluidHandlerCapability = (blockEntity as BlockEntityCapabilityProviderImpl).getCapability(ForgeCapabilities.FLUID_HANDLER, direction)

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

            val fluidHandlerCapability = (itemStack as ItemStackCapabilityProviderImpl).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)

            if (fluidHandlerCapability.isPresent) {
                val handler = fluidHandlerCapability.resolve().get()
                if (true) { // TODO: verify if FabricFluidItemStorageCapability
                    return@registerFallback ForgeFluidStorage(handler)
                }
            }

            null
        }
    }

    @SubscribeEvent
    fun onAttachBlockEntityItemCapabilities(event: AttachCapabilitiesEvent<BlockEntity>) {
        val blockEntity = event.`object`
        event.addCapability(ResourceLocation(Kilt.MOD_ID, "fabric_item_storage"), FabricItemStorageCapabilityProvider(blockEntity))
    }

    @SubscribeEvent
    fun onAttachBlockEntityFluidCapabilities(event: AttachCapabilitiesEvent<BlockEntity>) {
        val blockEntity = event.`object`
        event.addCapability(ResourceLocation(Kilt.MOD_ID, "fabric_fluid_storage"), FabricFluidStorageCapabilityProvider(blockEntity))
    }

    @SubscribeEvent
    fun onAttachItemStackFluidCapabilities(event: AttachCapabilitiesEvent<ItemStack>) {
        val stack = event.`object`
        event.addCapability(ResourceLocation(Kilt.MOD_ID, "fabric_fluid_item_storage"), FabricFluidItemStorageCapabilityProvider(stack))
    }
}
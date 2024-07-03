package xyz.bluspring.kilt.interop.transfer

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.injections.capabilities.BlockEntityCapabilityProviderImpl
import xyz.bluspring.kilt.interop.transfer.item.FabricItemStorageCapability
import xyz.bluspring.kilt.interop.transfer.item.FabricItemStorageCapabilityProvider
import xyz.bluspring.kilt.interop.transfer.item.ForgeSlottedStorage

object TransferInterop {
    fun init() {
        MinecraftForge.EVENT_BUS.register(::onAttachBlockEntityCapabilities)

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
    }

    @SubscribeEvent
    fun onAttachBlockEntityCapabilities(event: AttachCapabilitiesEvent<BlockEntity>) {
        val blockEntity = event.`object`
        val fabricStorage = ItemStorage.SIDED.getProvider(blockEntity.blockState.block) ?: return
        val storage = fabricStorage.find(blockEntity.level!!, blockEntity.blockPos, blockEntity.blockState, blockEntity, null) ?: return

        // Forge's transfer API is effectively slot-based, so let's respect that.
        if (storage !is SlottedStorage<ItemVariant> || storage is ForgeSlottedStorage)
            return

        event.addCapability(ResourceLocation(Kilt.MOD_ID, "fabric_item_storage"), FabricItemStorageCapabilityProvider(storage))
    }
}
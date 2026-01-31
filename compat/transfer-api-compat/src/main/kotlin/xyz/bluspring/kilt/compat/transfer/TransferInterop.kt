package xyz.bluspring.kilt.compat.transfer

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemItemStorages
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.common.NeoForge
import team.reborn.energy.api.EnergyStorage
import xyz.bluspring.kilt.compat.transfer.energy.FabricEnergyStorageCapability
import xyz.bluspring.kilt.compat.transfer.energy.ForgeEnergyStorage
import xyz.bluspring.kilt.compat.transfer.fluid.FabricFluidItemStorageCapability
import xyz.bluspring.kilt.compat.transfer.fluid.FabricFluidStorageCapability
import xyz.bluspring.kilt.compat.transfer.fluid.ForgeFluidStorage
import xyz.bluspring.kilt.compat.transfer.item.FabricItemStorageCapability
import xyz.bluspring.kilt.compat.transfer.item.ForgeSlottedStorage

class TransferInterop : ModInitializer {
    override fun onInitialize() {
        NeoForge.EVENT_BUS.register(this)

        ItemStorage.SIDED.registerFallback { world, pos, state, blockEntity, direction ->
            if (blockEntity == null)
                return@registerFallback null

            val handler = Capabilities.ItemHandler.BLOCK.getCapability(world, pos, state, blockEntity, direction)

            if (handler != null && handler !is FabricItemStorageCapability) {
                return@registerFallback ForgeSlottedStorage(handler)
            }

            null
        }

        ItemItemStorages.ITEM.registerFallback { stack, context ->
            if (stack == null)
                return@registerFallback null

            val handler = Capabilities.ItemHandler.ITEM.getCapability(stack, null)

            if (handler != null && handler !is FabricItemStorageCapability) {
                return@registerFallback ForgeSlottedStorage(handler)
            }

            null
        }

        FluidStorage.SIDED.registerFallback { world, pos, state, blockEntity, direction ->
            if (blockEntity == null)
                return@registerFallback null

            val handler = Capabilities.FluidHandler.BLOCK.getCapability(world, pos, state, blockEntity, direction)

            if (handler != null && handler !is FabricFluidStorageCapability) {
                return@registerFallback ForgeFluidStorage(handler)
            }

            null
        }

        FluidStorage.ITEM.registerFallback { itemStack, context ->
            if (itemStack == null || itemStack.isEmpty)
                return@registerFallback null

            val handler = Capabilities.FluidHandler.ITEM.getCapability(itemStack, null)

            if (handler != null && handler !is FabricFluidItemStorageCapability) {
                return@registerFallback ForgeFluidStorage(handler)
            }

            null
        }

        EnergyStorage.SIDED.registerFallback { world, pos, state, blockEntity, direction ->
            if (blockEntity == null)
                return@registerFallback null

            val handler = Capabilities.EnergyStorage.BLOCK.getCapability(world, pos, state, blockEntity, direction)

            if (handler != null && handler !is FabricEnergyStorageCapability) {
                return@registerFallback ForgeEnergyStorage(handler)
            }

            null
        }

        EnergyStorage.ITEM.registerFallback { itemStack, context ->
            if (itemStack == null || itemStack.isEmpty)
                return@registerFallback null

            val handler = Capabilities.EnergyStorage.ITEM.getCapability(itemStack, null)

            if (handler != null && handler !is FabricEnergyStorageCapability) {
                return@registerFallback ForgeEnergyStorage(handler)
            }

            null
        }
    }

    /*@SubscribeEvent
    fun onAttachBlockEntityCapabilities(event: AttachCapabilitiesEvent<BlockEntity>) {
        val blockEntity = event.`object`
        if (ItemStorage.SIDED.getProvider(blockEntity.blockState.block) != null)
            event.addCapability(ResourceLocation(Kilt.MOD_ID, "fabric_item_storage"),
                FabricItemStorageCapabilityProvider(blockEntity)
            )

        if (FluidStorage.SIDED.getProvider(blockEntity.blockState.block) != null)
            event.addCapability(ResourceLocation(Kilt.MOD_ID, "fabric_fluid_storage"),
                FabricFluidStorageCapabilityProvider(blockEntity)
            )

        if (EnergyStorage.SIDED.getProvider(blockEntity.blockState.block) != null)
            event.addCapability(ResourceLocation(Kilt.MOD_ID, "fabric_energy_storage"),
                FabricEnergyStorageCapabilityProvider(blockEntity)
            )
    }

    @SubscribeEvent
    fun onAttachItemStackCapabilities(event: AttachCapabilitiesEvent<ItemStack>) {
        val stack = event.`object`
        if (ItemItemStorages.ITEM.getProvider(stack.item) != null)
            event.addCapability(ResourceLocation(Kilt.MOD_ID, "fabric_item_item_storage"),
                FabricItemItemStorageCapabilityProvider(stack)
            )

        if (FluidStorage.ITEM.getProvider(stack.item) != null)
            event.addCapability(ResourceLocation(Kilt.MOD_ID, "fabric_fluid_item_storage"),
                FabricFluidItemStorageCapabilityProvider(stack)
            )

        if (EnergyStorage.ITEM.getProvider(stack.item) != null)
            event.addCapability(ResourceLocation(Kilt.MOD_ID, "fabric_energy_item_storage"),
                FabricEnergyItemStorageCapabilityProvider(stack)
            )
    }*/

    companion object {
        const val REBORN_ENERGY_TO_FORGE_ENERGY = 10 // 1 E -> 10 FE, matching Connector Extras
    }
}
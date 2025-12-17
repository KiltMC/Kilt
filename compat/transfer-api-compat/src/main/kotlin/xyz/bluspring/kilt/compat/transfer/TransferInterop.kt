package xyz.bluspring.kilt.compat.transfer

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemItemStorages
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.common.capabilities.ForgeCapabilities
import net.neoforged.neoforge.common.capabilities.ICapabilityProvider
import net.neoforged.neoforge.event.AttachCapabilitiesEvent
import net.neoforged.bus.api.SubscribeEvent
import team.reborn.energy.api.EnergyStorage
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.compat.transfer.energy.FabricEnergyItemStorageCapabilityProvider
import xyz.bluspring.kilt.compat.transfer.energy.FabricEnergyStorageCapability
import xyz.bluspring.kilt.compat.transfer.energy.FabricEnergyStorageCapabilityProvider
import xyz.bluspring.kilt.compat.transfer.energy.ForgeEnergyStorage
import xyz.bluspring.kilt.compat.transfer.fluid.*
import xyz.bluspring.kilt.compat.transfer.item.FabricItemItemStorageCapabilityProvider
import xyz.bluspring.kilt.compat.transfer.item.FabricItemStorageCapability
import xyz.bluspring.kilt.compat.transfer.item.FabricItemStorageCapabilityProvider
import xyz.bluspring.kilt.compat.transfer.item.ForgeSlottedStorage

class TransferInterop : ModInitializer {
    override fun onInitialize() {
        NeoForge.EVENT_BUS.register(this)

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

        ItemItemStorages.ITEM.registerFallback { stack, context ->
            if (stack == null)
                return@registerFallback null

            val itemHandlerCapability = stack.getCapability(ForgeCapabilities.ITEM_HANDLER)

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
    }

    companion object {
        val REBORN_ENERGY_TO_FORGE_ENERGY = 10 // 1 E -> 10 FE, matching Connector Extras
    }
}
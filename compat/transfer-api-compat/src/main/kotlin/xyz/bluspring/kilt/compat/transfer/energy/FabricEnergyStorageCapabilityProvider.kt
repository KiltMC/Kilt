package xyz.bluspring.kilt.compat.transfer.energy

//class FabricEnergyStorageCapabilityProvider(val blockEntity: BlockEntity) : ICapabilityProvider {
//    private val capabilityCache = Collections.synchronizedMap(mutableMapOf<EnergyStorage, FabricEnergyStorageCapability>())
//
//    override fun <T : Any?> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
//        if (cap == ForgeCapabilities.ENERGY) {
//            val fabricStorage = EnergyStorage.SIDED.getProvider(blockEntity.blockState.block) ?: return LazyOptional.empty()
//            val storage = fabricStorage.find(blockEntity.level!!, blockEntity.blockPos, blockEntity.blockState, blockEntity, side) ?: return LazyOptional.empty()
//
//            // Ignore our own storage
//            if (storage is ForgeEnergyStorage)
//                return LazyOptional.empty()
//
//            return LazyOptional.of { capabilityCache.computeIfAbsent(storage) { s -> FabricEnergyStorageCapability(s) } }.cast()
//        }
//
//        return LazyOptional.empty()
//    }
//}
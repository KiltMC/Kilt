package xyz.bluspring.kilt.compat.transfer.fluid

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.fluids.capability.IFluidHandler
import xyz.bluspring.kilt.util.neoforge.fluid.FluidTransferUtils.toDroplets
import xyz.bluspring.kilt.util.neoforge.fluid.FluidTransferUtils.toMillibuckets

class NeoForgeFluidStorage(val handler: IFluidHandler) : Storage<FluidVariant> {
    override fun iterator(): MutableIterator<StorageView<FluidVariant>> {
        // TODO: make this an actually good iterator
        return object : MutableIterator<StorageView<FluidVariant>> {
            var currentIndex = 0

            override fun hasNext(): Boolean {
                return currentIndex + 1 < handler.tanks
            }

            override fun next(): StorageView<FluidVariant> {
                return NeoForgeFluidTankStorage(currentIndex++)
            }

            override fun remove() {
                currentIndex++
            }
        }
    }

    override fun extract(resource: FluidVariant, maxAmount: Long, transaction: TransactionContext): Long {
        val fluidStack = FluidStack(resource.fluid.builtInRegistryHolder(), maxAmount.toMillibuckets(), resource.components)

        val snapshot = NeoForgeFluidStackSnapshot(fluidStack, false)
        snapshot.updateSnapshots(transaction)
        val drained = handler.drain(fluidStack, IFluidHandler.FluidAction.SIMULATE)

        return drained.amount.toDroplets()
    }

    override fun insert(resource: FluidVariant, maxAmount: Long, transaction: TransactionContext): Long {
        val fluidStack = FluidStack(resource.fluid.builtInRegistryHolder(), maxAmount.toMillibuckets(), resource.components)

        val snapshot = NeoForgeFluidStackSnapshot(fluidStack, true)
        snapshot.updateSnapshots(transaction)

        val filled = handler.fill(fluidStack, IFluidHandler.FluidAction.SIMULATE)

        return filled.toDroplets()
    }

    private inner class NeoForgeFluidStackSnapshot(var stack: FluidStack, val insert: Boolean) : SnapshotParticipant<FluidStack>() {
        override fun createSnapshot(): FluidStack {
            return stack
        }

        override fun readSnapshot(snapshot: FluidStack) {
            stack = snapshot
        }

        override fun onFinalCommit() {
            if (insert) {
                handler.fill(stack, IFluidHandler.FluidAction.EXECUTE)
            } else {
                handler.drain(stack, IFluidHandler.FluidAction.EXECUTE)
            }
        }
    }

    private inner class NeoForgeFluidTankStorage(val tank: Int) : StorageView<FluidVariant> {
        override fun extract(resource: FluidVariant, maxAmount: Long, transaction: TransactionContext): Long {
            val stack = FluidStack(resource.fluid.builtInRegistryHolder(), maxAmount.toMillibuckets().coerceAtMost(this.amount.toMillibuckets()), resource.components)

            if (!handler.isFluidValid(tank, stack))
                return 0L

            val snapshot = NeoForgeFluidStackSnapshot(stack, false)
            snapshot.updateSnapshots(transaction)

            val extracted = handler.drain(stack, IFluidHandler.FluidAction.SIMULATE)

            return extracted.amount.toDroplets()
        }

        override fun isResourceBlank(): Boolean {
            return handler.getFluidInTank(tank).isEmpty
        }

        override fun getResource(): FluidVariant {
            val stack = handler.getFluidInTank(tank)
            return FluidVariant.of(stack.fluid, stack.componentsPatch)
        }

        override fun getAmount(): Long {
            return handler.getFluidInTank(tank).amount.toDroplets()
        }

        override fun getCapacity(): Long {
            return handler.getTankCapacity(tank).toDroplets()
        }

    }
}

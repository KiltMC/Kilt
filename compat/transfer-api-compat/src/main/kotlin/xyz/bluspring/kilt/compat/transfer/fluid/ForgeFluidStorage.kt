package xyz.bluspring.kilt.compat.transfer.fluid

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler
import xyz.bluspring.kilt.compat.transfer.fluid.FluidTransferUtils.toDroplets
import xyz.bluspring.kilt.compat.transfer.fluid.FluidTransferUtils.toMillibuckets

class ForgeFluidStorage(val handler: IFluidHandler) : Storage<FluidVariant> {
    override fun iterator(): MutableIterator<StorageView<FluidVariant>> {
        // TODO: make this an actually good iterator
        return object : MutableIterator<StorageView<FluidVariant>> {
            var currentIndex = 0

            override fun hasNext(): Boolean {
                return currentIndex + 1 < handler.tanks
            }

            override fun next(): StorageView<FluidVariant> {
                return ForgeFluidTankStorage(currentIndex++)
            }

            override fun remove() {
                currentIndex++
            }
        }
    }

    override fun extract(resource: FluidVariant, maxAmount: Long, transaction: TransactionContext): Long {
        val fluidStack = FluidStack(resource.fluid, maxAmount.toMillibuckets(), resource.nbt)

        val snapshot = ForgeFluidStackSnapshot(fluidStack, false)
        snapshot.updateSnapshots(transaction)
        val drained = handler.drain(fluidStack, IFluidHandler.FluidAction.SIMULATE)

        return drained.amount.toDroplets()
    }

    override fun insert(resource: FluidVariant, maxAmount: Long, transaction: TransactionContext): Long {
        val fluidStack = FluidStack(resource.fluid, maxAmount.toMillibuckets(), resource.nbt)

        val snapshot = ForgeFluidStackSnapshot(fluidStack, true)
        snapshot.updateSnapshots(transaction)

        val filled = handler.fill(fluidStack, IFluidHandler.FluidAction.SIMULATE)

        return filled.toDroplets()
    }

    private inner class ForgeFluidStackSnapshot(var stack: FluidStack, val insert: Boolean) : SnapshotParticipant<FluidStack>() {
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

    private inner class ForgeFluidTankStorage(val tank: Int) : StorageView<FluidVariant> {
        override fun extract(resource: FluidVariant, maxAmount: Long, transaction: TransactionContext): Long {
            val stack = FluidStack(resource.fluid, maxAmount.toMillibuckets().coerceAtMost(this.amount.toMillibuckets()), resource.nbt)

            if (!handler.isFluidValid(tank, stack))
                return 0L

            val snapshot = ForgeFluidStackSnapshot(stack, false)
            snapshot.updateSnapshots(transaction)

            val extracted = handler.drain(stack, IFluidHandler.FluidAction.SIMULATE)

            return extracted.amount.toDroplets()
        }

        override fun isResourceBlank(): Boolean {
            return handler.getFluidInTank(tank).isEmpty
        }

        override fun getResource(): FluidVariant {
            val stack = handler.getFluidInTank(tank)
            return FluidVariant.of(stack.fluid, stack.tag)
        }

        override fun getAmount(): Long {
            return handler.getFluidInTank(tank).amount.toDroplets()
        }

        override fun getCapacity(): Long {
            return handler.getTankCapacity(tank).toDroplets()
        }

    }
}
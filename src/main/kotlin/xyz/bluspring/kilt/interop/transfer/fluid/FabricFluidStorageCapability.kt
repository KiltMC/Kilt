package xyz.bluspring.kilt.interop.transfer.fluid

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler

open class FabricFluidStorageCapability(val storage: Storage<FluidVariant>) : IFluidHandler {
    override fun getTanks(): Int {
        return storage.toList().size
    }

    override fun getFluidInTank(tank: Int): FluidStack {
        val view = storage.toList()[tank]
        return FluidStack(view.resource.fluid, view.amount.toInt(), view.resource.nbt)
    }

    override fun getTankCapacity(tank: Int): Int {
        return storage.toList()[tank].capacity.toInt()
    }

    override fun isFluidValid(tank: Int, stack: FluidStack): Boolean {
        // TODO: is this correct?
        val view = storage.toList()[tank]
        return view.resource == stack.fluid && view.resource.nbtMatches(stack.tag)
    }

    override fun fill(resource: FluidStack, action: IFluidHandler.FluidAction): Int {
        TransferUtil.getTransaction().use { transaction ->
            val inserted = storage.insert(FluidVariant.of(resource.fluid, resource.tag), resource.amount, transaction)

            if (action == IFluidHandler.FluidAction.EXECUTE)
                transaction.commit()
            else
                transaction.abort()

            return inserted.toInt()
        }
    }

    override fun drain(resource: FluidStack, action: IFluidHandler.FluidAction): FluidStack {
        TransferUtil.getTransaction().use { transaction ->
            val extracted = storage.extract(FluidVariant.of(resource.fluid, resource.tag), resource.amount, transaction)

            if (action == IFluidHandler.FluidAction.EXECUTE)
                transaction.commit()
            else
                transaction.abort()

            return FluidStack(resource.fluid, extracted.toInt(), resource.tag)
        }
    }

    override fun drain(maxDrain: Int, action: IFluidHandler.FluidAction): FluidStack {
        TransferUtil.getTransaction().use { transaction ->
            var totalToDrain = maxDrain.toLong()
            var selectedStack = FluidStack.EMPTY
            for (view in storage.toList()) {
                if (selectedStack.isEmpty && !view.isResourceBlank) {
                    selectedStack = FluidStack(view.resource.fluid, 0, view.resource.nbt)
                }

                if (view.resource.fluid != selectedStack.fluid || !view.resource.nbtMatches(selectedStack.tag))
                    continue

                val totalExtracted = storage.extract(FluidVariant.of(selectedStack.fluid, selectedStack.tag), totalToDrain, transaction)
                selectedStack.amount += totalExtracted
                totalToDrain -= totalExtracted

                if (totalToDrain <= 0L)
                    break
            }

            if (action == IFluidHandler.FluidAction.EXECUTE)
                transaction.commit()
            else
                transaction.abort()

            return selectedStack
        }
    }
}
package xyz.bluspring.kilt.compat.transfer.fluid

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.fluids.capability.IFluidHandler
import xyz.bluspring.kilt.compat.transfer.fluid.FluidTransferUtils.toDroplets
import xyz.bluspring.kilt.compat.transfer.fluid.FluidTransferUtils.toMillibuckets

open class FabricFluidStorageCapability(val storage: Storage<FluidVariant>) : IFluidHandler {
    override fun getTanks(): Int {
        return storage.toList().size
    }

    override fun getFluidInTank(tank: Int): FluidStack {
        val view = storage.toList()[tank]
        return FluidStack(view.resource.fluid.builtInRegistryHolder(), view.amount.toMillibuckets(), view.resource.components)
    }

    override fun getTankCapacity(tank: Int): Int {
        return storage.toList()[tank].capacity.toMillibuckets()
    }

    override fun isFluidValid(tank: Int, stack: FluidStack): Boolean {
        // TODO: is this correct?
        val view = storage.toList()[tank]
        return view.resource == stack.fluid && view.resource.componentsMatch(stack.componentsPatch)
    }

    override fun fill(resource: FluidStack, action: IFluidHandler.FluidAction): Int {
        TransferUtil.getTransaction().use { transaction ->
            val inserted = storage.insert(FluidVariant.of(resource.fluid, resource.componentsPatch), resource.amount.toDroplets(), transaction)

            if (action == IFluidHandler.FluidAction.EXECUTE)
                transaction.commit()
            else
                transaction.abort()

            return inserted.toMillibuckets()
        }
    }

    override fun drain(resource: FluidStack, action: IFluidHandler.FluidAction): FluidStack {
        TransferUtil.getTransaction().use { transaction ->
            val extracted = storage.extract(FluidVariant.of(resource.fluid, resource.componentsPatch), resource.amount.toDroplets(), transaction)

            if (action == IFluidHandler.FluidAction.EXECUTE)
                transaction.commit()
            else
                transaction.abort()

            return FluidStack(resource.fluid.builtInRegistryHolder(), extracted.toMillibuckets(), resource.componentsPatch)
        }
    }

    override fun drain(maxDrain: Int, action: IFluidHandler.FluidAction): FluidStack {
        TransferUtil.getTransaction().use { transaction ->
            var totalToDrain = maxDrain.toDroplets()
            var selectedStack = FluidStack.EMPTY
            for (view in storage.toList()) {
                if (selectedStack.isEmpty && !view.isResourceBlank) {
                    selectedStack = FluidStack(view.resource.fluid.builtInRegistryHolder(), 0, view.resource.components)
                }

                if (view.resource.fluid != selectedStack.fluid || !view.resource.componentsMatch(selectedStack.componentsPatch))
                    continue

                val totalExtracted = storage.extract(FluidVariant.of(selectedStack.fluid, selectedStack.componentsPatch), totalToDrain, transaction)
                selectedStack.amount += totalExtracted.toMillibuckets()
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
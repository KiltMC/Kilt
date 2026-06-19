package xyz.bluspring.kilt.compat.fabric.cctweaked

import dan200.computercraft.api.detail.DetailProvider
import dan200.computercraft.api.detail.DetailRegistry
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.neoforged.neoforge.fluids.FluidStack

class WrappedDetailRegistry(val wrapped: DetailRegistry<StorageView<FluidVariant>>) : DetailRegistry<FluidStack> {
    private fun StorageView<FluidVariant>.toStack(): FluidStack {
        return FluidStack(this.resource.registryEntry, this.amount.toInt())
    }

    private fun FluidStack.toVariantView(): StorageView<FluidVariant> {
        val fluid = this.fluid
        val amount = this.amount
        return object : StorageView<FluidVariant> {
            override fun extract(resource: FluidVariant, maxAmount: Long, transaction: TransactionContext): Long = 0;
            override fun isResourceBlank(): Boolean = false
            override fun getResource(): FluidVariant? = FluidVariant.of(fluid)
            override fun getAmount(): Long = amount.toLong()
            override fun getCapacity(): Long = amount.toLong()
        }
    }

    override fun addProvider(provider: DetailProvider<in FluidStack>) {
        wrapped.addProvider { map, view ->
            provider.provideDetails(map, view.toStack())
        }
    }

    override fun getBasicDetails(stack: FluidStack): Map<String, Any> {
        return wrapped.getBasicDetails(stack.toVariantView())
    }

    override fun getDetails(stack: FluidStack): Map<String, Any> {
        return wrapped.getDetails(stack.toVariantView())
    }
}

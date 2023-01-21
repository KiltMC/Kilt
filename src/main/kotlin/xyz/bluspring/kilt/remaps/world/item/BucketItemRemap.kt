package xyz.bluspring.kilt.remaps.world.item

import net.minecraft.world.item.BucketItem
import net.minecraft.world.level.material.Fluid
import xyz.bluspring.kilt.injections.item.BucketItemInjection
import java.util.function.Supplier

class BucketItemRemap(private val fluidSupplier: Supplier<out Fluid>, builder: Properties) : BucketItem(fluidSupplier.get(), builder), BucketItemInjection {
    override fun getFluid(): Fluid {
        return fluidSupplier.get()
    }
}
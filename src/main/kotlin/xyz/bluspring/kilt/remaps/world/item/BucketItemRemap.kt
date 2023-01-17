package xyz.bluspring.kilt.remaps.world.item

import net.minecraft.world.item.BucketItem
import net.minecraft.world.level.material.Fluid
import java.util.function.Supplier

class BucketItemRemap(private val fluidSupplier: Supplier<out Fluid>, builder: Properties) : BucketItem(fluidSupplier.get(), builder) {
    val fluid: Fluid
        get() = fluidSupplier.get()
}
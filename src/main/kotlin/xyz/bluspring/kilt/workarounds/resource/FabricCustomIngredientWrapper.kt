package xyz.bluspring.kilt.workarounds.resource

import com.mojang.serialization.MapCodec
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.common.crafting.ICustomIngredient
import net.neoforged.neoforge.common.crafting.IngredientType
import java.util.stream.Stream

@JvmRecord
data class FabricCustomIngredientWrapper(val original: CustomIngredient) : ICustomIngredient {
    override fun test(stack: ItemStack): Boolean = this.original.test(stack)
    override fun getItems(): Stream<ItemStack> = this.original.matchingStacks.stream()
    override fun isSimple(): Boolean = this.original.requiresTesting()

    override fun getType(): IngredientType<*> {
        val codec = this.original.serializer.getCodec(false) as MapCodec<CustomIngredient>
        val streamCodec = this.original.serializer.packetCodec as StreamCodec<RegistryFriendlyByteBuf, CustomIngredient>
        return IngredientType(
            codec.xmap(::FabricCustomIngredientWrapper, FabricCustomIngredientWrapper::original),
            streamCodec.map(::FabricCustomIngredientWrapper, FabricCustomIngredientWrapper::original)
        )
    }
}

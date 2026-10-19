package xyz.bluspring.kilt.workarounds

import com.mojang.serialization.MapCodec
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient
import net.minecraft.core.Holder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.common.crafting.ICustomIngredient
import net.neoforged.neoforge.common.crafting.IngredientType
import java.util.stream.Stream

@JvmRecord
data class FabricCustomIngredientWrapper(val original: CustomIngredient) : ICustomIngredient {
    override fun test(stack: ItemStack): Boolean = this.original.test(stack)
    override fun items(): Stream<Holder<Item>> = this.original.items()
    override fun isSimple(): Boolean = this.original.requiresTesting()

    override fun getType(): IngredientType<*> {
        val codec = this.original.serializer.codec as MapCodec<CustomIngredient>
        val streamCodec = this.original.serializer.streamCodec as StreamCodec<RegistryFriendlyByteBuf, CustomIngredient>
        return IngredientType(
            codec.xmap(::FabricCustomIngredientWrapper, FabricCustomIngredientWrapper::original),
            streamCodec.map(::FabricCustomIngredientWrapper, FabricCustomIngredientWrapper::original)
        )
    }
}

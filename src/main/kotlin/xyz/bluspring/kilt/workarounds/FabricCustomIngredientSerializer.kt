package xyz.bluspring.kilt.workarounds

import com.mojang.serialization.MapCodec
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.Identifier
import net.neoforged.neoforge.common.crafting.ICustomIngredient
import net.neoforged.neoforge.common.crafting.IngredientType
import net.neoforged.neoforge.registries.NeoForgeRegistries

data class FabricCustomIngredientSerializer<T : ICustomIngredient>(val id: Identifier, val type: IngredientType<T>) : CustomIngredientSerializer<T> {
    override fun getIdentifier(): Identifier = this.id
    override fun getCodec(): MapCodec<T> = this.type.codec
    override fun getStreamCodec(): StreamCodec<RegistryFriendlyByteBuf, T> = this.type.streamCodec.cast()

    companion object {
        @JvmStatic
        fun <T : ICustomIngredient> getOrCreate(type: IngredientType<T>): CustomIngredientSerializer<*> {
            val id = NeoForgeRegistries.INGREDIENT_TYPES.getKey(type)
                ?: throw IllegalStateException("Ingredient type $type wasn't registered to the NeoForge registry!")

            return CustomIngredientSerializer.get(id)
                ?: FabricCustomIngredientSerializer(id, type).apply {
                    CustomIngredientSerializer.register(this)
                }
        }
    }
}

package xyz.bluspring.kilt.workarounds.resource

import com.mojang.serialization.MapCodec
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer
import net.minecraft.core.Registry
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.common.crafting.ICustomIngredient
import net.neoforged.neoforge.common.crafting.IngredientType
import net.neoforged.neoforge.registries.NeoForgeRegistries
import xyz.bluspring.kilt.Kilt
import java.util.stream.Stream

@JvmRecord
data class FabricCustomIngredientSerializer<T : ICustomIngredient>(val id: ResourceLocation, val type: IngredientType<T>) : CustomIngredientSerializer<T> {
    override fun getIdentifier(): ResourceLocation = this.id
    override fun getCodec(allowEmpty: Boolean): MapCodec<T> {
        return if (allowEmpty)
            this.type.codec.orElse(EmptyCustomIngredient as T) // Kilt: lmao
        else
            this.type.codec
    }
    override fun getPacketCodec(): StreamCodec<RegistryFriendlyByteBuf, T> = this.type.streamCodec.cast()

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

    object EmptyCustomIngredient : ICustomIngredient {
        @JvmField val CODEC: MapCodec<EmptyCustomIngredient> = MapCodec.unit(EmptyCustomIngredient)
        @JvmField val TYPE: IngredientType<EmptyCustomIngredient> = Registry.register(NeoForgeRegistries.INGREDIENT_TYPES, Kilt.id("empty"),
            IngredientType(CODEC))

        override fun test(stack: ItemStack): Boolean = false
        override fun getItems(): Stream<ItemStack> = Stream.empty()
        override fun isSimple(): Boolean = true
        override fun getType(): IngredientType<*> = TYPE
    }
}

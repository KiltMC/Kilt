package net.minecraftforge.common.crafting

import com.google.gson.JsonObject
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.item.crafting.Ingredient
import java.util.stream.Stream

class VanillaIngredientSerializer : IIngredientSerializer<Ingredient> {
    override fun parse(buffer: FriendlyByteBuf): Ingredient {
        return Ingredient.fromValues(Stream.generate {
            Ingredient.ItemValue(buffer.readItem())
        }.limit(buffer.readVarInt().toLong()))
    }

    override fun parse(json: JsonObject): Ingredient {
        return Ingredient.fromValues(Stream.of(Ingredient.valueFromJson(json)))
    }

    override fun write(buffer: FriendlyByteBuf, ingredient: Ingredient) {
        val items = ingredient.items
        buffer.writeVarInt(items.size)

        items.forEach {
            buffer.writeItem(it)
        }
    }

    companion object {
        @JvmField
        val INSTANCE = VanillaIngredientSerializer()
    }
}
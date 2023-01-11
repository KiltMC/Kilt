package net.minecraftforge.common.crafting

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.GsonHelper
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraftforge.common.crafting.conditions.ICondition
import net.minecraftforge.common.crafting.conditions.IConditionSerializer
import io.github.fabricators_of_create.porting_lib.crafting.CraftingHelper as FabricCraftingHelper

object CraftingHelper {
    private val conditions = mutableMapOf<ResourceLocation, IConditionSerializer<*>>()

    @JvmStatic
    fun register(serializer: IConditionSerializer<*>): IConditionSerializer<*> {
        val key = serializer.ID
        if (conditions.contains(key))
            throw IllegalStateException("Duplicate recipe condition serializer: $key")

        conditions[key] = serializer
        return serializer
    }

    @JvmStatic
    fun <T : Ingredient> register(key: ResourceLocation, serializer: IIngredientSerializer<T>): IIngredientSerializer<T> {
        // this should work, i think.
        return FabricCraftingHelper.register(key, serializer) as IIngredientSerializer<T>
    }

    @JvmStatic
    fun getID(serializer: IIngredientSerializer<*>): ResourceLocation? {
        return FabricCraftingHelper.getID(serializer)
    }

    @JvmStatic
    fun <T : Ingredient> write(buffer: FriendlyByteBuf, ingredient: T) {
        // TODO: Make this use a serializer provided by Ingredient, using an extension.
        val serializer = VanillaIngredientSerializer.INSTANCE
        val key = FabricCraftingHelper.getID(serializer)
            ?: throw IllegalArgumentException("Tried to serialize unregistered ingredient: $ingredient $serializer")

        // I'm surprised this hasn't had IntelliJ yelling at me, considering
        // that, well, these two are pretty fuckin' equal.
        if (serializer != VanillaIngredientSerializer.INSTANCE) {
            buffer.writeVarInt(-1)
            buffer.writeResourceLocation(key)
        }

        serializer.write(buffer, ingredient)
    }

    @JvmStatic
    fun getIngredient(type: ResourceLocation, buffer: FriendlyByteBuf): Ingredient {
        return VanillaIngredientSerializer.INSTANCE.fromNetwork(buffer)
    }

    @JvmStatic
    fun getIngredient(json: JsonElement): Ingredient {
        return FabricCraftingHelper.getIngredient(json)
    }

    @JvmStatic
    fun getItemStack(json: JsonObject, readNBT: Boolean): ItemStack {
        return FabricCraftingHelper.getItemStack(json, readNBT)
    }

    @JvmStatic
    fun getItemStack(json: JsonObject, readNBT: Boolean, disallowsAirInRecipe: Boolean): ItemStack {
        return FabricCraftingHelper.getItemStack(json, readNBT, disallowsAirInRecipe)
    }

    @JvmStatic
    fun getItem(itemName: String, disallowsAirInRecipe: Boolean): Item {
        return FabricCraftingHelper.getItem(itemName, disallowsAirInRecipe)
    }

    @JvmStatic
    fun getNBT(element: JsonElement): CompoundTag {
        return FabricCraftingHelper.getNBT(element)
    }

    @JvmStatic
    fun processConditions(json: JsonObject, memberName: String, context: ICondition.IContext): Boolean {
        return FabricCraftingHelper.processConditions(json, memberName)
    }

    @JvmStatic
    fun processConditions(conditions: JsonArray, context: ICondition.IContext): Boolean {
        return FabricCraftingHelper.processConditions(conditions)
    }

    @JvmStatic
    fun getCondition(json: JsonObject): ICondition {
        val type = ResourceLocation(GsonHelper.getAsString(json, "type"))
        val serializer = conditions[type] ?: throw JsonSyntaxException("Unknown condition type: $type")

        return serializer.read(json)
    }

    @JvmStatic
    fun <T : ICondition> serialize(condition: T): JsonObject {
        val serializer = conditions[condition.ID] as IConditionSerializer<T>? ?: throw JsonSyntaxException("Unknown condition type: ${condition.ID}")

        return serializer.getJson(condition)
    }

    @JvmStatic
    fun serialize(vararg conditions: ICondition): JsonArray {
        val array = JsonArray()
        conditions.forEach {
            array.add(serialize(it))
        }

        return array
    }
}
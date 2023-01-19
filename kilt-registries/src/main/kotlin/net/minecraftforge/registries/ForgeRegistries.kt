package net.minecraftforge.registries

import net.minecraft.core.Registry
import net.minecraft.network.syncher.EntityDataSerializer
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.fluids.FluidType
import net.minecraftforge.registries.holdersets.HolderSetType

object ForgeRegistries {
    // Game objects
    @JvmField val BLOCKS = forgeRegistry(Registry.BLOCK_REGISTRY)
    @JvmField val FLUIDS = forgeRegistry(Registry.FLUID_REGISTRY)
    @JvmField val ITEMS = forgeRegistry(Registry.ITEM_REGISTRY)
    @JvmField val MOB_EFFECTS = forgeRegistry(Registry.MOB_EFFECT_REGISTRY)
    @JvmField val SOUND_EVENTS = forgeRegistry(Registry.SOUND_EVENT_REGISTRY)
    @JvmField val POTIONS = forgeRegistry(Registry.POTION_REGISTRY)
    @JvmField val ENCHANTMENTS = forgeRegistry(Registry.ENCHANTMENT_REGISTRY)
    @JvmField val ENTITY_TYPES = forgeRegistry(Registry.ENTITY_TYPE_REGISTRY)
    @JvmField val BLOCK_ENTITY_TYPES = forgeRegistry(Registry.BLOCK_ENTITY_TYPE_REGISTRY)
    @JvmField val PARTICLE_TYPES = forgeRegistry(Registry.PARTICLE_TYPE_REGISTRY)
    @JvmField val MENU_TYPES = forgeRegistry(Registry.MENU_REGISTRY)
    @JvmField val PAINTING_VARIANTS = forgeRegistry(Registry.PAINTING_VARIANT_REGISTRY)
    @JvmField val RECIPE_TYPES = forgeRegistry(Registry.RECIPE_TYPE_REGISTRY)
    @JvmField val RECIPE_SERIALIZERS = forgeRegistry(Registry.RECIPE_SERIALIZER_REGISTRY)
    @JvmField val ATTRIBUTES = forgeRegistry(Registry.ATTRIBUTE_REGISTRY)
    @JvmField val STAT_TYPES = forgeRegistry(Registry.STAT_TYPE_REGISTRY)
    @JvmField val COMMAND_ARGUMENT_TYPES = forgeRegistry(Registry.COMMAND_ARGUMENT_TYPE_REGISTRY)

    // Villages
    @JvmField val VILLAGER_PROFESSIONS = forgeRegistry(Registry.VILLAGER_PROFESSION_REGISTRY)
    @JvmField val POI_TYPES = forgeRegistry(Registry.POINT_OF_INTEREST_TYPE_REGISTRY)
    @JvmField val MEMORY_MODULE_TYPES = forgeRegistry(Registry.MEMORY_MODULE_TYPE_REGISTRY)
    @JvmField val SENSOR_TYPES = forgeRegistry(Registry.SENSOR_TYPE_REGISTRY)
    @JvmField val SCHEDULES = forgeRegistry(Registry.SCHEDULE_REGISTRY)
    @JvmField val ACTIVITIES = forgeRegistry(Registry.ACTIVITY_REGISTRY)

    // World Generation
    @JvmField val WORLD_CARVERS = forgeRegistry(Registry.CARVER_REGISTRY)
    @JvmField val FEATURES = forgeRegistry(Registry.FEATURE_REGISTRY)
    @JvmField val CHUNK_STATUS = forgeRegistry(Registry.CHUNK_STATUS_REGISTRY)
    @JvmField val BLOCK_STATE_PROVIDER_TYPES = forgeRegistry(Registry.BLOCK_STATE_PROVIDER_TYPE_REGISTRY)
    @JvmField val FOLIAGE_PLACER_TYPES = forgeRegistry(Registry.FOLIAGE_PLACER_TYPE_REGISTRY)
    @JvmField val TREE_DECORATOR_TYPES = forgeRegistry(Registry.TREE_DECORATOR_TYPE_REGISTRY)

    // Dynamic/Data Driven
    @JvmField val BIOMES = forgeRegistry(Registry.BIOME_REGISTRY)

    // Custom Forge Registries
    internal val DEFERRED_ENTITY_DATA_SERIALIZERS = DeferredRegister.create(Keys.ENTITY_DATA_SERIALIZERS, Keys.ENTITY_DATA_SERIALIZERS.location().namespace)
    //@JvmField val ENTITY_DATA_SERIALIZERS = DEFERRED_ENTITY_DATA_SERIALIZERS.makeRegistry(GameData::getGLMSerializersRegistryBuilder)

    internal val DEFERRED_FLUID_TYPES = DeferredRegister.create(Keys.FLUID_TYPES, Keys.FLUID_TYPES.location().namespace)
    @JvmField val FLUID_TYPES = DEFERRED_FLUID_TYPES.makeRegistry {
        RegistryBuilder<FluidType>().setName(Keys.FLUIDS.location())
    }

    internal val DEFERRED_HOLDER_SET_TYPES = DeferredRegister.create(Keys.HOLDER_SET_TYPES, Keys.HOLDER_SET_TYPES.location().namespace)
    @JvmField val HOLDER_SET_TYPES = DEFERRED_HOLDER_SET_TYPES.makeRegistry { RegistryBuilder<HolderSetType>().setName(
        Keys.HOLDER_SET_TYPES.location()) }
    // TODO: Implement the rest of these registries

    private fun <T> forgeRegistry(registry: ResourceKey<out Registry<T>>): ForgeRegistry<T> {
        return ForgeRegistry(registry.location(), RegistryBuilder())
    }

    object Keys {
        @JvmField val BLOCKS = Registry.BLOCK_REGISTRY
        @JvmField val FLUIDS = Registry.FLUID_REGISTRY
        @JvmField val ITEMS = Registry.ITEM_REGISTRY
        @JvmField val MOB_EFFECTS = Registry.MOB_EFFECT_REGISTRY
        @JvmField val SOUND_EVENTS = Registry.SOUND_EVENT_REGISTRY
        @JvmField val POTIONS = Registry.POTION_REGISTRY
        @JvmField val ENCHANTMENTS = Registry.ENCHANTMENT_REGISTRY
        @JvmField val ENTITY_TYPES = Registry.ENTITY_TYPE_REGISTRY
        @JvmField val BLOCK_ENTITY_TYPES = Registry.BLOCK_ENTITY_TYPE_REGISTRY
        @JvmField val PARTICLE_TYPES = Registry.PARTICLE_TYPE_REGISTRY
        @JvmField val MENU_TYPES = Registry.MENU_REGISTRY
        @JvmField val PAINTING_VARIANTS = Registry.PAINTING_VARIANT_REGISTRY
        @JvmField val RECIPE_TYPES = Registry.RECIPE_TYPE_REGISTRY
        @JvmField val RECIPE_SERIALIZERS = Registry.RECIPE_SERIALIZER_REGISTRY
        @JvmField val ATTRIBUTES = Registry.ATTRIBUTE_REGISTRY
        @JvmField val STAT_TYPES = Registry.STAT_TYPE_REGISTRY
        @JvmField val COMMAND_ARGUMENT_TYPES = Registry.COMMAND_ARGUMENT_TYPE_REGISTRY

        // Villages
        @JvmField val VILLAGER_PROFESSIONS = (Registry.VILLAGER_PROFESSION_REGISTRY)
        @JvmField val POI_TYPES = (Registry.POINT_OF_INTEREST_TYPE_REGISTRY)
        @JvmField val MEMORY_MODULE_TYPES = (Registry.MEMORY_MODULE_TYPE_REGISTRY)
        @JvmField val SENSOR_TYPES = (Registry.SENSOR_TYPE_REGISTRY)
        @JvmField val SCHEDULES = (Registry.SCHEDULE_REGISTRY)
        @JvmField val ACTIVITIES = (Registry.ACTIVITY_REGISTRY)

        // World Generation
        @JvmField val WORLD_CARVERS = (Registry.CARVER_REGISTRY)
        @JvmField val FEATURES = (Registry.FEATURE_REGISTRY)
        @JvmField val CHUNK_STATUS = (Registry.CHUNK_STATUS_REGISTRY)
        @JvmField val BLOCK_STATE_PROVIDER_TYPES = (Registry.BLOCK_STATE_PROVIDER_TYPE_REGISTRY)
        @JvmField val FOLIAGE_PLACER_TYPES = (Registry.FOLIAGE_PLACER_TYPE_REGISTRY)
        @JvmField val TREE_DECORATOR_TYPES = (Registry.TREE_DECORATOR_TYPE_REGISTRY)

        // Dynamic/Data Driven
        @JvmField val BIOMES = (Registry.BIOME_REGISTRY)

        // Forge
        @JvmField val ENTITY_DATA_SERIALIZERS = key<EntityDataSerializer<*>>("forge:entity_data_serializers")
        //@JvmField val GLOBAL_LOOT_MODIFIER_SERIALIZERS = key<Codec<out IGlobalLootModifier>>("forge:global_loot_modifier_serializers")
        //@JvmField val BIOME_MODIFIER_SERIALIZERS = key<Codec<out BiomeModifier>>("forge:biome_modifier_serializers")
        //@JvmField val STRUCTURE_MODIFIER_SERIALIZERS = key<Codec<out StructureModifier>>("forge:structure_modifier_serializers")
        @JvmField val FLUID_TYPES = key<FluidType>("forge:fluid_type")
        @JvmField val HOLDER_SET_TYPES = key<HolderSetType>("forge:holder_set_type")

        // Forge Dynamic
        //@JvmField val BIOME_MODIFIERS = key<BiomeModifier>("forge:biome_modifier")
        //@JvmField val STRUCTURE_MODIFIERS = key<StructureModifier>("forge:structure_modifier")

        private fun <T> key(name: String): ResourceKey<Registry<T>> {
            return ResourceKey.createRegistryKey(ResourceLocation(name))
        }
    }
}
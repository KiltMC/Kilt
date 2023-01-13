package net.minecraftforge.common

import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.Block
import me.alphamode.forgetags.Tags as FabricTags

// i didn't like how much i needed to do so i wrote a script to just make this a lot faster.
object Tags {
    @JvmStatic
    fun init() {
        Tags.Blocks.init()
        Tags.Items.init()
        Tags.Biomes.init()
        Tags.EntityTypes.init()
        Tags.Fluids.init()
    }

    object Blocks {
        internal fun init() {}

        @JvmField val BARRELS = FabricTags.Blocks.BARRELS
        @JvmField val BARRELS_WOODEN = FabricTags.Blocks.BARRELS_WOODEN
        @JvmField val BOOKSHELVES = tag("bookshelves")
        @JvmField val CHESTS = FabricTags.Blocks.CHESTS
        @JvmField val CHESTS_ENDER = FabricTags.Blocks.CHESTS_ENDER
        @JvmField val CHESTS_TRAPPED = FabricTags.Blocks.CHESTS_TRAPPED
        @JvmField val CHESTS_WOODEN = FabricTags.Blocks.CHESTS_WOODEN
        @JvmField val COBBLESTONE = FabricTags.Blocks.COBBLESTONE
        @JvmField val COBBLESTONE_NORMAL = FabricTags.Blocks.COBBLESTONE_NORMAL
        @JvmField val COBBLESTONE_INFESTED = FabricTags.Blocks.COBBLESTONE_INFESTED
        @JvmField val COBBLESTONE_MOSSY = FabricTags.Blocks.COBBLESTONE_MOSSY
        @JvmField val COBBLESTONE_DEEPSLATE = FabricTags.Blocks.COBBLESTONE_DEEPSLATE
        @JvmField val END_STONES = FabricTags.Blocks.END_STONES
        @JvmField val ENDERMAN_PLACE_ON_BLACKLIST = FabricTags.Blocks.ENDERMAN_PLACE_ON_BLACKLIST
        @JvmField val FENCE_GATES = FabricTags.Blocks.FENCE_GATES
        @JvmField val FENCE_GATES_WOODEN = FabricTags.Blocks.FENCE_GATES_WOODEN
        @JvmField val FENCES = FabricTags.Blocks.FENCES
        @JvmField val FENCES_NETHER_BRICK = FabricTags.Blocks.FENCES_NETHER_BRICK
        @JvmField val FENCES_WOODEN = FabricTags.Blocks.FENCES_WOODEN
        @JvmField val GLASS = FabricTags.Blocks.GLASS
        @JvmField val GLASS_BLACK = FabricTags.Blocks.GLASS_BLACK
        @JvmField val GLASS_BLUE = FabricTags.Blocks.GLASS_BLUE
        @JvmField val GLASS_BROWN = FabricTags.Blocks.GLASS_BROWN
        @JvmField val GLASS_COLORLESS = FabricTags.Blocks.GLASS_COLORLESS
        @JvmField val GLASS_CYAN = FabricTags.Blocks.GLASS_CYAN
        @JvmField val GLASS_GRAY = FabricTags.Blocks.GLASS_GRAY
        @JvmField val GLASS_GREEN = FabricTags.Blocks.GLASS_GREEN
        @JvmField val GLASS_LIGHT_BLUE = FabricTags.Blocks.GLASS_LIGHT_BLUE
        @JvmField val GLASS_LIGHT_GRAY = FabricTags.Blocks.GLASS_LIGHT_GRAY
        @JvmField val GLASS_LIME = FabricTags.Blocks.GLASS_LIME
        @JvmField val GLASS_MAGENTA = FabricTags.Blocks.GLASS_MAGENTA
        @JvmField val GLASS_ORANGE = FabricTags.Blocks.GLASS_ORANGE
        @JvmField val GLASS_PINK = FabricTags.Blocks.GLASS_PINK
        @JvmField val GLASS_PURPLE = FabricTags.Blocks.GLASS_PURPLE
        @JvmField val GLASS_RED = FabricTags.Blocks.GLASS_RED
        @JvmField val GLASS_SILICA = FabricTags.Blocks.GLASS_SILICA
        @JvmField val GLASS_TINTED = FabricTags.Blocks.GLASS_TINTED
        @JvmField val GLASS_WHITE = FabricTags.Blocks.GLASS_WHITE
        @JvmField val GLASS_YELLOW = FabricTags.Blocks.GLASS_YELLOW
        @JvmField val GLASS_PANES = FabricTags.Blocks.GLASS_PANES
        @JvmField val GLASS_PANES_BLACK = FabricTags.Blocks.GLASS_PANES_BLACK
        @JvmField val GLASS_PANES_BLUE = FabricTags.Blocks.GLASS_PANES_BLUE
        @JvmField val GLASS_PANES_BROWN = FabricTags.Blocks.GLASS_PANES_BROWN
        @JvmField val GLASS_PANES_COLORLESS = FabricTags.Blocks.GLASS_PANES_COLORLESS
        @JvmField val GLASS_PANES_CYAN = FabricTags.Blocks.GLASS_PANES_CYAN
        @JvmField val GLASS_PANES_GRAY = FabricTags.Blocks.GLASS_PANES_GRAY
        @JvmField val GLASS_PANES_GREEN = FabricTags.Blocks.GLASS_PANES_GREEN
        @JvmField val GLASS_PANES_LIGHT_BLUE = FabricTags.Blocks.GLASS_PANES_LIGHT_BLUE
        @JvmField val GLASS_PANES_LIGHT_GRAY = FabricTags.Blocks.GLASS_PANES_LIGHT_GRAY
        @JvmField val GLASS_PANES_LIME = FabricTags.Blocks.GLASS_PANES_LIME
        @JvmField val GLASS_PANES_MAGENTA = FabricTags.Blocks.GLASS_PANES_MAGENTA
        @JvmField val GLASS_PANES_ORANGE = FabricTags.Blocks.GLASS_PANES_ORANGE
        @JvmField val GLASS_PANES_PINK = FabricTags.Blocks.GLASS_PANES_PINK
        @JvmField val GLASS_PANES_PURPLE = FabricTags.Blocks.GLASS_PANES_PURPLE
        @JvmField val GLASS_PANES_RED = FabricTags.Blocks.GLASS_PANES_RED
        @JvmField val GLASS_PANES_WHITE = FabricTags.Blocks.GLASS_PANES_WHITE
        @JvmField val GLASS_PANES_YELLOW = FabricTags.Blocks.GLASS_PANES_YELLOW
        @JvmField val GRAVEL = FabricTags.Blocks.GRAVEL
        @JvmField val NETHERRACK = FabricTags.Blocks.NETHERRACK
        @JvmField val OBSIDIAN = FabricTags.Blocks.OBSIDIAN
        @JvmField val ORE_BEARING_GROUND_DEEPSLATE = FabricTags.Blocks.ORE_BEARING_GROUND_DEEPSLATE
        @JvmField val ORE_BEARING_GROUND_NETHERRACK = FabricTags.Blocks.ORE_BEARING_GROUND_NETHERRACK
        @JvmField val ORE_BEARING_GROUND_STONE = FabricTags.Blocks.ORE_BEARING_GROUND_STONE
        @JvmField val ORE_RATES_DENSE = FabricTags.Blocks.ORE_RATES_DENSE
        @JvmField val ORE_RATES_SINGULAR = FabricTags.Blocks.ORE_RATES_SINGULAR
        @JvmField val ORE_RATES_SPARSE = FabricTags.Blocks.ORE_RATES_SPARSE
        @JvmField val ORES = FabricTags.Blocks.ORES
        @JvmField val ORES_COAL = FabricTags.Blocks.ORES_COAL
        @JvmField val ORES_COPPER = FabricTags.Blocks.ORES_COPPER
        @JvmField val ORES_DIAMOND = FabricTags.Blocks.ORES_DIAMOND
        @JvmField val ORES_EMERALD = FabricTags.Blocks.ORES_EMERALD
        @JvmField val ORES_GOLD = FabricTags.Blocks.ORES_GOLD
        @JvmField val ORES_IRON = FabricTags.Blocks.ORES_IRON
        @JvmField val ORES_LAPIS = FabricTags.Blocks.ORES_LAPIS
        @JvmField val ORES_NETHERITE_SCRAP = FabricTags.Blocks.ORES_NETHERITE_SCRAP
        @JvmField val ORES_QUARTZ = FabricTags.Blocks.ORES_QUARTZ
        @JvmField val ORES_REDSTONE = FabricTags.Blocks.ORES_REDSTONE
        @JvmField val ORES_IN_GROUND_DEEPSLATE = FabricTags.Blocks.ORES_IN_GROUND_DEEPSLATE
        @JvmField val ORES_IN_GROUND_NETHERRACK = FabricTags.Blocks.ORES_IN_GROUND_NETHERRACK
        @JvmField val ORES_IN_GROUND_STONE = FabricTags.Blocks.ORES_IN_GROUND_STONE
        @JvmField val SAND = FabricTags.Blocks.SAND
        @JvmField val SAND_COLORLESS = FabricTags.Blocks.SAND_COLORLESS
        @JvmField val SAND_RED = FabricTags.Blocks.SAND_RED
        @JvmField val SANDSTONE = FabricTags.Blocks.SANDSTONE
        @JvmField val STAINED_GLASS = FabricTags.Blocks.STAINED_GLASS
        @JvmField val STAINED_GLASS_PANES = FabricTags.Blocks.STAINED_GLASS_PANES
        @JvmField val STONE = FabricTags.Blocks.STONE
        @JvmField val STORAGE_BLOCKS = FabricTags.Blocks.STORAGE_BLOCKS
        @JvmField val STORAGE_BLOCKS_AMETHYST = FabricTags.Blocks.STORAGE_BLOCKS_AMETHYST
        @JvmField val STORAGE_BLOCKS_COAL = FabricTags.Blocks.STORAGE_BLOCKS_COAL
        @JvmField val STORAGE_BLOCKS_COPPER = FabricTags.Blocks.STORAGE_BLOCKS_COPPER
        @JvmField val STORAGE_BLOCKS_DIAMOND = FabricTags.Blocks.STORAGE_BLOCKS_DIAMOND
        @JvmField val STORAGE_BLOCKS_EMERALD = FabricTags.Blocks.STORAGE_BLOCKS_EMERALD
        @JvmField val STORAGE_BLOCKS_GOLD = FabricTags.Blocks.STORAGE_BLOCKS_GOLD
        @JvmField val STORAGE_BLOCKS_IRON = FabricTags.Blocks.STORAGE_BLOCKS_IRON
        @JvmField val STORAGE_BLOCKS_LAPIS = FabricTags.Blocks.STORAGE_BLOCKS_LAPIS
        @JvmField val STORAGE_BLOCKS_NETHERITE = FabricTags.Blocks.STORAGE_BLOCKS_NETHERITE
        @JvmField val STORAGE_BLOCKS_QUARTZ = FabricTags.Blocks.STORAGE_BLOCKS_QUARTZ
        @JvmField val STORAGE_BLOCKS_RAW_COPPER = FabricTags.Blocks.STORAGE_BLOCKS_RAW_COPPER
        @JvmField val STORAGE_BLOCKS_RAW_GOLD = FabricTags.Blocks.STORAGE_BLOCKS_RAW_GOLD
        @JvmField val STORAGE_BLOCKS_RAW_IRON = FabricTags.Blocks.STORAGE_BLOCKS_RAW_IRON
        @JvmField val STORAGE_BLOCKS_REDSTONE = FabricTags.Blocks.STORAGE_BLOCKS_REDSTONE
        @JvmField val NEEDS_WOOD_TOOL = FabricTags.Blocks.NEEDS_WOOD_TOOL
        @JvmField val NEEDS_GOLD_TOOL = FabricTags.Blocks.NEEDS_GOLD_TOOL
        @JvmField val NEEDS_NETHERITE_TOOL = FabricTags.Blocks.NEEDS_NETHERITE_TOOL

        fun tag(name: String): TagKey<Block> {
            return TagKey.create(Registry.BLOCK_REGISTRY, ResourceLocation("c", name))
        }
    }

    object EntityTypes {
        internal fun init() {}

        @JvmField val BOSSES = FabricTags.EntityTypes.BOSSES
    }

    object Items {
        internal fun init() {}

        @JvmField val BARRELS = FabricTags.Items.BARRELS
        @JvmField val BARRELS_WOODEN = FabricTags.Items.BARRELS_WOODEN
        @JvmField val BONES = FabricTags.Items.BONES
        @JvmField val BOOKSHELVES = FabricTags.Items.BOOKSHELVES
        @JvmField val CHESTS = FabricTags.Items.CHESTS
        @JvmField val CHESTS_ENDER = FabricTags.Items.CHESTS_ENDER
        @JvmField val CHESTS_TRAPPED = FabricTags.Items.CHESTS_TRAPPED
        @JvmField val CHESTS_WOODEN = FabricTags.Items.CHESTS_WOODEN
        @JvmField val COBBLESTONE = FabricTags.Items.COBBLESTONE
        @JvmField val COBBLESTONE_NORMAL = FabricTags.Items.COBBLESTONE_NORMAL
        @JvmField val COBBLESTONE_INFESTED = FabricTags.Items.COBBLESTONE_INFESTED
        @JvmField val COBBLESTONE_MOSSY = FabricTags.Items.COBBLESTONE_MOSSY
        @JvmField val COBBLESTONE_DEEPSLATE = FabricTags.Items.COBBLESTONE_DEEPSLATE
        @JvmField val CROPS = FabricTags.Items.CROPS
        @JvmField val CROPS_BEETROOT = FabricTags.Items.CROPS_BEETROOT
        @JvmField val CROPS_CARROT = FabricTags.Items.CROPS_CARROT
        @JvmField val CROPS_NETHER_WART = FabricTags.Items.CROPS_NETHER_WART
        @JvmField val CROPS_POTATO = FabricTags.Items.CROPS_POTATO
        @JvmField val CROPS_WHEAT = FabricTags.Items.CROPS_WHEAT
        @JvmField val DUSTS = FabricTags.Items.DUSTS
        @JvmField val DUSTS_PRISMARINE = FabricTags.Items.DUSTS_PRISMARINE
        @JvmField val DUSTS_REDSTONE = FabricTags.Items.DUSTS_REDSTONE
        @JvmField val DUSTS_GLOWSTONE = FabricTags.Items.DUSTS_GLOWSTONE
        @JvmField val DYES = FabricTags.Items.DYES
        @JvmField val EGGS = FabricTags.Items.EGGS
        @JvmField val ENCHANTING_FUELS = FabricTags.Items.ENCHANTING_FUELS
        @JvmField val END_STONES = FabricTags.Items.END_STONES
        @JvmField val ENDER_PEARLS = FabricTags.Items.ENDER_PEARLS
        @JvmField val FEATHERS = FabricTags.Items.FEATHERS
        @JvmField val FENCE_GATES = FabricTags.Items.FENCE_GATES
        @JvmField val FENCE_GATES_WOODEN = FabricTags.Items.FENCE_GATES_WOODEN
        @JvmField val FENCES = FabricTags.Items.FENCES
        @JvmField val FENCES_NETHER_BRICK = FabricTags.Items.FENCES_NETHER_BRICK
        @JvmField val FENCES_WOODEN = FabricTags.Items.FENCES_WOODEN
        @JvmField val GEMS = FabricTags.Items.GEMS
        @JvmField val GEMS_DIAMOND = FabricTags.Items.GEMS_DIAMOND
        @JvmField val GEMS_EMERALD = FabricTags.Items.GEMS_EMERALD
        @JvmField val GEMS_AMETHYST = FabricTags.Items.GEMS_AMETHYST
        @JvmField val GEMS_LAPIS = FabricTags.Items.GEMS_LAPIS
        @JvmField val GEMS_PRISMARINE = FabricTags.Items.GEMS_PRISMARINE
        @JvmField val GEMS_QUARTZ = FabricTags.Items.GEMS_QUARTZ
        @JvmField val GLASS = FabricTags.Items.GLASS
        @JvmField val GLASS_BLACK = FabricTags.Items.GLASS_BLACK
        @JvmField val GLASS_BLUE = FabricTags.Items.GLASS_BLUE
        @JvmField val GLASS_BROWN = FabricTags.Items.GLASS_BROWN
        @JvmField val GLASS_COLORLESS = FabricTags.Items.GLASS_COLORLESS
        @JvmField val GLASS_CYAN = FabricTags.Items.GLASS_CYAN
        @JvmField val GLASS_GRAY = FabricTags.Items.GLASS_GRAY
        @JvmField val GLASS_GREEN = FabricTags.Items.GLASS_GREEN
        @JvmField val GLASS_LIGHT_BLUE = FabricTags.Items.GLASS_LIGHT_BLUE
        @JvmField val GLASS_LIGHT_GRAY = FabricTags.Items.GLASS_LIGHT_GRAY
        @JvmField val GLASS_LIME = FabricTags.Items.GLASS_LIME
        @JvmField val GLASS_MAGENTA = FabricTags.Items.GLASS_MAGENTA
        @JvmField val GLASS_ORANGE = FabricTags.Items.GLASS_ORANGE
        @JvmField val GLASS_PINK = FabricTags.Items.GLASS_PINK
        @JvmField val GLASS_PURPLE = FabricTags.Items.GLASS_PURPLE
        @JvmField val GLASS_RED = FabricTags.Items.GLASS_RED
        @JvmField val GLASS_SILICA = FabricTags.Items.GLASS_SILICA
        @JvmField val GLASS_TINTED = FabricTags.Items.GLASS_TINTED
        @JvmField val GLASS_WHITE = FabricTags.Items.GLASS_WHITE
        @JvmField val GLASS_YELLOW = FabricTags.Items.GLASS_YELLOW
        @JvmField val GLASS_PANES = FabricTags.Items.GLASS_PANES
        @JvmField val GLASS_PANES_BLACK = FabricTags.Items.GLASS_PANES_BLACK
        @JvmField val GLASS_PANES_BLUE = FabricTags.Items.GLASS_PANES_BLUE
        @JvmField val GLASS_PANES_BROWN = FabricTags.Items.GLASS_PANES_BROWN
        @JvmField val GLASS_PANES_COLORLESS = FabricTags.Items.GLASS_PANES_COLORLESS
        @JvmField val GLASS_PANES_CYAN = FabricTags.Items.GLASS_PANES_CYAN
        @JvmField val GLASS_PANES_GRAY = FabricTags.Items.GLASS_PANES_GRAY
        @JvmField val GLASS_PANES_GREEN = FabricTags.Items.GLASS_PANES_GREEN
        @JvmField val GLASS_PANES_LIGHT_BLUE = FabricTags.Items.GLASS_PANES_LIGHT_BLUE
        @JvmField val GLASS_PANES_LIGHT_GRAY = FabricTags.Items.GLASS_PANES_LIGHT_GRAY
        @JvmField val GLASS_PANES_LIME = FabricTags.Items.GLASS_PANES_LIME
        @JvmField val GLASS_PANES_MAGENTA = FabricTags.Items.GLASS_PANES_MAGENTA
        @JvmField val GLASS_PANES_ORANGE = FabricTags.Items.GLASS_PANES_ORANGE
        @JvmField val GLASS_PANES_PINK = FabricTags.Items.GLASS_PANES_PINK
        @JvmField val GLASS_PANES_PURPLE = FabricTags.Items.GLASS_PANES_PURPLE
        @JvmField val GLASS_PANES_RED = FabricTags.Items.GLASS_PANES_RED
        @JvmField val GLASS_PANES_WHITE = FabricTags.Items.GLASS_PANES_WHITE
        @JvmField val GLASS_PANES_YELLOW = FabricTags.Items.GLASS_PANES_YELLOW
        @JvmField val GRAVEL = FabricTags.Items.GRAVEL
        @JvmField val GUNPOWDER = FabricTags.Items.GUNPOWDER
        @JvmField val HEADS = FabricTags.Items.HEADS
        @JvmField val INGOTS = FabricTags.Items.INGOTS
        @JvmField val INGOTS_BRICK = FabricTags.Items.INGOTS_BRICK
        @JvmField val INGOTS_COPPER = FabricTags.Items.INGOTS_COPPER
        @JvmField val INGOTS_GOLD = FabricTags.Items.INGOTS_GOLD
        @JvmField val INGOTS_IRON = FabricTags.Items.INGOTS_IRON
        @JvmField val INGOTS_NETHERITE = FabricTags.Items.INGOTS_NETHERITE
        @JvmField val INGOTS_NETHER_BRICK = FabricTags.Items.INGOTS_NETHER_BRICK
        @JvmField val LEATHER = FabricTags.Items.LEATHER
        @JvmField val MUSHROOMS = FabricTags.Items.MUSHROOMS
        @JvmField val NETHER_STARS = FabricTags.Items.NETHER_STARS
        @JvmField val NETHERRACK = FabricTags.Items.NETHERRACK
        @JvmField val NUGGETS = FabricTags.Items.NUGGETS
        @JvmField val NUGGETS_GOLD = FabricTags.Items.NUGGETS_GOLD
        @JvmField val NUGGETS_IRON = FabricTags.Items.NUGGETS_IRON
        @JvmField val OBSIDIAN = FabricTags.Items.OBSIDIAN
        @JvmField val ORE_BEARING_GROUND_DEEPSLATE = FabricTags.Items.ORE_BEARING_GROUND_DEEPSLATE
        @JvmField val ORE_BEARING_GROUND_NETHERRACK = FabricTags.Items.ORE_BEARING_GROUND_NETHERRACK
        @JvmField val ORE_BEARING_GROUND_STONE = FabricTags.Items.ORE_BEARING_GROUND_STONE
        @JvmField val ORE_RATES_DENSE = FabricTags.Items.ORE_RATES_DENSE
        @JvmField val ORE_RATES_SINGULAR = FabricTags.Items.ORE_RATES_SINGULAR
        @JvmField val ORE_RATES_SPARSE = FabricTags.Items.ORE_RATES_SPARSE
        @JvmField val ORES = FabricTags.Items.ORES
        @JvmField val ORES_COAL = FabricTags.Items.ORES_COAL
        @JvmField val ORES_COPPER = FabricTags.Items.ORES_COPPER
        @JvmField val ORES_DIAMOND = FabricTags.Items.ORES_DIAMOND
        @JvmField val ORES_EMERALD = FabricTags.Items.ORES_EMERALD
        @JvmField val ORES_GOLD = FabricTags.Items.ORES_GOLD
        @JvmField val ORES_IRON = FabricTags.Items.ORES_IRON
        @JvmField val ORES_LAPIS = FabricTags.Items.ORES_LAPIS
        @JvmField val ORES_NETHERITE_SCRAP = FabricTags.Items.ORES_NETHERITE_SCRAP
        @JvmField val ORES_QUARTZ = FabricTags.Items.ORES_QUARTZ
        @JvmField val ORES_REDSTONE = FabricTags.Items.ORES_REDSTONE
        @JvmField val ORES_IN_GROUND_DEEPSLATE = FabricTags.Items.ORES_IN_GROUND_DEEPSLATE
        @JvmField val ORES_IN_GROUND_NETHERRACK = FabricTags.Items.ORES_IN_GROUND_NETHERRACK
        @JvmField val ORES_IN_GROUND_STONE = FabricTags.Items.ORES_IN_GROUND_STONE
        @JvmField val RAW_MATERIALS = FabricTags.Items.RAW_MATERIALS
        @JvmField val RAW_MATERIALS_COPPER = FabricTags.Items.RAW_MATERIALS_COPPER
        @JvmField val RAW_MATERIALS_GOLD = FabricTags.Items.RAW_MATERIALS_GOLD
        @JvmField val RAW_MATERIALS_IRON = FabricTags.Items.RAW_MATERIALS_IRON
        @JvmField val RODS = FabricTags.Items.RODS
        @JvmField val RODS_BLAZE = FabricTags.Items.RODS_BLAZE
        @JvmField val RODS_WOODEN = FabricTags.Items.RODS_WOODEN
        @JvmField val SAND = FabricTags.Items.SAND
        @JvmField val SAND_COLORLESS = FabricTags.Items.SAND_COLORLESS
        @JvmField val SAND_RED = FabricTags.Items.SAND_RED
        @JvmField val SANDSTONE = FabricTags.Items.SANDSTONE
        @JvmField val SEEDS = FabricTags.Items.SEEDS
        @JvmField val SEEDS_BEETROOT = FabricTags.Items.SEEDS_BEETROOT
        @JvmField val SEEDS_MELON = FabricTags.Items.SEEDS_MELON
        @JvmField val SEEDS_PUMPKIN = FabricTags.Items.SEEDS_PUMPKIN
        @JvmField val SEEDS_WHEAT = FabricTags.Items.SEEDS_WHEAT
        @JvmField val SHEARS = FabricTags.Items.SHEARS
        @JvmField val SLIMEBALLS = FabricTags.Items.SLIMEBALLS
        @JvmField val STAINED_GLASS = FabricTags.Items.STAINED_GLASS
        @JvmField val STAINED_GLASS_PANES = FabricTags.Items.STAINED_GLASS_PANES
        @JvmField val STONE = FabricTags.Items.STONE
        @JvmField val STORAGE_BLOCKS = FabricTags.Items.STORAGE_BLOCKS
        @JvmField val STORAGE_BLOCKS_AMETHYST = FabricTags.Items.STORAGE_BLOCKS_AMETHYST
        @JvmField val STORAGE_BLOCKS_COAL = FabricTags.Items.STORAGE_BLOCKS_COAL
        @JvmField val STORAGE_BLOCKS_COPPER = FabricTags.Items.STORAGE_BLOCKS_COPPER
        @JvmField val STORAGE_BLOCKS_DIAMOND = FabricTags.Items.STORAGE_BLOCKS_DIAMOND
        @JvmField val STORAGE_BLOCKS_EMERALD = FabricTags.Items.STORAGE_BLOCKS_EMERALD
        @JvmField val STORAGE_BLOCKS_GOLD = FabricTags.Items.STORAGE_BLOCKS_GOLD
        @JvmField val STORAGE_BLOCKS_IRON = FabricTags.Items.STORAGE_BLOCKS_IRON
        @JvmField val STORAGE_BLOCKS_LAPIS = FabricTags.Items.STORAGE_BLOCKS_LAPIS
        @JvmField val STORAGE_BLOCKS_NETHERITE = FabricTags.Items.STORAGE_BLOCKS_NETHERITE
        @JvmField val STORAGE_BLOCKS_QUARTZ = FabricTags.Items.STORAGE_BLOCKS_QUARTZ
        @JvmField val STORAGE_BLOCKS_RAW_COPPER = FabricTags.Items.STORAGE_BLOCKS_RAW_COPPER
        @JvmField val STORAGE_BLOCKS_RAW_GOLD = FabricTags.Items.STORAGE_BLOCKS_RAW_GOLD
        @JvmField val STORAGE_BLOCKS_RAW_IRON = FabricTags.Items.STORAGE_BLOCKS_RAW_IRON
        @JvmField val STORAGE_BLOCKS_REDSTONE = FabricTags.Items.STORAGE_BLOCKS_REDSTONE
        @JvmField val TOOLS = FabricTags.Items.TOOLS
        @JvmField val TOOLS_SWORDS = FabricTags.Items.TOOLS_SWORDS
        @JvmField val TOOLS_AXES = FabricTags.Items.TOOLS_AXES
        @JvmField val TOOLS_PICKAXES = FabricTags.Items.TOOLS_PICKAXES
        @JvmField val TOOLS_SHOVELS = FabricTags.Items.TOOLS_SHOVELS
        @JvmField val TOOLS_HOES = FabricTags.Items.TOOLS_HOES
        @JvmField val TOOLS_SHIELDS = FabricTags.Items.TOOLS_SHIELDS
        @JvmField val TOOLS_BOWS = FabricTags.Items.TOOLS_BOWS
        @JvmField val TOOLS_CROSSBOWS = FabricTags.Items.TOOLS_CROSSBOWS
        @JvmField val TOOLS_FISHING_RODS = FabricTags.Items.TOOLS_FISHING_RODS
        @JvmField val TOOLS_TRIDENTS = FabricTags.Items.TOOLS_TRIDENTS
        @JvmField val ARMORS = FabricTags.Items.ARMORS
        @JvmField val ARMORS_HELMETS = FabricTags.Items.ARMORS_HELMETS
        @JvmField val ARMORS_CHESTPLATES = FabricTags.Items.ARMORS_CHESTPLATES
        @JvmField val ARMORS_LEGGINGS = FabricTags.Items.ARMORS_LEGGINGS
        @JvmField val ARMORS_BOOTS = FabricTags.Items.ARMORS_BOOTS
        @JvmField val STRING = FabricTags.Items.STRING
    }

    object Fluids {
        internal fun init() {}

        @JvmField val MILK = FabricTags.Fluids.MILK
        @JvmField val GASEOUS = FabricTags.Fluids.GASEOUS
    }

    object Biomes {
        internal fun init() {}

        @JvmField val IS_HOT = FabricTags.Biomes.IS_HOT
        @JvmField val IS_HOT_OVERWORLD = FabricTags.Biomes.IS_HOT_OVERWORLD
        @JvmField val IS_HOT_NETHER = FabricTags.Biomes.IS_HOT_NETHER
        @JvmField val IS_HOT_END = FabricTags.Biomes.IS_HOT_END
        @JvmField val IS_COLD = FabricTags.Biomes.IS_COLD
        @JvmField val IS_COLD_OVERWORLD = FabricTags.Biomes.IS_COLD_OVERWORLD
        @JvmField val IS_COLD_NETHER = FabricTags.Biomes.IS_COLD_NETHER
        @JvmField val IS_COLD_END = FabricTags.Biomes.IS_COLD_END
        @JvmField val IS_SPARSE = FabricTags.Biomes.IS_SPARSE
        @JvmField val IS_SPARSE_OVERWORLD = FabricTags.Biomes.IS_SPARSE_OVERWORLD
        @JvmField val IS_SPARSE_NETHER = FabricTags.Biomes.IS_SPARSE_NETHER
        @JvmField val IS_SPARSE_END = FabricTags.Biomes.IS_SPARSE_END
        @JvmField val IS_DENSE = FabricTags.Biomes.IS_DENSE
        @JvmField val IS_DENSE_OVERWORLD = FabricTags.Biomes.IS_DENSE_OVERWORLD
        @JvmField val IS_DENSE_NETHER = FabricTags.Biomes.IS_DENSE_NETHER
        @JvmField val IS_DENSE_END = FabricTags.Biomes.IS_DENSE_END
        @JvmField val IS_WET = FabricTags.Biomes.IS_WET
        @JvmField val IS_WET_OVERWORLD = FabricTags.Biomes.IS_WET_OVERWORLD
        @JvmField val IS_WET_NETHER = FabricTags.Biomes.IS_WET_NETHER
        @JvmField val IS_WET_END = FabricTags.Biomes.IS_WET_END
        @JvmField val IS_DRY = FabricTags.Biomes.IS_DRY
        @JvmField val IS_DRY_OVERWORLD = FabricTags.Biomes.IS_DRY_OVERWORLD
        @JvmField val IS_DRY_NETHER = FabricTags.Biomes.IS_DRY_NETHER
        @JvmField val IS_DRY_END = FabricTags.Biomes.IS_DRY_END
        @JvmField val IS_CONIFEROUS = FabricTags.Biomes.IS_CONIFEROUS
        @JvmField val IS_SPOOKY = FabricTags.Biomes.IS_SPOOKY
        @JvmField val IS_DEAD = FabricTags.Biomes.IS_DEAD
        @JvmField val IS_LUSH = FabricTags.Biomes.IS_LUSH
        @JvmField val IS_MUSHROOM = FabricTags.Biomes.IS_MUSHROOM
        @JvmField val IS_MAGICAL = FabricTags.Biomes.IS_MAGICAL
        @JvmField val IS_RARE = FabricTags.Biomes.IS_RARE
        @JvmField val IS_PLATEAU = FabricTags.Biomes.IS_PLATEAU
        @JvmField val IS_MODIFIED = FabricTags.Biomes.IS_MODIFIED
        @JvmField val IS_WATER = FabricTags.Biomes.IS_WATER
        @JvmField val IS_DESERT = tag("is_desert")
        @JvmField val IS_PLAINS = FabricTags.Biomes.IS_PLAINS
        @JvmField val IS_SWAMP = FabricTags.Biomes.IS_SWAMP
        @JvmField val IS_SANDY = FabricTags.Biomes.IS_SANDY
        @JvmField val IS_SNOWY = FabricTags.Biomes.IS_SNOWY
        @JvmField val IS_WASTELAND = FabricTags.Biomes.IS_WASTELAND
        @JvmField val IS_VOID = FabricTags.Biomes.IS_VOID
        @JvmField val IS_UNDERGROUND = FabricTags.Biomes.IS_UNDERGROUND
        @JvmField val IS_CAVE = tag("is_cave")
        @JvmField val IS_PEAK = FabricTags.Biomes.IS_PEAK
        @JvmField val IS_SLOPE = FabricTags.Biomes.IS_SLOPE
        @JvmField val IS_MOUNTAIN = FabricTags.Biomes.IS_MOUNTAIN

        fun tag(name: String): TagKey<Block> {
            return TagKey.create(Registry.BLOCK_REGISTRY, ResourceLocation("c", name))
        }
    }
}
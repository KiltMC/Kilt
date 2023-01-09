package net.minecraftforge.common

object ToolActions {
    @JvmField
    val AXE_DIG = ToolAction.get("axe_dig")

    @JvmField
    val PICKAXE_DIG = ToolAction.get("pickaxe_dig")

    @JvmField
    val SHOVEL_DIG = ToolAction.get("shovel_dig")

    @JvmField
    val HOE_DIG = ToolAction.get("hoe_dig")

    @JvmField
    val SWORD_DIG = ToolAction.get("sword_dig")

    @JvmField
    val SHEARS_DIG = ToolAction.get("shears_dig")

    @JvmField
    val AXE_STRIP = ToolAction.get("axe_strip")

    @JvmField
    val AXE_SCRAPE = ToolAction.get("axe_scrape")

    @JvmField
    val AXE_WAX_OFF = ToolAction.get("axe_wax_off")

    @JvmField
    val SHOVEL_FLATTEN = ToolAction.get("shovel_flatten")

    @JvmField
    val SWORD_SWEEP = ToolAction.get("sword_sweep")

    @JvmField
    val SHEARS_HARVEST = ToolAction.get("shears_harvest")

    @JvmField
    val SHEARS_CARVE = ToolAction.get("shears_carve")

    @JvmField
    val SHEARS_DISARM = ToolAction.get("shears_disarm")

    @JvmField
    val HOE_TILL = ToolAction.get("till")

    @JvmField
    val SHIELD_BLOCK = ToolAction.get("shield_block")

    @JvmField
    val FISHING_ROD_CAST = ToolAction.get("fishing_rod_cast")

    @JvmField
    val DEFAULT_AXE_ACTIONS = setOf(AXE_DIG, AXE_STRIP, AXE_SCRAPE, AXE_WAX_OFF)

    @JvmField
    val DEFAULT_HOE_ACTIONS = setOf(HOE_DIG, HOE_TILL)

    @JvmField
    val DEFAULT_SHOVEL_ACTIONS = setOf(SHOVEL_DIG, SHOVEL_FLATTEN)

    @JvmField
    val DEFAULT_PICKAXE_ACTIONS = setOf(PICKAXE_DIG)

    @JvmField
    val DEFAULT_SWORD_ACTIONS = setOf(SWORD_DIG, SWORD_SWEEP)

    @JvmField
    val DEFAULT_SHEARS_ACTIONS = setOf(SHEARS_CARVE, SHEARS_DIG, SHEARS_DISARM, SHEARS_HARVEST)

    @JvmField
    val DEFAULT_SHIELD_ACTIONS = setOf(SHIELD_BLOCK)

    @JvmField
    val DEFAULT_FISHING_ROD_ACTIONS = setOf(FISHING_ROD_CAST)
}
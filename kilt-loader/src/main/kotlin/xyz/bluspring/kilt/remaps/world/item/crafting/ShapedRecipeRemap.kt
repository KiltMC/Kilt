package xyz.bluspring.kilt.remaps.world.item.crafting

object ShapedRecipeRemap {
    @JvmField var MAX_WIDTH = 3
    @JvmField var MAX_HEIGHT = 3

    @JvmStatic
    fun setCraftingSize(width: Int, height: Int) {
        if (MAX_WIDTH < width)
            MAX_WIDTH = width

        if (MAX_HEIGHT < height)
            MAX_HEIGHT = height
    }
}
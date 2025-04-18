package xyz.bluspring.kilt.compat.curios_trinkets

enum class GuiMode {
    TRINKETS,
    CURIOS;

    val isTrinkets: Boolean
        get() = this == TRINKETS

    val isCurios: Boolean
        get() = this == CURIOS
}
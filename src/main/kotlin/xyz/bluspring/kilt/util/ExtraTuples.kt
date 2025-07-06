package xyz.bluspring.kilt.util

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D) {
    override fun toString(): String {
        return "($first, $second, $third, $fourth)"
    }
}
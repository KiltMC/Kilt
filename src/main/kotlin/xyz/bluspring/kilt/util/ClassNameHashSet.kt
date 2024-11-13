package xyz.bluspring.kilt.util

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet

class ClassNameHashSet : ObjectOpenCustomHashSet<String>(ClassNameStrategy) {
    object ClassNameStrategy : Hash.Strategy<String> {
        override fun equals(a: String?, b: String?): Boolean {
            return a == b || a?.replace('/', '.') == b?.replace('/', '.')
        }

        override fun hashCode(o: String?): Int {
            return o?.replace('/', '.')?.hashCode() ?: 0
        }
    }
}
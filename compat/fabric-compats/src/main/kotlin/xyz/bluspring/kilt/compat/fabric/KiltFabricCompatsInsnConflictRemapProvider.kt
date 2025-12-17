package xyz.bluspring.kilt.compat.fabric

import xyz.bluspring.kilt.api.remapping.InsnConflictRemapProvider

class KiltFabricCompatsInsnConflictRemapProvider : InsnConflictRemapProvider {
    override fun remapMethod(owner: String, name: String, descriptor: String): String {
        if (owner == "virtuoel/pehkui/api/ScaleType") {
            if ((name == "getScaleChangedEvent" || name == "getPreTickEvent" || name == "getPostTickEvent") && descriptor == "Ljava/util/Collection;") {
                return $$"kilt$pehkui$$$name"
            }
        }

        return super.remapMethod(owner, name, descriptor)
    }
}
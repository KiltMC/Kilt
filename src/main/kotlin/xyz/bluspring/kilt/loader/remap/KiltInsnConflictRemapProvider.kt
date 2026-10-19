package xyz.bluspring.kilt.loader.remap

import net.fabricmc.loader.api.FabricLoader
import xyz.bluspring.kilt.api.remapping.InsnConflictRemapProvider

class KiltInsnConflictRemapProvider : InsnConflictRemapProvider {
    private val mappingResolver = FabricLoader.getInstance().mappingResolver

    override fun remapMethod(owner: String, name: String, descriptor: String): String {
        when (owner) {
            "net/minecraft/world/effect/MobEffectInstance" ->
                when (name) {
                    "getCures" -> return $$"neoforge$getCures"
                }

            "net/minecraft/server/level/ServerEntity" ->
                when (name) {
                    "sendPairingData" -> return $$"neoforge$sendPairingData"
                }

            "net/minecraft/world/item/alchemy/PotionBrewing" -> {
                when (name) {
                    "getRecipes" -> return $$"neoforge$getRecipes"
                }
            }
        }

        return super.remapMethod(owner, name, descriptor)
    }

    override fun remapField(owner: String, name: String, descriptor: String): String {
        return super.remapField(owner, name, descriptor)
    }
}

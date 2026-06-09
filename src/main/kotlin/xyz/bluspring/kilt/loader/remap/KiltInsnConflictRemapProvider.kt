package xyz.bluspring.kilt.loader.remap

import net.fabricmc.loader.api.FabricLoader
import xyz.bluspring.kilt.api.remapping.InsnConflictRemapProvider

class KiltInsnConflictRemapProvider : InsnConflictRemapProvider {
    private val mappingResolver = FabricLoader.getInstance().mappingResolver

    private val potionBrewingMapped = KiltRemapper.remapClass("net/minecraft/world/item/alchemy/PotionBrewing\$Mix")
    private val pbFromMapped = mappingResolver.mapFieldName("intermediary", "net.minecraft.class_1845\$class_1846", "field_8962", "Ljava/lang/Object;")
    private val pbToMapped = mappingResolver.mapFieldName("intermediary", "net.minecraft.class_1845\$class_1846", "field_8961", "Ljava/lang/Object;")

    override fun remapMethod(owner: String, name: String, descriptor: String): String {
        when (owner) {
            "net/minecraftforge/fluids/FluidStack" ->
                when (name) {
                    "getAmount" -> return $$"forge$getAmount"
                    "writeToPacket" -> return $$"forge$writeToPacket"
                }

            "net/minecraft/world/effect/MobEffectInstance", KiltRemapper.remapClass("net/minecraft/world/effect/MobEffectInstance") ->
                when (name) {
                    "getCures" -> return $$"neoforge$getCures"
                }

            "net/minecraft/server/level/ServerEntity", KiltRemapper.remapClass("net/minecraft/server/level/ServerEntity") ->
                when (name) {
                    "sendPairingData" -> return $$"neoforge$sendPairingData"
                }
        }

        return super.remapMethod(owner, name, descriptor)
    }

    override fun remapField(owner: String, name: String, descriptor: String): String {
        return super.remapField(owner, name, descriptor)
    }
}

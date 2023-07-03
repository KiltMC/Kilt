package xyz.bluspring.kilt.loader.mod.fabric

import net.fabricmc.loader.impl.ModContainerImpl
import xyz.bluspring.kilt.loader.mod.ForgeMod

class KiltFabricModContainer(mod: ForgeMod) : ModContainerImpl(FabricModProvider.instance.createModCandidate(mod)) {
}
package xyz.bluspring.kilt.loader.mixin

import net.fabricmc.loader.impl.util.mappings.MixinIntermediaryDevRemapper
import xyz.bluspring.kilt.loader.remap.KiltRemapper

class MixinSrgRemapper : MixinIntermediaryDevRemapper(KiltRemapper.srgIntermediaryTree, "searge", "intermediary")
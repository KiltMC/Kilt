package xyz.bluspring.kilt.loader.mixin.modifications.modifiers

import com.llamalad7.mixinextras.sugar.Share
import org.objectweb.asm.tree.AnnotationNode
import xyz.bluspring.kilt.loader.mixin.modifications.KiltMixinModifications
import xyz.bluspring.kilt.loader.mixin.modifications.ParamPair

interface MixinModifier {
    val owner: String
    var mappedOwner: String
}
package xyz.bluspring.kilt.loader.remap

import com.llamalad7.mixinextras.sugar.Local
import com.llamalad7.mixinextras.sugar.ref.*
import org.objectweb.asm.Type
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Overwrite
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.gen.Accessor
import org.spongepowered.asm.mixin.gen.Invoker
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

object MixinTypes {
    // Mixin annotations
    @JvmField val MIXIN: Type = Type.getType(Mixin::class.java)
    @JvmField val ACCESSOR: Type = Type.getType(Accessor::class.java)
    @JvmField val INVOKER: Type = Type.getType(Invoker::class.java)
    @JvmField val SHADOW: Type = Type.getType(Shadow::class.java)
    @JvmField val OVERWRITE: Type = Type.getType(Overwrite::class.java)
    @JvmField val INJECT: Type = Type.getType(Inject::class.java)

    // MixinExtras annotations
    @JvmField val LOCAL: Type = Type.getType(Local::class.java)

    // Mixin types used in annotations
    @JvmField val AT_SHIFT: Type = Type.getType(At.Shift::class.java)

    // MixinExtras types
    @JvmField val LOCAL_BOOLEAN_REF: Type = Type.getType(LocalBooleanRef::class.java)
    @JvmField val LOCAL_CHAR_REF: Type = Type.getType(LocalCharRef::class.java)
    @JvmField val LOCAL_BYTE_REF: Type = Type.getType(LocalByteRef::class.java)
    @JvmField val LOCAL_SHORT_REF: Type = Type.getType( LocalShortRef::class.java)
    @JvmField val LOCAL_INT_REF: Type = Type.getType(LocalIntRef::class.java)
    @JvmField val LOCAL_LONG_REF: Type = Type.getType(LocalLongRef::class.java)
    @JvmField val LOCAL_DOUBLE_REF: Type = Type.getType(LocalDoubleRef::class.java)
    @JvmField val LOCAL_FLOAT_REF: Type = Type.getType(LocalFloatRef::class.java)
    @JvmField val LOCAL_REF: Type = Type.getType(LocalRef::class.java)

    // Mixin types
    @JvmField val CALLBACK_INFO: Type = Type.getType(CallbackInfo::class.java)
    @JvmField val CALLBACK_INFO_RETURNABLE: Type = Type.getType(CallbackInfoReturnable::class.java)

    // MixinExtras wrappers
    @JvmField val SUGAR_WRAPPER: Type = Type.getType("Lcom/llamalad7/mixinextras/sugar/impl/SugarWrapper;")
    @JvmField val FACTORY_REDIRECT_WRAPPER: Type = Type.getType("Lcom/llamalad7/mixinextras/wrapper/factory/FactoryRedirectWrapper;")
}

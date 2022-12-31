package net.minecraftforge.fml.loading.moddiscovery

import net.minecraftforge.forgespi.language.ModFileScanData
import org.objectweb.asm.Type
import java.lang.annotation.ElementType

class ModAnnotation(val type: ElementType, private val asmType: Type, val member: String) {
    private var arrayList: ArrayList<Any>? = null
    private var arrayName: String? = null
    val values = mutableMapOf<String, Any>()

    constructor(asmType: Type, parent: ModAnnotation) : this(parent.type, asmType, parent.member)

    fun getASMType(): Type {
        return asmType
    }

    fun addArray(name: String) {
        arrayList = ArrayList()
        arrayName = name
    }

    fun addChildAnnotation(name: String, desc: String): ModAnnotation {
        val child = ModAnnotation(Type.getType(desc), this)
        addProperty(name, child.values)

        return child
    }

    fun addEnumProperty(key: String, enumName: String, value: String) {
        addProperty(key, EnumHolder(enumName, value))
    }

    fun addProperty(key: String, value: Any) {
        if (arrayList != null) {
            arrayList!!.add(value)
        } else {
            values[key] = value
        }
    }

    fun endArray() {
        values[arrayName!!] = arrayList!!
        arrayList = null
    }

    companion object {
        @JvmStatic
        fun fromModAnnotation(clazz: Type, annotation: ModAnnotation): ModFileScanData.AnnotationData {
            return ModFileScanData.AnnotationData(
                annotation.asmType, annotation.type, clazz, annotation.member, annotation.values
            )
        }
    }

    class EnumHolder(val desc: String, val value: String)
}
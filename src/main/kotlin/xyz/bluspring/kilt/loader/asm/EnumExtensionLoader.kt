package xyz.bluspring.kilt.loader.asm

import com.chocohead.mm.api.ClassTinkerers
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.fabricmc.loader.api.FabricLoader
import net.neoforged.fml.common.asm.enumextension.*
import net.neoforged.fml.common.asm.enumextension.NetworkedEnum.NetworkCheck
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import org.spongepowered.asm.util.Annotations
import xyz.bluspring.kilt.loader.mod.NeoForgeMod
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import java.nio.file.Files
import java.util.*
import java.util.function.Consumer

object EnumExtensionLoader {

    val enumExtensions = mutableMapOf<String, MutableSet<EnumPrototype>>()
    val proxies = mutableMapOf<String, MutableMap<String, EnumParameters.FieldReference>>()
    val indexed = mutableMapOf<String, Int>()

    private fun remapPrototype(prototype: EnumPrototype): EnumPrototype {
        return EnumPrototype(
            prototype.owningMod,
            KiltRemapper.remapClass(prototype.enumName, toIntermediary = !FabricLoader.getInstance().isDevelopmentEnvironment),
            prototype.fieldName,
            KiltRemapper.remapDescriptor(prototype.ctorDesc, toIntermediary = !FabricLoader.getInstance().isDevelopmentEnvironment),
            KiltRemapper.remapDescriptor(prototype.fullCtorDesc, toIntermediary = !FabricLoader.getInstance().isDevelopmentEnvironment),
            prototype.ctorParams
        )
    }

    fun loadEnumExtension(mod: NeoForgeMod) {
        mod.config.getConfigList("mods").forEach { config ->
            config.getConfigElement<String>("enumExtensions").ifPresent{ file ->
                val path = mod.owningFile.getFile().findResource(file)
                if (Files.isRegularFile(path)) {
                    EnumPrototype.load(mod, path).forEach{ prototype ->
                        val usablePrototype = remapPrototype(prototype)
                        enumExtensions.computeIfAbsent(
                            usablePrototype.enumName,
                            { TreeSet() }
                        ).add(usablePrototype)
                    }
                }
            }
        }
    }

    private fun <T> forceSetEnumProxyValue(proxy: EnumProxy<T>, value: Any) where T: Enum<T>, T: IExtensibleEnum {
        @Suppress("UNCHECKED_CAST")
        proxy.value = value as T
    }

    //Automatically invoked by enums after enum extension, DO NOT DELETE!
    @Suppress("unused")
    fun setProxyValue(enumName: String, fieldName: String, enum: Any) {
        if (enum !is Enum<*> || enum !is IExtensibleEnum) {
            return
        }
        val proxyField = proxies[enumName]?.get(fieldName)
        if (proxyField != null) {
            val proxy = getEnumProxy(proxyField)
            forceSetEnumProxyValue(proxy, enum)
        }
    }

    private fun getEnumProxy(fieldReference: EnumParameters.FieldReference): EnumProxy<*> {
        // Luckily for us, NeoForge crashes unless these are public.
        // This is good because getDeclaredField might crash the game on servers.
        return Class.forName(fieldReference.owner.className).getField(fieldReference.fieldName).get(null) as EnumProxy<*>
    }

    private fun getCastClass(type: Type): Class<*> {
        return when (type.sort) {
            Type.VOID -> Void.TYPE
            Type.BOOLEAN -> java.lang.Boolean.TYPE
            Type.BYTE -> java.lang.Byte.TYPE
            Type.SHORT -> java.lang.Short.TYPE
            Type.INT -> Integer.TYPE
            Type.LONG -> java.lang.Long.TYPE
            Type.FLOAT -> java.lang.Float.TYPE
            Type.DOUBLE -> java.lang.Double.TYPE
            Type.CHAR -> Character.TYPE
            Type.ARRAY -> Class.forName(type.internalName.replace("/", "."))
            Type.OBJECT -> Class.forName(type.className)
            else -> throw IllegalStateException("Could not find the class for $type")
        }
    }

    fun applyEnumExtensions() {
        enumExtensions.forEach { (name, prototypes) ->
            for (prototype in prototypes) {
                val descriptor = Type.getType(prototype.ctorDesc)
                val parameters = prototype.ctorParams
                if (parameters is EnumParameters.FieldReference) {
                    proxies.computeIfAbsent(prototype.enumName, {mutableMapOf()})[prototype.fieldName] = parameters
                }
                ClassTinkerers.enumBuilder(
                    prototype.enumName, *descriptor.argumentTypes
                ).addEnum(
                    prototype.fieldName, {
                        when (parameters) {
                            is EnumParameters.Constant -> parameters.params
                            is EnumParameters.FieldReference -> {
                                val proxy = getEnumProxy(parameters)
                                val args = mutableListOf<Any>()
                                for (i in 0..< descriptor.argumentCount) {
                                    args.add(proxy.getParameter(i))
                                }
                                args
                            }
                            is EnumParameters.MethodReference -> {
                                // Luckily for us, NeoForge crashes unless these are public.
                                // This is good because getDeclaredMethod might crash the game on servers.
                                val method = Class.forName(parameters.owner.className).getMethod(parameters.methodName, Integer.TYPE, Class::class.java)
                                val args = mutableListOf<Any>()
                                for (i in 0..< descriptor.argumentCount) {
                                    if (indexed[name] == i) {
                                        args.add(-1)
                                    } else {
                                        args.add(method.invoke(null, i, getCastClass(descriptor.argumentTypes[i])))
                                    }
                                }
                                args
                            }
                        }.toTypedArray()
                    }
                ).build()
            }
        }
    }

    val INITIAL_ENUM_COUNTS: Object2IntMap<String?> = Object2IntOpenHashMap<String?>()
    const val EXTENSIBLE_ENUM: String = "net/neoforged/fml/common/asm/enumextension/IExtensibleEnum"

    private fun countEnumFields(classNode: ClassNode): Int {
        var count = 0
        for (field in classNode.fields) {
            if (field.desc == "L" + classNode.name + ";" && (field.access and Opcodes.ACC_STATIC) != 0) {
                count++
            }
        }
        return count
    }

    fun preApplyEnum(targetClass: ClassNode, mixinClass: ClassNode) {
        if (mixinClass.interfaces.contains(EXTENSIBLE_ENUM)) {
            // Note how many enum constants exist before mixin injection.
            INITIAL_ENUM_COUNTS.put(targetClass.name, countEnumFields(targetClass))

            // Handle IndexedEnums since NeoForge mods always set index to -1.
            // I decided to set this parameter to the same value as ordinal for all values since there isn't really any good reason to allow Fabric mods to set their own values here.
            val indexed = Annotations.getInvisible(mixinClass, IndexedEnum::class.java)
            if (indexed != null) {
                var index = Annotations.getValue<Number?>(indexed)
                if (index == null) {
                    index = 0
                }
				EnumExtensionLoader.indexed[targetClass.name] = index.toInt()
				val fixedIndex = index.toInt()
                targetClass.methods.stream().filter { method: MethodNode? -> method!!.name == "<init>" }
                    .forEach { constructor: MethodNode? ->
                        val list = InsnList()
                        list.add(LabelNode())
                        list.add(VarInsnNode(Opcodes.ILOAD, 2))
                        list.add(VarInsnNode(Opcodes.ISTORE, 3 + fixedIndex))
                        constructor!!.instructions.insertBefore(constructor.instructions.getFirst(), list)
                    }
            }
        }
    }

    fun postApplyEnum(targetClass: ClassNode, mixinClass: ClassNode) {
        if (mixinClass.interfaces.contains(EXTENSIBLE_ENUM)) {
            val clinit =
                targetClass.methods.stream().filter { method: MethodNode? -> method!!.name == "<clinit>" }.findFirst()
                    .orElseThrow()

            // Fill all EnumProxy instances.
            val proxies = proxies.get(targetClass.name)
			proxies?.keys?.forEach(Consumer { fieldName: String? ->
				val fillProxy = InsnList()
				fillProxy.add(LabelNode())
				val enumExtensionLoader = "xyz/bluspring/kilt/loader/asm/EnumExtensionLoader"
				fillProxy.add(
					FieldInsnNode(
						Opcodes.GETSTATIC,
						enumExtensionLoader,
						"INSTANCE",
						"L$enumExtensionLoader;"
					)
				)
				fillProxy.add(LdcInsnNode(targetClass.name))
				fillProxy.add(LdcInsnNode(fieldName))
				fillProxy.add(
					FieldInsnNode(
						Opcodes.GETSTATIC,
						targetClass.name,
						fieldName,
						"L" + targetClass.name + ";"
					)
				)
				fillProxy.add(
					MethodInsnNode(
						Opcodes.INVOKEVIRTUAL,
						enumExtensionLoader,
						"setProxyValue",
						"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)V"
					)
				)
				clinit.instructions.insertBefore(clinit.instructions.getLast(), fillProxy)
			})

            // Overwrite getExtensionInfo
            val currentCount = countEnumFields(targetClass)
            val vanillaCount = INITIAL_ENUM_COUNTS.getOrDefault(targetClass.name, 0)
            if (currentCount != vanillaCount) {
                var networkCheck: NetworkCheck? = null
                val networked = Annotations.getVisible(mixinClass, NetworkedEnum::class.java)
                if (networked != null) {
                    networkCheck = NetworkCheck.valueOf(Annotations.getValue<Array<String?>>(networked)[1]!!)
                }
                val extensionInfoName = "net/neoforged/fml/common/asm/enumextension/ExtensionInfo"
                val extensionInfoDesc = "L$extensionInfoName;"
                val extensionInfoField = "kilt\$extensionInfo"
                targetClass.fields.add(
                    FieldNode(
                        Opcodes.ACC_PRIVATE or Opcodes.ACC_STATIC or Opcodes.ACC_FINAL, extensionInfoField,
                        extensionInfoDesc, extensionInfoDesc, null
                    )
                )
                val fieldSetup = InsnList()
                fieldSetup.add(LabelNode())
                fieldSetup.add(TypeInsnNode(Opcodes.NEW, extensionInfoName))
                fieldSetup.add(InsnNode(Opcodes.DUP))
                fieldSetup.add(InsnNode(Opcodes.ICONST_1)) // Set extended to true.
                fieldSetup.add(pushInt(vanillaCount))
                fieldSetup.add(pushInt(currentCount))

                if (networkCheck == null) {
                    fieldSetup.add(InsnNode(Opcodes.ACONST_NULL))
                } else {
                    val networkCheckClassName = NetworkCheck::class.java.getName().replace(".", "/")
                    fieldSetup.add(
                        FieldInsnNode(
                            Opcodes.GETSTATIC,
                            networkCheckClassName,
                            networkCheck.name,
							"L$networkCheckClassName;"
                        )
                    )
                }

                fieldSetup.add(
                    MethodInsnNode(
                        Opcodes.INVOKESPECIAL,
                        extensionInfoName,
                        "<init>",
                        "(ZIILnet/neoforged/fml/common/asm/enumextension/NetworkedEnum\$NetworkCheck;)V"
                    )
                )

                fieldSetup.add(
                    FieldInsnNode(
                        Opcodes.PUTSTATIC,
                        targetClass.name,
                        extensionInfoField,
                        extensionInfoDesc
                    )
                )
                if (clinit.maxStack < 6) {
                    clinit.maxStack = 6
                }
                clinit.instructions.insertBefore(clinit.instructions.getLast(), fieldSetup)
                val getExtensionInfo = targetClass.methods.stream().filter { method: MethodNode? ->
                    method!!.name == "getExtensionInfo" &&
                            method.desc == "()$extensionInfoDesc" &&
                            (method.access and Opcodes.ACC_STATIC) != 0
                }.findFirst().orElseThrow()
                getExtensionInfo.instructions.clear()
                getExtensionInfo.instructions.add(LabelNode())
                getExtensionInfo.instructions.add(
                    FieldInsnNode(
                        Opcodes.GETSTATIC,
                        targetClass.name,
                        extensionInfoField,
                        extensionInfoDesc
                    )
                )
                getExtensionInfo.instructions.add(InsnNode(Opcodes.ARETURN))
                getExtensionInfo.maxStack = 1
            }
        }
    }

    private fun pushInt(num: Int): AbstractInsnNode {
        return when (num) {
            -1 -> InsnNode(Opcodes.ICONST_M1)
            0 -> InsnNode(Opcodes.ICONST_0)
            1 -> InsnNode(Opcodes.ICONST_1)
            2 -> InsnNode(Opcodes.ICONST_2)
            3 -> InsnNode(Opcodes.ICONST_3)
            4 -> InsnNode(Opcodes.ICONST_4)
            5 -> InsnNode(Opcodes.ICONST_5)
            else -> IntInsnNode(Opcodes.BIPUSH, num)
        }
    }
    
}
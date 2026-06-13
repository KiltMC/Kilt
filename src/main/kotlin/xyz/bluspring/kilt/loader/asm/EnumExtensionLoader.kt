package xyz.bluspring.kilt.loader.asm

import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.neoforged.fml.common.asm.enumextension.*
import net.neoforged.fml.common.asm.enumextension.NetworkedEnum.NetworkCheck
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import org.spongepowered.asm.util.Annotations
import xyz.bluspring.fork.mm.api.ClassTinkerers
import xyz.bluspring.kilt.loader.mod.NeoForgeMod
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import java.nio.file.Files
import java.util.*
import java.util.function.Consumer

object EnumExtensionLoader {

    val enumExtensions = mutableSetOf<EnumPrototype>()
    val proxies = mutableMapOf<String, MutableMap<String, EnumParameters.FieldReference>>()
    val indexed = mutableMapOf<String, Int>()

    private val insnListSizeField = InsnList::class.java.getDeclaredField("size").apply {
        isAccessible = true
    }

    private fun remapPrototype(prototype: EnumPrototype): EnumPrototype {
        return EnumPrototype(
            prototype.owningMod,
            (prototype.enumName),
            prototype.fieldName,
            KiltRemapper.remapDescriptor(prototype.ctorDesc),
            KiltRemapper.remapDescriptor(prototype.fullCtorDesc),
            prototype.ctorParams
        )
    }

    fun loadEnumExtension(mod: NeoForgeMod) {
        mod.config.getConfigList("mods").forEach { config ->
            // Prevent mods from loading enum extensions that aren't theirs.
            val modId = config.getConfigElement<String>("modId")
            if (modId.isEmpty || modId.get() != mod.modId) {
                return@forEach
            }

            config.getConfigElement<String>("enumExtensions").ifPresent { file ->
                val path = mod.owningFile.getFile().findResource(file)
                if (Files.isRegularFile(path)) {
                    EnumPrototype.load(mod, path).forEach { prototype ->
                        val usablePrototype = remapPrototype(prototype)
                        enumExtensions.add(usablePrototype)
                    }
                }
            }
        }
    }

    private fun <T> forceSetEnumProxyValue(proxy: EnumProxy<T>, value: Any) where T : Enum<T>, T : IExtensibleEnum {
        @Suppress("UNCHECKED_CAST")
        proxy.value = value as T
    }


    //Keep this constant close to the function below.
    val SET_PROXY_VALUE_TARGET_CLASS = javaClass.name.replace(".", "/")
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

    private fun getCastClass(type: Type): Class<*>? {
        return when (type.sort) {
            Type.VOID -> Void::class.javaPrimitiveType
            Type.BOOLEAN -> Boolean::class.javaPrimitiveType
            Type.BYTE -> Byte::class.javaPrimitiveType
            Type.SHORT -> Short::class.javaPrimitiveType
            Type.INT -> Int::class.javaPrimitiveType
            Type.LONG -> Long::class.javaPrimitiveType
            Type.FLOAT -> Float::class.javaPrimitiveType
            Type.DOUBLE -> Double::class.javaPrimitiveType
            Type.CHAR -> Char::class.javaPrimitiveType
            Type.ARRAY -> Class.forName(type.internalName.replace("/", "."))
            Type.OBJECT -> Class.forName(type.className)
            else -> throw IllegalStateException("Could not find the class for $type")
        }
    }

    fun applyEnumExtensions() {
        for (prototype in enumExtensions) {
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
                            val args = mutableListOf<Any?>()
                            for (i in 0 until descriptor.argumentCount) {
                                args.add(proxy.getParameter(i))
                            }
                            args
                        }
                        is EnumParameters.MethodReference -> {
                            // Luckily for us, NeoForge crashes unless these are public.
                            // This is good because getDeclaredMethod might crash the game on servers.
                            val method = Class.forName(parameters.owner.className).getMethod(parameters.methodName, Integer.TYPE, Class::class.java)
                            val args = mutableListOf<Any?>()
                            for (i in 0 until descriptor.argumentCount) {
                                if (indexed[prototype.enumName] == i) {
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

    val INITIAL_ENUM_COUNTS: Object2IntMap<String> = Object2IntOpenHashMap()
    const val EXTENSIBLE_ENUM: String = "net/neoforged/fml/common/asm/enumextension/IExtensibleEnum"

    private fun countEnumFields(classNode: ClassNode): Int {
        var count = 0
        for (field in classNode.fields) {
            if (field.desc == "L${classNode.name};" && (field.access and Opcodes.ACC_STATIC) != 0) {
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
                var index = Annotations.getValue<Int>(indexed)
                if (index == null) {
                    index = 0
                }
				EnumExtensionLoader.indexed[targetClass.name] = index
				targetClass.methods.filter { method -> method.name == "<init>" }
                    .forEach { constructor ->
                        val list = InsnList()
                        list.add(LabelNode())
                        list.add(VarInsnNode(Opcodes.ILOAD, 2))
                        list.add(VarInsnNode(Opcodes.ISTORE, 3 + index))
                        constructor.instructions.insertBefore(constructor.instructions.getFirst(), list)
                    }
            }
        }
    }

    private fun fixSize(nodes: InsnList) {
        val notedSize = nodes.size()
        var index = 0
        var actualSize = -1
        var insn = nodes.first
        while (insn != null) {
            index++
            if (insn == nodes.last) {
                actualSize = index
            }
            insn = insn.next
        }
        if (actualSize != -1 && notedSize != actualSize && index == actualSize) {
            insnListSizeField.set(nodes, actualSize)
        }
    }

    fun postApplyEnum(targetClass: ClassNode, mixinClass: ClassNode) {
        if (mixinClass.interfaces.contains(EXTENSIBLE_ENUM)) {
            val clinit = targetClass.methods.first { it.name == "<clinit>" }

            val vanillaCount = INITIAL_ENUM_COUNTS.getOrDefault(targetClass.name, 0)

            // Fix ArrayIndexOutOfBoundsException exceptions because the cached size does not match the size of the linked list.
            fixSize(clinit.instructions)

            // Make sure the ordinal parameter does not change between reboots.
            let {
                val enumPositions = TreeMap<String, Int>()
                var vanillaRemaining = vanillaCount
                for (i in 0 until clinit.instructions.size()) {
                    when (val ins = clinit.instructions.get(i)) {
                        is MethodInsnNode -> {
                            if (ins.name == "<init>" && ins.owner == targetClass.name) {
                                val assign = ins.next
                                if (assign is FieldInsnNode) {
                                    // We don't want to change the ordinal of the vanilla parameters.
                                    if (vanillaRemaining > 0) {
                                        vanillaRemaining--
                                        continue
                                    }
                                    enumPositions[assign.name] = i
                                }
                            }
                        }
                    }
                }

                val enumOrdinals = mutableMapOf<String, Int>()

                // Overwrite the ordinals for each non-vanilla enum (including Fabric ones) based on their order in our map.
                // This effectively sorts them in alphabetical order.
                var newIndex = vanillaCount
                for ((name, pos) in enumPositions) {
                    val ins = clinit.instructions.get(pos)
                    var prev = ins
                    while (prev !is TypeInsnNode || prev.desc != targetClass.name || prev.opcode != Opcodes.NEW) {
                        prev = prev.previous
                    }
                    enumOrdinals[name] = newIndex
                    clinit.instructions.set(prev.next.next.next, pushInt(newIndex))

                    newIndex++
                }

                val valuesName = KiltRemapper.enhancedRemapper.mapFieldName(
                    KiltRemapper.unmapClass(targetClass.name),
                    $$"$VALUES",
                    "[L${KiltRemapper.unmapClass(targetClass.name)};"
                )

                // Sort the $VALUES array based on the ordinal after it has been set for the last time.
                val fixValuesOrder = InsnList()
                fixValuesOrder.add(LabelNode())
                fixValuesOrder.add(FieldInsnNode(
                    Opcodes.GETSTATIC, targetClass.name,
                    valuesName,
                    "[L${targetClass.name};"
                ))

                fixValuesOrder.add(InvokeDynamicInsnNode(
                    "apply", "()Ljava/util/function/Function;",
                    Handle(
                        Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
                        $$"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
                        false
                    ),
                    Type.getType("(Ljava/lang/Object;)Ljava/lang/Object;"),
                    Handle(Opcodes.H_INVOKEVIRTUAL, "java/lang/Enum", "ordinal", "()I", false),
                    Type.getType("(L${targetClass.name};)Ljava/lang/Integer;")
                ))

                fixValuesOrder.add(MethodInsnNode(Opcodes.INVOKESTATIC, "java/util/Comparator", "comparing", "(Ljava/util/function/Function;)Ljava/util/Comparator;", true))
                fixValuesOrder.add(MethodInsnNode(Opcodes.INVOKESTATIC, "java/util/Arrays", "sort", "([Ljava/lang/Object;Ljava/util/Comparator;)V", false))

                var setValuesIndex = 0
                for (ins in clinit.instructions) {
                    if (ins is FieldInsnNode && ins.name == valuesName && ins.owner == targetClass.name && ins.desc == "[L${targetClass.name};" && ins.opcode == Opcodes.PUTSTATIC) {
                        setValuesIndex = clinit.instructions.indexOf(ins)
                    }
                }
                clinit.instructions.insert(clinit.instructions[setValuesIndex], fixValuesOrder)
            }

            // Fill all EnumProxy instances.
            val proxies = proxies.get(targetClass.name)
			proxies?.keys?.forEach(Consumer { fieldName: String? ->
				val fillProxy = InsnList()
				fillProxy.add(LabelNode())
				fillProxy.add(
					FieldInsnNode(
						Opcodes.GETSTATIC,
                        SET_PROXY_VALUE_TARGET_CLASS,
						"INSTANCE",
						"L$SET_PROXY_VALUE_TARGET_CLASS;"
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
                        SET_PROXY_VALUE_TARGET_CLASS,
						"setProxyValue",
						"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)V"
					)
				)
				clinit.instructions.insertBefore(clinit.instructions.getLast(), fillProxy)
			})

            // Overwrite getExtensionInfo
            val currentCount = countEnumFields(targetClass)
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
                val getExtensionInfo = targetClass.methods.first {
                    it.name == "getExtensionInfo" &&
                    it.desc == "()$extensionInfoDesc" &&
                    (it.access and Opcodes.ACC_STATIC) != 0
                }
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

package xyz.bluspring.kilt.gradle

import net.fabricmc.mappingio.MappingReader
import net.fabricmc.mappingio.tree.MemoryMappingTree
import java.io.File

class AccessTransformerRemapper {
    fun remapDescriptor(descriptor: String, mappings: MemoryMappingTree): String {
        var formed = ""

        var incomplete = ""
        var inClass = false
        for (c in descriptor) {
            if (c == 'L' && !inClass)
                inClass = true

            if (inClass) {
                incomplete += c

                if (c == ';') {
                    inClass = false
                    formed += 'L'

                    val name = incomplete.removePrefix("L").removeSuffix(";")
                    formed += mappings.classes.firstOrNull { it.getName(0) == name }?.srcName ?: name

                    formed += ';'

                    incomplete = ""
                }
            } else {
                formed += c
            }
        }

        return formed
    }

    fun convertTransformerToWidener(data: String, output: File, version: String, tempDir: File) {
        val mappingDownloader = MappingDownloader(version, tempDir)
        mappingDownloader.downloadFiles()

        val srg = MemoryMappingTree() // obf -> srg
        MappingReader.read(mappingDownloader.srgMappingsFile.reader(), srg)

        val mojmap = MemoryMappingTree() // obf -> moj
        MappingReader.read(mappingDownloader.mojangMappingsFile.reader(), mojmap)

        println("Mapping SRG directly to MojMap...")
        val srg2mojmap = mutableMapOf<String, String>()
        val fieldDescriptors = mutableMapOf<String, String>()

        for (classMapping in mojmap.classes) {
            //val mojClassMap = mojmap.classes.firstOrNull { it.getName(0) == classMapping.srcName } ?: continue

            for (field in classMapping.fields) {
                val srgName = field.srcName

                srg2mojmap[srgName] = field.srcName
                if (field.srcDesc != null)
                    fieldDescriptors[srgName] = field.srcDesc!!
            }

            /*for (method in classMapping.methods) {
                val srgName = method.getName("srg")!!
                if (!srgName.startsWith("f_") && !srgName.startsWith("m_"))
                    continue

                val remappedDesc = remapDescriptor(method.srcDesc ?: "", mojmap)
                val mojMethod = mojClassMap.methods.firstOrNull { it.getName(0) == method.srcName && remappedDesc == it.srcDesc } ?: continue
                srg2mojmap[srgName] = mojMethod.srcName
            }*/
        }

        println("Finished mapping SRG to MojMap!")
        println("Proceeding with converting access transformer to access widener...")

        val widener = mutableListOf<String>()

        widener += "accessWidener v2 named"

        widener += ""
        widener += "# Kilt's own access wideners"
        widener += "accessible method net/minecraft/client/renderer/texture/SpriteContents\$AnimatedTexture getFrameX (I)I\n" +
                "accessible method net/minecraft/client/renderer/texture/SpriteContents\$AnimatedTexture getFrameY (I)I\n" +
                "transitive-accessible method net/minecraft/world/entity/Entity setRemoved (Lnet/minecraft/world/entity/Entity\$RemovalReason;)V\n" +
                "transitive-extendable method net/minecraft/world/entity/Entity setRemoved (Lnet/minecraft/world/entity/Entity\$RemovalReason;)V\n" +
                "transitive-accessible method net/minecraft/world/item/CreativeModeTab <init> (Lnet/minecraft/world/item/CreativeModeTab\$Row;ILnet/minecraft/world/item/CreativeModeTab\$Type;Lnet/minecraft/network/chat/Component;Ljava/util/function/Supplier;Lnet/minecraft/world/item/CreativeModeTab\$DisplayItemsGenerator;)V\n" +
                "transitive-accessible class net/minecraft/server/advancements/AdvancementVisibilityEvaluator\$VisibilityRule\n" +
                "transitive-accessible class net/minecraft/data/HashCache\$ProviderCache\n" +
                "transitive-accessible method net/minecraft/client/gui/components/AbstractSliderButton setValue (D)V\n" +
                "transitive-extendable method net/minecraft/client/gui/components/AbstractSliderButton setValue (D)V\n" +
                "\n" +
                "accessible class net/minecraft/world/effect/MobEffectInstance\$Details\n" +
                "accessible method net/minecraft/world/effect/MobEffectInstance\$Details <init> (IIZZZLjava/util/Optional;)V\n" +
                "accessible class net/minecraft/world/inventory/BrewingStandMenu\$PotionSlot\n" +
                "\n" +
                "accessible field com/mojang/blaze3d/platform/GlStateManager BLEND Lcom/mojang/blaze3d/platform/GlStateManager\$BlendState;\n" +
                "accessible field com/mojang/blaze3d/platform/GlStateManager DEPTH Lcom/mojang/blaze3d/platform/GlStateManager\$DepthState;\n" +
                "accessible field com/mojang/blaze3d/platform/GlStateManager CULL Lcom/mojang/blaze3d/platform/GlStateManager\$CullState;\n" +
                "accessible field com/mojang/blaze3d/platform/GlStateManager POLY_OFFSET Lcom/mojang/blaze3d/platform/GlStateManager\$PolygonOffsetState;\n" +
                "accessible field com/mojang/blaze3d/platform/GlStateManager COLOR_LOGIC Lcom/mojang/blaze3d/platform/GlStateManager\$ColorLogicState;\n" +
                "accessible field com/mojang/blaze3d/platform/GlStateManager STENCIL Lcom/mojang/blaze3d/platform/GlStateManager\$StencilState;\n" +
                "accessible field com/mojang/blaze3d/platform/GlStateManager SCISSOR Lcom/mojang/blaze3d/platform/GlStateManager\$ScissorState;\n" +
                "accessible field com/mojang/blaze3d/platform/GlStateManager COLOR_MASK Lcom/mojang/blaze3d/platform/GlStateManager\$ColorMask;\n" +
                "\n" +
                "accessible field com/mojang/blaze3d/vertex/VertexFormatElement BY_ID [Lcom/mojang/blaze3d/vertex/VertexFormatElement;"

        widener += ""
        widener += "# Auto generated access widener from NeoForge's access transformers."

        for (line in data.lines()) {
            val trimmed = line.replaceAfter("#", "").replace("#", "").trim()

            if (trimmed.isBlank())
                continue

            val split = trimmed.split(" ")

            val className = split[1].replace(".", "/")

            if (split.size == 2) { // Class
                widener += "transitive-accessible class $className"
                widener += "transitive-extendable class $className"
            } else {
                if (split[2].contains("(")) { // Method
                    val srgMethodName = split[2].replaceAfter("(", "").replace("(", "")
                    val methodName = srg2mojmap[srgMethodName] ?: srgMethodName

                    // this isn't a joke, why does Forge access transform lambdas????
                    if (methodName.startsWith("lambda$"))
                        continue

                    val descriptor = split[2].replaceBefore("(", "")
                    widener += "transitive-accessible method $className $methodName $descriptor"
                    widener += "transitive-extendable method $className $methodName $descriptor"
                } else { // Field
                    val srgFieldName = split[2]
                    val fieldName = srg2mojmap[srgFieldName] ?: srgFieldName
                    val descriptor = fieldDescriptors[srgFieldName] ?: "# TODO: ADD DESC"

                    widener += "transitive-accessible field $className $fieldName $descriptor"
                    widener += "transitive-mutable field $className $fieldName $descriptor"
                }
            }
        }

        // Custom widener values for Kilt
        widener += "transitive-accessible class net/minecraft/world/item/CreativeModeTab\$ItemDisplayBuilder"
        widener += "transitive-accessible class net/minecraft/client/gui/screens/advancements/AdvancementTabType"
        widener += "transitive-accessible field net/minecraft/client/renderer/ItemBlockRenderTypes TYPE_BY_BLOCK Ljava/util/Map;"
        widener += "transitive-accessible field net/minecraft/client/renderer/ItemBlockRenderTypes TYPE_BY_FLUID Ljava/util/Map;"
        widener += "transitive-accessible field net/minecraft/commands/synchronization/ArgumentTypeInfos BY_CLASS Ljava/util/Map;"
        widener += "transitive-accessible field net/minecraft/world/entity/SpawnPlacements DATA_BY_TYPE Ljava/util/Map;"
        widener += "transitive-accessible class net/minecraft/world/entity/SpawnPlacements\$Data"
        widener += "transitive-accessible class net/minecraft/core/registries/BuiltInRegistries\$RegistryBootstrap"
        widener += "transitive-accessible class net/minecraft/core/RegistrySetBuilder\$BuildState"
        widener += "transitive-accessible class net/minecraft/core/RegistrySetBuilder\$BuildState\$1"

        if (!output.exists())
            output.createNewFile()

        output.writeText(widener.joinToString("\n"))
    }
}
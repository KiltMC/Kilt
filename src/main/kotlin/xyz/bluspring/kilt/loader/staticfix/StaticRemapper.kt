package xyz.bluspring.kilt.loader.staticfix

import xyz.bluspring.kilt.loader.remap.KiltRemapper

class StaticRemapper(private val membersToRemap: List<ClassData>, private val classesToRemap: List<Pair<String, String>>) {
    fun tryRemapOwner(owner: String, name: String, descriptor: String): String {
        val mojmappedName = KiltRemapper.srgIntermediaryTree.classes.firstOrNull { it.getName("intermediary") == owner }?.getName("srg")

        val classData = membersToRemap.firstOrNull { it.from == (mojmappedName ?: owner) } ?: return owner
        classData.members.firstOrNull { it.name == name && it.descriptor == descriptor } ?: return owner

        return classData.to
    }

    fun getSuper(owner: String): String? {
        return membersToRemap.firstOrNull { it.from == owner }?.members?.firstOrNull { it.type == MemberData.MemberType.SUPER }?.descriptor
    }

    companion object {
        fun read(data: String): StaticRemapper {
            val split = if (data.contains("\r\n")) "\r\n" else "\n"

            val membersToRemap = mutableListOf<ClassData>()
            val classesToRemap = mutableListOf<Pair<String, String>>()

            var currentClass: ClassData? = null

            data.split(split).forEach { line ->
                if (line.startsWith("#"))
                    return@forEach

                if (line.trim().isBlank())
                    return@forEach

                val lineSplit = line.trim().split(" ")
                if (lineSplit[0] == "c") {
                    membersToRemap.add(ClassData(
                        lineSplit[1], lineSplit[2], mutableListOf()
                    ).apply {
                        currentClass = this
                    })
                } else if (lineSplit[0] == "f") {
                    currentClass?.members?.add(MemberData(
                        lineSplit[1], lineSplit[2],
                        MemberData.MemberType.FIELD
                    ))
                } else if (lineSplit[0] == "m") {
                    currentClass?.members?.add(MemberData(
                        lineSplit[1], lineSplit[2],
                        MemberData.MemberType.METHOD
                    ))
                } else if (lineSplit[0] == "mc") {
                    classesToRemap.add(lineSplit[1] to lineSplit[2])
                } else if (lineSplit[0] == "i") {
                    currentClass?.members?.add(MemberData(
                        lineSplit[1], lineSplit[2],
                        MemberData.MemberType.INITIALIZER
                    ))
                } else if (lineSplit[0] == "s") {
                    currentClass?.members?.add(MemberData(
                        lineSplit[1], lineSplit[2],
                        MemberData.MemberType.SUPER
                    ))
                }
            }

            return StaticRemapper(membersToRemap, classesToRemap)
        }
    }

    data class ClassData(
        val from: String,
        val to: String,
        val members: MutableList<MemberData>
    )

    data class MemberData(
        val name: String,
        val descriptor: String,
        val type: MemberType
    ) {
        enum class MemberType {
            FIELD, METHOD, INITIALIZER, SUPER
        }
    }
}
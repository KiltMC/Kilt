package xyz.bluspring.kilt.loader.remap

object MixinHelpers {
    fun mapToAnnotationValues(map: Map<String, Any>): List<Any> {
        val values = mutableListOf<Any>()

        for ((key, v) in map) {
            values.add(key)
            values.add(v)
        }

        return values
    }

    fun annotationValuesToMap(values: List<Any>): Map<String, Any> {
        val map = mutableMapOf<String, Any>()

        var currentKey = ""
        for ((index, value) in values.withIndex()) {
            if ((index and 1) == 0) {
                currentKey = value as String
            } else {
                map[currentKey] = value
            }
        }

        return map
    }

//        fun splitDescriptor(descriptor: String): List<String> {
//            val split = mutableListOf<String>()
//
//            var incompleteString = ""
//            var isInArray = false
//            var isInClass = false
//
//            for (ch in descriptor) {
//                incompleteString += ch
//
//                if (ch == '[') {
//                    isInArray = true
//                } else if (ch == 'L') {
//                    isInClass = true
//                } else if (ch == ';' && isInClass) {
//                    isInClass = false
//                    isInArray = false
//                    split.add(incompleteString)
//                    incompleteString = ""
//                } else if (!isInClass) {
//                    if (isInArray)
//                        isInArray = false
//
//                    split.add(incompleteString)
//                    incompleteString = ""
//                }
//            }
//
//            return split
//        }
//
    fun splitSignature(descriptor: String): List<String> {
        val split = mutableListOf<String>()
        val current = mutableListOf<String>()

        var incompleteString = ""
        var isInArray = false
        var isInClass = false
        var genericLayer = 0

        for (ch in descriptor) {
            incompleteString += ch

            if (ch == '<') {
                genericLayer++
            } else if (ch == '>') {
                if (--genericLayer <= 0) {
                    current.add(incompleteString)
                    incompleteString = ""
                    genericLayer = 0
                }
            } else if (ch == '[') {
                isInArray = true
            } else if (ch == 'L') {
                isInClass = true
            } else if (ch == ';' && isInClass && genericLayer <= 0) {
                isInClass = false
                isInArray = false
                current.add(incompleteString)
                split.add(current.joinToString(""))
                current.clear()
                incompleteString = ""
            } else if (!isInClass && genericLayer <= 0) {
                if (isInArray)
                    isInArray = false

                split.add(incompleteString)
                incompleteString = ""
            }
        }

        if (current.isNotEmpty())
            split.add(current.joinToString(""))
        return split
    }
}

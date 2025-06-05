package xyz.bluspring.knit.loader.fabric.mod.metadata

import net.fabricmc.loader.api.metadata.CustomValue

class CustomNullValue : CustomValue {
    override fun getType(): CustomValue.CvType? {
        return CustomValue.CvType.NULL
    }

    override fun getAsObject(): CustomValue.CvObject? {
        TODO("Not yet implemented")
    }

    override fun getAsArray(): CustomValue.CvArray? {
        TODO("Not yet implemented")
    }

    override fun getAsString(): String? {
        TODO("Not yet implemented")
    }

    override fun getAsNumber(): Number? {
        TODO("Not yet implemented")
    }

    override fun getAsBoolean(): Boolean {
        TODO("Not yet implemented")
    }
}
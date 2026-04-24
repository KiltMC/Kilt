package xyz.bluspring.kilt.workarounds

import com.google.gson.JsonParseException

class SkippedConditionException(msg: String) : JsonParseException(msg) {
}
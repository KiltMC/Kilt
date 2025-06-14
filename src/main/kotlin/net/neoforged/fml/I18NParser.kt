package net.neoforged.fml

interface I18NParser {
    fun parseMessage(i18nMessage: String, vararg args: Any): String
    fun stripControlCodes(toStrip: String): String
}
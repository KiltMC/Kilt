package xyz.bluspring.kilt.workarounds

import com.mojang.datafixers.util.Either
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.locale.Language
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.Style
import net.minecraft.world.inventory.tooltip.TooltipComponent
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.mixin.ClientTextTooltipAccessor
import java.lang.reflect.Field
import java.lang.reflect.Modifier

@JvmRecord
data class WrappedClientTooltipComponent(
    val wrapped: ClientTooltipComponent
) : TooltipComponent {
    companion object {
        private val tooltipComponentFinder = object : ClassValue<Field>() {
            override fun computeValue(type: Class<*>): Field? {
                var candidate: Field? = null
                val duplicates = mutableListOf<String>()

                var currentType: Class<*>? = type
                while (currentType != null && currentType != Any::class.java) {
                    for (field in type.declaredFields) {
                        if (!Modifier.isStatic(field.modifiers) && TooltipComponent::class.java.isAssignableFrom(field.type)) {
                            if (candidate != null) {
                                duplicates.add(field.name)
                            } else candidate = field
                        }
                    }

                    currentType = type.superclass
                }

                if (duplicates.isNotEmpty()) {
                    Kilt.logger.warn("Multiple TooltipComponents found in ${type.name}, using first: ${candidate?.name} (others: ${duplicates.joinToString(", ")})")
                }

                candidate?.isAccessible = true
                return candidate
            }
        }

        @JvmStatic
        fun extractOrWrap(component: ClientTooltipComponent, originalList: List<FormattedText>): Either<FormattedText, TooltipComponent> {
            if (component is ClientTextTooltip) {
                val text = (component as ClientTextTooltipAccessor).text

                // there is no caching this. good luck.
                return Either.left(run {
                    for (component in originalList) {
                        // little hack to bypass stuff
                        if (Language.getInstance().getVisualOrder(component) == text)
                            return@run component
                    }

                    // otherwise, reconstruct the string, somehow.
                    var current = FormattedText.EMPTY
                    val currentText = StringBuilder()
                    var currentStyle = Style.EMPTY

                    text.accept { _, style, codepoint ->
                        if (currentStyle != style) {
                            current = FormattedText.composite(current, FormattedText.of(currentText.toString(), currentStyle))
                            currentStyle = style
                            currentText.clear()
                        }

                        currentText.append(Character.toString(codepoint))
                        true
                    }

                    if (currentText.isNotEmpty()) {
                        current = FormattedText.composite(current, FormattedText.of(currentText.toString(), currentStyle))
                    }

                    current
                })
            }

            val field = this.tooltipComponentFinder.get(component::class.java)

            if (field != null) {
                val value = field.get(component) as TooltipComponent
                return Either.right(value)
            }

            return Either.right(WrappedClientTooltipComponent(component))
        }
    }
}

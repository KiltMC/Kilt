package xyz.bluspring.kilt.injections.client.gui.components;

import net.minecraft.client.gui.components.Button;

import java.util.function.Function;

public interface ButtonBuilderInjection {
    default Button build(Function<Button.Builder, Button> builder) {
        return builder.apply((Button.Builder) this);
    }
}

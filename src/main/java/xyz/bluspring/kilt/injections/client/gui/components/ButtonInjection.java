package xyz.bluspring.kilt.injections.client.gui.components;

import net.minecraft.client.gui.components.Button;

public interface ButtonInjection {
    static Button create(Button.Builder builder) {
        return builder.build();
    }
}

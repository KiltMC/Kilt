package xyz.bluspring.kilt.injections.client;

import net.minecraft.client.MouseHandler;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;

@FabricInjectedInterface(MouseHandler.class)
public interface MouseHandlerInjection {
    default double getXVelocity() {
        throw new IllegalStateException();
    }

    default double getYVelocity() {
        throw new IllegalStateException();
    }
}

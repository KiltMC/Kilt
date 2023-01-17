package xyz.bluspring.kilt.mixin;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.SharedSuggestionProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientPacketListener.class)
public interface ClientPacketListenerAccessor {
    @Accessor
    CommandDispatcher<SharedSuggestionProvider> getCommands();

    @Accessor
    void setCommands(CommandDispatcher<SharedSuggestionProvider> commands);
}

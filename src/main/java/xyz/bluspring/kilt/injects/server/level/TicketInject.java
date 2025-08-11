package xyz.bluspring.kilt.injects.server.level;

import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.server.level.TicketInjection;

@Mixin(Ticket.class)
public abstract class TicketInject<T> implements TicketInjection {
    public TicketInject(TicketType<T> type, int ticketLevel, T key) {}

    @CreateInitializer
    public TicketInject(TicketType<T> type, int ticketLevel, T key, boolean forceTicks) {
        this(type, ticketLevel, key);
        this.kilt$setForceTicks(forceTicks);
    }

    // Handled by Porting Lib
}

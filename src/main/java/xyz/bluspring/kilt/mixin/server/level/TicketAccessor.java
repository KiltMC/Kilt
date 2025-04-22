package xyz.bluspring.kilt.mixin.server.level;

import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Ticket.class)
public interface TicketAccessor {
    @Invoker("<init>")
    static <T> Ticket<T> createTicket(TicketType<T> type, int ticketLevel, T key) {
        throw new IllegalStateException();
    }
}

package xyz.bluspring.kilt.injections.server.level;

import io.github.fabricators_of_create.porting_lib.chunk.loading.extensions.TicketExtension;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import xyz.bluspring.kilt.mixin.server.level.TicketAccessor;

public interface TicketInjection extends TicketExtension {
    static <T> Ticket<T> createTicket(TicketType<T> type, int ticketLevel, T key, boolean forceTicks) {
        var ticket = TicketAccessor.createTicket(type, ticketLevel, key);
        ((TicketInjection) (Object) ticket).kilt$setForceTicks(forceTicks);

        return ticket;
    }

    default boolean isForceTicks() {
        throw new IllegalStateException();
    }

    default void kilt$setForceTicks(boolean forceTicks) {
        this.setForceTicks(forceTicks);
    }
}

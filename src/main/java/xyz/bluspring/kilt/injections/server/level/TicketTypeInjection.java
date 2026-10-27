package xyz.bluspring.kilt.injections.server.level;

import xyz.bluspring.kilt.helpers.RecordMixinRefMaps;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.server.level.TicketType;

public interface TicketTypeInjection {
    default boolean forceNaturalSpawning() {
        throw KiltHelper.createMixinException(TicketTypeInjection.class, "forceNaturalSpawning");
    }

    static TicketType create(long timeout, @TicketType.Flags int flags, boolean forceNaturalSpawning) {
        var type = new TicketType(timeout, flags);
        RecordMixinRefMaps.EXTENDED_TICKET_TYPE.put(type, new RecordMixinRefMaps.ExtendedTicketType(forceNaturalSpawning));
        return type;
    }
}

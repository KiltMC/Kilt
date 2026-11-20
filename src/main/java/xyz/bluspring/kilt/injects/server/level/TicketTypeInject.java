package xyz.bluspring.kilt.injects.server.level;

import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.RecordMixinRefMaps;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.server.level.TicketTypeInjection;

import net.minecraft.server.level.TicketType;

@Mixin(TicketType.class)
public abstract class TicketTypeInject implements TicketTypeInjection {
    public TicketTypeInject(long timeout, @TicketType.Flags int flags) {}

    @CreateInitializer
    public TicketTypeInject(long timeout, @TicketType.Flags int flags, boolean forceNaturalSpawning) {
        this(timeout, flags);
        RecordMixinRefMaps.EXTENDED_TICKET_TYPE.put((TicketType) (Object) this, new RecordMixinRefMaps.ExtendedTicketType(forceNaturalSpawning));
    }

    @Override
    public boolean forceNaturalSpawning() {
        if (RecordMixinRefMaps.EXTENDED_TICKET_TYPE.containsKey(this)) {
            return RecordMixinRefMaps.EXTENDED_TICKET_TYPE.get(this).forceNaturalSpawning();
        }

        return false;
    }
}

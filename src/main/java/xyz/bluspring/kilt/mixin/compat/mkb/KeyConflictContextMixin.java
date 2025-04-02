package xyz.bluspring.kilt.mixin.compat.mkb;

import committee.nova.mkb.keybinding.KeyConflictContext;
import net.minecraftforge.client.settings.IKeyConflictContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xyz.bluspring.kilt.compat.mkb.MKBKeyConflictContextWrapper;

import java.util.HashMap;
import java.util.Map;

@Mixin(KeyConflictContext.class)
public abstract class KeyConflictContextMixin implements IKeyConflictContext {
    @Unique
    private static final Map<IKeyConflictContext, MKBKeyConflictContextWrapper> kilt$contextWrappers = new HashMap<>();

    @Override
    public boolean conflicts(IKeyConflictContext other) {
        return ((committee.nova.mkb.api.IKeyConflictContext) this).conflicts(kilt$contextWrappers.computeIfAbsent(other, MKBKeyConflictContextWrapper::new));
    }
}

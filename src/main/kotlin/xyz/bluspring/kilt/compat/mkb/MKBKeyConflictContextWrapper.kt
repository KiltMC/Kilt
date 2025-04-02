package xyz.bluspring.kilt.compat.mkb

import net.minecraftforge.client.settings.IKeyConflictContext
import committee.nova.mkb.api.IKeyConflictContext as MKBKeyConflictContext

class MKBKeyConflictContextWrapper(val wrapped: IKeyConflictContext) : MKBKeyConflictContext {
    override fun isActive(): Boolean {
        return wrapped.isActive
    }

    override fun conflicts(ctx: MKBKeyConflictContext): Boolean {
        return wrapped.conflicts(ctx as IKeyConflictContext)
    }

    override fun equals(other: Any?): Boolean {
        if (other is MKBKeyConflictContextWrapper) {
            return this.wrapped == other.wrapped
        }

        return super.equals(other)
    }
}
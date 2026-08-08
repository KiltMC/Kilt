package xyz.bluspring.mods.flashbacksablecompat.compat

import dev.ryanhcode.sable.network.packets.ClientboundSableSnapshotDualPacket
import xyz.bluspring.mods.flashbacksablecompat.FlashbackSableCompat

object ActionSableSnapshot : ActionSableUdp<ClientboundSableSnapshotDualPacket>(FlashbackSableCompat.id("action/sable_snapshot_optional"), ClientboundSableSnapshotDualPacket.CODEC)

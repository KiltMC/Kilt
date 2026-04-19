package xyz.bluspring.kilt.compat.fabric.sable

import dev.ryanhcode.sable.fabric.event.FabricSablePostPhysicsTickEvent
import dev.ryanhcode.sable.fabric.event.FabricSablePrePhysicsTickEvent
import dev.ryanhcode.sable.fabric.event.FabricSableSubLevelContainerReadyEvent
import dev.ryanhcode.sable.neoforge.event.ForgeSablePostPhysicsTickEvent
import dev.ryanhcode.sable.neoforge.event.ForgeSablePrePhysicsTickEvent
import dev.ryanhcode.sable.neoforge.event.ForgeSableSubLevelContainerReadyEvent
import net.neoforged.neoforge.common.NeoForge

object SableCompatBridge {
    fun init() {
        FabricSablePrePhysicsTickEvent.EVENT.register { system, timeStep -> NeoForge.EVENT_BUS.post(ForgeSablePrePhysicsTickEvent(system, timeStep)) }
        FabricSablePostPhysicsTickEvent.EVENT.register { system, timeStep -> NeoForge.EVENT_BUS.post(ForgeSablePostPhysicsTickEvent(system, timeStep)) }
        FabricSableSubLevelContainerReadyEvent.EVENT.register { level, container -> NeoForge.EVENT_BUS.post(ForgeSableSubLevelContainerReadyEvent(level, container)) }
    }
}
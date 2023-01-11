package net.minecraftforge.event

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraftforge.eventbus.api.Event

class RegisterCommandsEvent(val dispatcher: CommandDispatcher<CommandSourceStack>, environment: Commands.CommandSelection, context: CommandBuildContext) : Event() {
    val buildContext = context
    val commandSelection = environment
}
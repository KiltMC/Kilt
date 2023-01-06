package net.minecraftforge.common.util

import com.mojang.authlib.GameProfile
import net.minecraft.server.level.ServerLevel

// porting lib my beloved
class FakePlayer(
    level: ServerLevel,
    name: GameProfile
) : io.github.fabricators_of_create.porting_lib.fake_players.FakePlayer(level, name)
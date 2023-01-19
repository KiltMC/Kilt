/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.versions.forge;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.VersionChecker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import xyz.bluspring.kilt.loader.KiltLoader;

public class ForgeVersion
{
    private static final Logger LOGGER = LogManager.getLogger();
    // This is Forge's Mod Id, used for the ForgeMod and resource locations
    public static final String MOD_ID = "forge";

    private static final String forgeVersion = KiltLoader.Companion.getSUPPORTED_FORGE_API_VERSION().toString();
    private static final String forgeSpec = KiltLoader.Companion.getSUPPORTED_FORGE_SPEC_VERSION().toString();
    private static final String forgeGroup = "xyz.bluspring.kilt";

    public static String getVersion()
    {
        return forgeVersion;
    }

    public static VersionChecker.Status getStatus()
    {
        return VersionChecker.getResult(ModList.get().getModFileById(MOD_ID).getMods().get(0)).status();
    }

    @Nullable
    public static String getTarget()
    {
        VersionChecker.CheckResult res = VersionChecker.getResult(ModList.get().getModFileById(MOD_ID).getMods().get(0));
        return res.target() == null ? "" : res.target().toString();
    }

    public static String getSpec() {
        return forgeSpec;
    }

    public static String getGroup() {
        return forgeGroup;
    }
}


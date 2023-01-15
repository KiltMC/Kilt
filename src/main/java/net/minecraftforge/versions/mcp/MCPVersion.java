/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.versions.mcp;

import net.fabricmc.loader.impl.FabricLoaderImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.minecraftforge.fml.Logging.CORE;

public class MCPVersion {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String mcVersion;
    private static final String mcpVersion;
    static {
        LOGGER.debug(CORE, "MCP Version package {} from {}", MCPVersion.class.getPackage(), MCPVersion.class.getClassLoader());
        mcVersion = FabricLoaderImpl.INSTANCE.getGameProvider().getNormalizedGameVersion();
        if (mcVersion == null) throw new RuntimeException("Missing MC version, cannot continue");

        mcpVersion = FabricLoaderImpl.INSTANCE.getGameProvider().getNormalizedGameVersion();
        if (mcpVersion == null) throw new RuntimeException("Missing MCP version, cannot continue");

        LOGGER.debug(CORE, "Found MC version information {}", mcVersion);
        LOGGER.debug(CORE, "Found MCP version information {}", mcpVersion);
    }
    public static String getMCVersion() {
        return mcVersion;
    }

    public static String getMCPVersion() {
        return mcpVersion;
    }

    public static String getMCPandMCVersion()
    {
        return mcVersion+"-"+mcpVersion;
    }
}

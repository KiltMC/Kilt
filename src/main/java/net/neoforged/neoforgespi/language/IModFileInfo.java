/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforgespi.language;

import net.neoforged.neoforgespi.locating.IModFile;
import org.apache.maven.artifact.versioning.VersionRange;

import java.util.List;
import java.util.Map;

public interface IModFileInfo {
    List<IModInfo> getMods();

    record LanguageSpec(String languageName, VersionRange acceptedVersions) {}

    List<LanguageSpec> requiredLanguageLoaders();

    boolean showAsResourcePack();

    boolean showAsDataPack();

    Map<String, Object> getFileProperties();

    String getLicense();

    String moduleName();

    String versionString();

    List<String> usesServices();

    IModFile getFile();

    IConfigurable getConfig();
}

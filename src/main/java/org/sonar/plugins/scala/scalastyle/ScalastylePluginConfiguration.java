/*
 * Sonar Scala Plugin
 * Copyright (C) 2011 - 2014 All contributors
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.scala.scalastyle;

import org.apache.commons.lang.CharEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchExtension;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.InputFileUtils;
import org.sonar.api.resources.ProjectFileSystem;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * This is the main configuration for Scalastyle in Sonar Plugin.
 *
 * @author Tomasz Mosiej
 * @since 1.0.1
 */
public class ScalastylePluginConfiguration implements BatchExtension {

    private static final Logger LOG = LoggerFactory.getLogger(ScalastylePluginConfiguration.class);
    public static final String PROPERTY_USE_LOCAL_RULE = "sonar.scalastyle.localRuleSubset";

    private final RulesProfile profile;
    private final Settings conf;
    private final ProjectFileSystem fileSystem;

    public ScalastylePluginConfiguration(Settings conf, RulesProfile profile, ProjectFileSystem fileSystem) {
        this.conf = conf;
        this.profile = profile;
        this.fileSystem = fileSystem;
    }

    public List<File> getSourceFiles() {
        return InputFileUtils.toFiles(fileSystem.mainFiles("scala"));
    }

    public List<File> getSourceDirs() {
        return fileSystem.getSourceDirs();
    }

    public List<String> getSourceDir() {
        return Arrays.asList(fileSystem.getBasedir().getPath()+"/src/main/scala/",fileSystem.getBasedir().getPath()+"/app/");
    }

    public Charset getCharset() {
        Charset charset = fileSystem.getSourceCharset();
        if (charset == null) {
            charset = Charset.forName(System.getProperty("file.encoding", CharEncoding.UTF_8));
        }
        return charset;
    }

    public String getLocalRuleFile(){
        return conf.getString(PROPERTY_USE_LOCAL_RULE);
    }
}

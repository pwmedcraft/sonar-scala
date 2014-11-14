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

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.scala.language.Scala;

import java.io.InputStream;
import java.io.InputStreamReader;

public class ScalastyleAbstractProfile extends ProfileDefinition {

    private static final Logger LOG = LoggerFactory.getLogger(ScalastyleAbstractProfile.class);

    private final String name;
    private final String fileName;
    private final Boolean isDefault;

    private static final String DEFAULT_SCALASTYLE_DIR = "/org/sonar/plugins/scala/";

    private final ScalastyleProfileImporter scalastyleProfileImporter;

    public ScalastyleAbstractProfile(String name,String fileName,boolean isDefault, ScalastyleProfileImporter scalastyleProfileImporter) {
        this.name = name;
        this.fileName = fileName;
        this.isDefault = isDefault;
        this.scalastyleProfileImporter = scalastyleProfileImporter;
    }

    @Override
    public RulesProfile createProfile(ValidationMessages messages) {
        LOG.debug("Create profile:"+name + " from file:"+fileName);

        InputStream input = getClass().getResourceAsStream(DEFAULT_SCALASTYLE_DIR+fileName);
        try {
            RulesProfile scalaProfile= scalastyleProfileImporter.importProfile(new InputStreamReader(input), messages);
            scalaProfile.setLanguage(Scala.INSTANCE.getKey());
            scalaProfile.setName(name);
            scalaProfile.setDefaultProfile(isDefault);
            return scalaProfile;
        } finally {
            IOUtils.closeQuietly(input);
        }
    }
}
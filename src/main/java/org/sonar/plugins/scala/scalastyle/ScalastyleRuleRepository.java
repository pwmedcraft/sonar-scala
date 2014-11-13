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

import com.google.common.collect.Lists;
import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.resources.Java;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleRepository;
import org.sonar.api.rules.XMLRuleParser;

import java.io.File;
import java.util.List;

/**
 * This is the main rule repository for Scalastyle.
 *
 * @author Tomasz Mosiej
 * @since 1.0.1
 */
public class ScalastyleRuleRepository extends RuleRepository {
    private final ServerFileSystem fileSystem;
    private final XMLRuleParser xmlRuleParser;

    public ScalastyleRuleRepository(ServerFileSystem fileSystem, XMLRuleParser xmlRuleParser) {
        super(ScalastyleConstants.REPOSITORY_KEY, "scala");
        setName(ScalastyleConstants.REPOSITORY_NAME);
        this.fileSystem = fileSystem;
        this.xmlRuleParser = xmlRuleParser;
    }

    @Override
    public List<Rule> createRules() {
        List<Rule> rules = Lists.newArrayList();
        rules.addAll(xmlRuleParser.parse(getClass().getResourceAsStream("/org/sonar/plugins/scala/rules.xml")));
        for (File userExtensionXml : fileSystem.getExtensions(ScalastyleConstants.REPOSITORY_KEY, "xml")) {
            rules.addAll(xmlRuleParser.parse(userExtensionXml));
        }
        return rules;
    }
}

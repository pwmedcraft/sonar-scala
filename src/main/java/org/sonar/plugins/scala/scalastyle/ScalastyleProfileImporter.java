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

import com.google.common.io.CharStreams;
import org.scalastyle.ConfigurationChecker;
import org.scalastyle.ScalastyleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.profiles.ProfileImporter;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.utils.ValidationMessages;
import scala.collection.JavaConversions;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

/**
 * This is the main profile importer for Scalastyle.
 *
 * @author Tomasz Mosiej
 * @since 1.0.1
 */
public class ScalastyleProfileImporter extends ProfileImporter {

    private final RuleFinder ruleFinder;
    private static final Logger LOG = LoggerFactory.getLogger(ScalastyleProfileImporter.class);

    public ScalastyleProfileImporter(RuleFinder ruleFinder) {
        super(ScalastyleConstants.REPOSITORY_KEY, ScalastyleConstants.PLUGIN_NAME);
        setSupportedLanguages("scala");
        this.ruleFinder = ruleFinder;
    }

    @Override
    public RulesProfile importProfile(Reader configurationFile, ValidationMessages messages) {
        LOG.info("Load Scalastyle file :" + configurationFile);
        ScalastyleConfiguration configuration = null;
        try {
            configuration = ScalastyleConfiguration.readFromString(CharStreams.toString(configurationFile));
        } catch (IOException e) {
            LOG.warn("Read config error:",e);
        }
        return createRuleProfile(configuration, messages);
    }

    protected RulesProfile createRuleProfile(ScalastyleConfiguration ruleset, ValidationMessages messages) {
        RulesProfile profile = RulesProfile.create();

        for (ConfigurationChecker checker : JavaConversions.asJavaCollection(ruleset.checks()) ) {

            if (checker.className() == null) {
                messages.addWarningText("A Scalastyle rule without 'className' attribute can't be imported. see '" + checker.className() + "'");
                continue;
            }

            Rule rule = ruleFinder.find(RuleQuery.create().withRepositoryKey(ScalastyleConstants.REPOSITORY_KEY).withConfigKey(checker.className()));
            if (rule != null) {
                ActiveRule activeRule = profile.activateRule(rule, ScalastyleLevelUtils.fromLevel(checker.level().name()));
                if (checker.parameters() != null) {
                    for (Map.Entry<String,String> prop : JavaConversions.mapAsJavaMap(checker.parameters()).entrySet() ) {
                        if (rule.getParam(prop.getKey()) == null) {
                            messages.addWarningText("The property '" + prop.getKey() + "' is not supported in the scalastyle rule: " + checker.className());
                            continue;
                        }
                        activeRule.setParameter(prop.getKey(), prop.getValue());
                    }
                }
            } else {
                messages.addWarningText("Unable to import unknown Scalastyle rule '" + checker.className() + "'");
            }
        }
        return profile;
    }
}

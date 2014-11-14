
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


import org.scalastyle.ConfigurationChecker;
import org.scalastyle.ScalastyleConfiguration;
import org.sonar.api.profiles.ProfileExporter;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.ActiveRuleParam;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.scala.language.Scala;
import scala.collection.JavaConversions;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * This is the Scalastyle profile exporter
 *
 * @author Tomasz Mosiej
 * @since 1.0.1
 */
public class ScalastyleProfileExporter extends ProfileExporter {

    public ScalastyleProfileExporter() {
        super(ScalastyleConstants.REPOSITORY_KEY, ScalastyleConstants.PLUGIN_NAME);
        setSupportedLanguages(Scala.INSTANCE.getKey());
        setMimeType("application/xml");
    }

    @Override
    public void exportProfile(RulesProfile profile, Writer writer) {
        try {
            String xmlModules = exportProfile(ScalastyleConstants.REPOSITORY_KEY, profile);
            writer.append(xmlModules);
        } catch (IOException e) {
            throw new SonarException("Fail to export the profile " + profile, e);
        }
    }

    public String exportProfile(String repositoryKey, RulesProfile profile) {
        scala.collection.immutable.List<ConfigurationChecker> checkerList = createRuleset(repositoryKey, profile.getActiveRulesByRepository(repositoryKey));
        ScalastyleConfiguration configuration = new ScalastyleConfiguration(profile.getName(),true,checkerList);
        return exportRulesetToXml(configuration);
    }

    private scala.collection.immutable.List<ConfigurationChecker> createRuleset(String repositoryKey, List<ActiveRule> activeRules) {
        List<ConfigurationChecker> ruleset = new ArrayList<ConfigurationChecker>();
        for (ActiveRule activeRule : activeRules) {
            if (activeRule.getRule().getRepositoryKey().equals(repositoryKey)) {
                String configKey = activeRule.getRule().getConfigKey();

                final scala.Option<String> none = scala.Option.apply(null);

                Map<String,String> params = new HashMap<String,String>();

                if ((activeRule.getActiveRuleParams() != null) && !activeRule.getActiveRuleParams().isEmpty()) {
                    for (ActiveRuleParam activeRuleParam : activeRule.getActiveRuleParams()) {
                        params.put(activeRuleParam.getRuleParam().getKey(), activeRuleParam.getValue());
                    }
                }

                ConfigurationChecker rule = new ConfigurationChecker(configKey,ScalastyleLevelUtils.toLevel(activeRule.getSeverity()),true,ConverterUtil.makeMapImmutable(JavaConversions.mapAsScalaMap(params)),none,none);
                ruleset.add(rule);
            }
        }
        return JavaConversions.asScalaBuffer(ruleset).toList();
    }

    private String exportRulesetToXml(ScalastyleConfiguration configuration) {
        return ScalastyleConfiguration.toXmlString(configuration,120,1);
    }

}

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

import org.scalastyle.FileSpec;
import org.scalastyle.MainConfig;
import org.scalastyle.Message;
import org.scalastyle.MessageHelper;
import org.scalastyle.ScalastyleChecker;
import org.scalastyle.ScalastyleConfiguration;
import org.scalastyle.StyleError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchExtension;
import org.sonar.api.batch.ProjectClasspath;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.InputFileUtils;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.Violation;
import org.sonar.api.utils.SonarException;
import org.sonar.api.utils.TimeProfiler;
import org.sonar.plugins.scala.language.ScalaFile;
import scala.collection.JavaConversions;
import scala.collection.immutable.List;

import java.io.File;
import java.util.Locale;

/**
 * This is the main executor for Scalastyle in Sonar Plugin
 *
 * @author Tomasz Mosiej
 * @since 1.0.1
 */
public class ScalastyleExecutor implements BatchExtension {
    private static final Logger LOG = LoggerFactory.getLogger(ScalastyleExecutor.class);

    private final ScalastylePluginConfiguration configuration;
    private final ClassLoader projectClassloader;
    private final Project project;
    private final RulesProfile rulesProfile;
    private final RuleFinder ruleFinder;
    private final ScalastyleProfileExporter scalastyleProfileExporter;

    public ScalastyleExecutor(ScalastylePluginConfiguration configuration, ProjectClasspath classpath, Project project, RulesProfile rulesProfile, RuleFinder ruleFinder, ScalastyleProfileExporter scalastyleProfileExporter) {
        this.configuration = configuration;
        this.project = project;
        this.rulesProfile = rulesProfile;
        this.ruleFinder = ruleFinder;
        this.scalastyleProfileExporter = scalastyleProfileExporter;
        this.projectClassloader = classpath.getClassloader();
    }

    ScalastyleExecutor(ScalastylePluginConfiguration configuration, ClassLoader projectClassloader, Project project, RulesProfile rulesProfile, RuleFinder ruleFinder, ScalastyleProfileExporter scalastyleProfileExporter) {
        this.configuration = configuration;
        this.project = project;
        this.rulesProfile = rulesProfile;
        this.ruleFinder = ruleFinder;
        this.scalastyleProfileExporter = scalastyleProfileExporter;
        this.projectClassloader = projectClassloader;
    }

    public void execute(SensorContext sensorContext) {
        //ScalastyleConfiguration.readFromXml()
        LOG.info("Run Scala Check");
        //List<Message> messages = new ScalastyleChecker().checkFiles(configuration, Directory.getFiles(mc.inputEncoding, mc.directories.map(new File(_)).toSeq));

        TimeProfiler profiler = new TimeProfiler().start("Execute Scalastyle " + ScalestyleVersion.getVersion());
        ClassLoader initialClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ScalastyleChecker.class.getClassLoader());

        Locale initialLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        try {
            ScalastyleConfiguration remoteConfiguration = null;
            String localFilename = "scalastyle-config.xml";

            if (configuration.getLocalRuleFile() == null) {
                remoteConfiguration = createRulesets(ScalastyleConstants.REPOSITORY_KEY);
            } else {
                localFilename = configuration.getLocalRuleFile();
            }

            MainConfig config = ScalastyleRunner.prepareConfig(true, localFilename, JavaConversions.asScalaBuffer(configuration.getSourceDir()).toList(), true, false, false, configuration.getCharset().name());

            List<Message<FileSpec>> messages = ScalastyleRunner.execute(config, remoteConfiguration);

            MessageHelper messageHelper = ScalastyleRunner.messageHelper();

            ScalastyleRaporter.fillContext(messages, sensorContext);

            for (Message<FileSpec> elem : JavaConversions.asJavaIterable(messages)) {
                Violation v = toViolation(elem, sensorContext, messageHelper);
                if (v != null) sensorContext.saveViolation(v);
            }

            profiler.stop();
        } catch (Exception e) {
            throw new SonarException("Can not execute Scalastyle", e);

        } finally {
            Thread.currentThread().setContextClassLoader(initialClassLoader);
            Locale.setDefault(initialLocale);
        }
    }

    private ScalastyleConfiguration createRulesets(String repositoryKey) {
        String rulesXml = scalastyleProfileExporter.exportProfile(repositoryKey, rulesProfile);

        return ScalastyleConfiguration.readFromString(rulesXml);
    }

    public Violation toViolation(Message<FileSpec> msg, SensorContext context, MessageHelper messageHelper) {

        if (msg instanceof StyleError) {
            StyleError error = ((StyleError) msg);
            Resource resource = findResourceFor(error);
            if (context.getResource(resource) == null) {
                // Save violations only for existing resources
                LOG.info("No file " + error.fileSpec());
                return null;
            }

            Rule rule = findRuleFor(error);
            if (rule == null) {
                // Save violations only for enabled rules
                LOG.warn("No rule found  " + error.key());
                return null;
            }

            int lineId = Integer.valueOf(error.lineNumber().get().toString());

            String message = (String) ScalastyleRunner.message(error, messageHelper).productElement(2);

            return Violation.create(rule, resource).setLineId(lineId).setMessage(message);
        } else {
            LOG.info("No error:" + msg);
            return null;
        }
    }

    private Resource findResourceFor(StyleError violation) {
        InputFile inputFile = InputFileUtils.create(project.getFileSystem().getBasedir(),
                new java.io.File(((FileSpec) violation.fileSpec()).name()));
        return ScalaFile.fromInputFile(inputFile);
    }

    private Rule findRuleFor(StyleError violation) {
        String ruleKey = violation.key();
        LOG.info("Looking for key:" + ruleKey);
        Rule rule = ruleFinder.findByKey(ScalastyleConstants.REPOSITORY_KEY, ruleKey);
        if (rule != null) {
            return rule;
        }
        return ruleFinder.findByKey(ScalastyleConstants.TEST_REPOSITORY_KEY, ruleKey);

    }
}

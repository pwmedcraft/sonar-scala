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
package org.sonar.plugins.scala.surefire;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.CoverageExtension;
import org.sonar.api.batch.DependsUpon;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.plugins.scala.language.Scala;

public class SurefireSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(SurefireSensor.class);
  private final Settings settings;
  private final FileSystem fileSystem;

  @DependsUpon
  public Class<?> dependsUponCoverageSensors() {
    return CoverageExtension.class;
  }

  public SurefireSensor (Settings settings, FileSystem fileSystem){
	  this.settings = settings;
	  this.fileSystem = fileSystem;
  }
  
  public boolean shouldExecuteOnProject(Project project) {
      
      if(project.getAnalysisType().isDynamic(true) && Scala.INSTANCE.getKey().equals(project.getLanguageKey())){
          LOG.info("SurefireSensor will be executed");                
          return true;
      } else {
          LOG.info("SurefireSensor will NOT be executed"); 
          return false;
      }
  }

  public void analyse(Project project, SensorContext context) {
	  File dir = SurefireUtils.getReportsDirectory(settings, project);
	    collect(project, context, dir);
  }

  protected void collect(Project project, SensorContext context, File reportsDir) {
    LOG.info("parsing {}", reportsDir);
    new ScalaSurefireParser(fileSystem).collect(project, context, reportsDir);
  }

  @Override
  public String toString() {
    return "Scala SurefireSensor";
  }
}

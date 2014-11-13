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

import org.sonar.api.batch.SensorContext;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.plugins.scala.language.Scala;
import org.sonar.plugins.scala.sensor.AbstractScalaSensor;

/**
 * This is the main sensor of the Scala Scalastyle. It gathers all results
 * of the computation from Scalastyle.
 *
 * @author Tomasz Mosiej
 * @since 1.0.1
 */
public class ScalastyleSensor extends AbstractScalaSensor {

    private RulesProfile profile;
    private ScalastyleExecutor executor;

    public ScalastyleSensor(RulesProfile profile, ScalastyleExecutor executor,Scala scala) {
        super(scala);
        this.profile = profile;
        this.executor = executor;
    }

    @Override
    public void analyse(Project project, SensorContext context) {
        executor.execute(context);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}

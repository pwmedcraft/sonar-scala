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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.CoverageExtension;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;

public class SurefireSensorTest {

    private SurefireSensor sensor;
    private Settings settings;
    private FileSystem fileSystem;
    private Project project;
    
    @Before
    public void setUp() {
    	this.settings = new Settings();
    	this.fileSystem = new DefaultFileSystem();
    	sensor = new SurefireSensor(settings, fileSystem);
    	project = mock(Project.class);
    }


    @Test
    public void shouldDependOnCoverageSensors() {
        assertEquals(CoverageExtension.class, sensor.dependsUponCoverageSensors());
    }

    @Test
    public void testToString() {
        assertEquals("Scala SurefireSensor", sensor.toString());
    }
}
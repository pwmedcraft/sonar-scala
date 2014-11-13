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

import org.sonar.plugins.scala.language.Scala;

/**
 * @author Tomasz Mosiej
 * @since 1.0.1
 */
public class ScalastyleConstants {
    public static final String REPOSITORY_NAME = "Scalastyle";
    public static final String PLUGIN_NAME = "Scala";
    public static final String REPOSITORY_KEY = Scala.INSTANCE.getKey();
    public static final String TEST_REPOSITORY_KEY = REPOSITORY_KEY + "-unit-tests";

    private ScalastyleConstants() {
    }
}

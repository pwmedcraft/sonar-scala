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
package org.sonar.plugins.scala.scalastyle

import java.util.Date

import com.typesafe.config.{Config, ConfigFactory}
import java.io.File
import org.scalastyle._

import scala.collection.JavaConversions.seqAsJavaList

object ScalastyleRunner {

     private[this] def now(): Long = new Date().getTime()

     def prepareConfig(error : Boolean, config : String, directories : List[String], verbose : Boolean,
                       quiet : Boolean, warningsaserrors : Boolean, inputEncoding : String ): MainConfig = {
        MainConfig(error,Some(config),directories,verbose,quiet,warningsaserrors,None,None,Some(inputEncoding))
     }

     def execute(mc: MainConfig): List[org.scalastyle.Message[FileSpec]] = {
      val start = now()
      val configuration = ScalastyleConfiguration.readFromXml(mc.config.get)

      println("Input dirs:" + mc.directories.mkString(","))
      val messages = new ScalastyleChecker().checkFiles(configuration, Directory.getFiles(mc.inputEncoding, mc.directories.map(new File(_)).toSeq))

      // scalastyle:off regex
      val config = ConfigFactory.load()
      val outputResult = new TextOutput(config).output(messages)
      mc.xmlFile match {
        case Some(x) => {
          val encoding = mc.xmlEncoding.getOrElse(mc.inputEncoding.get).toString
          XmlOutput.save(config, x, encoding, messages)
        }
        case None =>
      }

      if (!mc.quiet) println("Processed " + outputResult.files + " file(s)")
      if (!mc.quiet) println("Found " + outputResult.errors + " errors")
      if (!mc.quiet) println("Found " + outputResult.warnings + " warnings")
      if (!mc.quiet) println("Finished in " + (now - start) + " ms")

      // scalastyle:on regex
      messages
    }

   def messageHelper:MessageHelper = {
     val config = ConfigFactory.load
     new MessageHelper(config)
   }

  def message(m: Message[FileSpec], messageHelper : MessageHelper):Tuple3[String,String,String] = m match {
    case StyleError(file, clazz, key, level, args, line, column, customMessage) =>
      (messageHelper.text(level.name), file.name, Output.findMessage(messageHelper, key, args, customMessage))
    case StyleException(file, clazz, message, stacktrace, line, column) =>
      ("",file.name,message)
    case _ => (null,null,null)
  }

}

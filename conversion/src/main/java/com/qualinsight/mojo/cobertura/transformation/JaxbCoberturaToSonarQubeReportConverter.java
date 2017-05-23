/*
 * qualinsight-mojo-cobertura
 * Copyright (c) 2015-2017, QualInsight
 * http://www.qualinsight.com/
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, you can retrieve a copy
 * from <http://www.gnu.org/licenses/>.
 */
package com.qualinsight.mojo.cobertura.transformation;

import net.sourceforge.cobertura.coveragedata.ProjectData;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.io.File;

/**
 * @author pfrank
 */
public class JaxbCoberturaToSonarQubeReportConverter implements CoberturaToSonarQubeReportConverter {
  private final Log log;
  private final ProjectData projectData;
  private final File destinationSonarXmlFile;

  public JaxbCoberturaToSonarQubeReportConverter(final Log log, final ProjectData projectData, final File destinationSonarXmlFile){
    if(log == null){
      throw new IllegalArgumentException("Log cannot be null");
    }
    if(projectData == null){
      throw new IllegalArgumentException("ProjectData cannot be null");
    }
    if(destinationSonarXmlFile == null){
      throw new IllegalArgumentException("File destinationSonarXmlFile cannot be null.");
    }

    this.log = log;
    this.projectData = projectData;
    this.destinationSonarXmlFile = destinationSonarXmlFile;
  }

  @Override
  public void convertReport(
      final String[] sourcesPaths,
      final String projectPath) throws MojoExecutionException {
    throw new UnsupportedOperationException("This is not supported yet");
  }
}

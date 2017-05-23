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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

/**
 * @author pfrank
 */
public class XslCoberturaToSonarQubeReportConverter implements CoberturaToSonarQubeReportConverter {
  private final Log log;
  private final File conversionInputFile;
  private final File conversionOutputFile;

  public XslCoberturaToSonarQubeReportConverter(final Log log, final File conversionInputFile, final File conversionOutputFile){
    if(log == null){
      throw new IllegalArgumentException("Log cannot be null.");
    }
    if(conversionInputFile == null){
      throw new IllegalArgumentException("conversionInputFile cannot be null.");
    }
    if(conversionOutputFile == null){
      throw new IllegalArgumentException("conversionOutputFile cannot be null");
    }

    this.log = log;
    this.conversionInputFile = conversionInputFile;
    this.conversionOutputFile = conversionOutputFile;
  }

  private Log getLog(){
    return log;
  }

  @Override
  public void convertReport(final String[] sourcesPaths, final String projectPath) throws MojoExecutionException {
    getLog().debug("Converting Cobertura report to SonarQube generic test coverage report format");
    try {
      final String sourcesPath = sourcesPaths[0];
      final String sourceDirectory = sourcesPath.substring(projectPath.length());
      if (!sourceDirectory.endsWith(File.separator)) {
        throw new MojoExecutionException("sourcesPath property must end with '" + File.separator + "' character. Stoping mojo execution.");
      }
      getLog().debug("XSLT SRC_DIR variable is set to: " + sourceDirectory);
      new CoberturaToSonarQubeCoverageReportConverter(sourceDirectory).withInputFile(conversionInputFile)
                                                                      .withOuputFile(conversionOutputFile)
                                                                      .process();
    } catch (final CoberturaToSonarQubeCoverageReportConversionProcessingException e) {
      final String message = "An error occurred during coverage output conversion: ";
      getLog().error(message, e);
      throw new MojoExecutionException(message, e);
    } catch (final TransformerConfigurationException e) {
      final String message = "An error occurred during transformation configuration: ";
      getLog().error(message, e);
      throw new MojoExecutionException(message, e);
    } catch (final ParserConfigurationException e) {
      final String message = "An error occurred during parser configuration: ";
      getLog().error(message, e);
      throw new MojoExecutionException(message, e);
    } catch (final IOException e) {
      final String message = "An error occurred while trying to access conversion files: ";
      getLog().error(message, e);
      throw new MojoExecutionException(message, e);
    }
  }
}

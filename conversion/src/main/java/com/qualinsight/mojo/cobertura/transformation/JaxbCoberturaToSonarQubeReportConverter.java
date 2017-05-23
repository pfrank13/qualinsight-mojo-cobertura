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

import com.qualinsight.mojo.sonar.model.CoverageType;
import com.qualinsight.mojo.sonar.model.FileType;
import com.qualinsight.mojo.sonar.model.LineToCoverType;
import com.qualinsight.mojo.sonar.model.ObjectFactory;

import net.sourceforge.cobertura.coveragedata.ProjectData;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 * @author pfrank
 */
public class JaxbCoberturaToSonarQubeReportConverter implements CoberturaToSonarQubeReportConverter {
  public static final String SONAR_JAXB_PACKAGE = "com.qualinsight.mojo.sonar.model";
  private final Log log;
  private final ProjectData projectData;
  private final File destinationSonarXmlFile;
  private JAXBContext jaxbContext;

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
    try {
      this.jaxbContext = JAXBContext.newInstance(SONAR_JAXB_PACKAGE);
    }catch(JAXBException je){
      throw new IllegalStateException(je);
    }
  }

  @Override
  public void convertReport(
      final String[] sourcesPaths,
      final String projectPath) throws MojoExecutionException {
    try {
      final Marshaller marshaller = jaxbContext.createMarshaller();
      final ObjectFactory factory = new ObjectFactory();

      final CoverageType coverageType = new CoverageType();
      coverageType.setVersion(1);

      final FileType fileType = new FileType();
      fileType.setPath("/my/path");
      coverageType.setFile(fileType);

      final LineToCoverType lineToCoverType = new LineToCoverType();
      lineToCoverType.setBranchesToCover(1);
      lineToCoverType.setCovered("1");
      lineToCoverType.setLineNumber(1);
      lineToCoverType.setCoveredBranches(1);
      lineToCoverType.setValue("1");
      fileType.getLineToCover().add(lineToCoverType);

      final JAXBElement<CoverageType> coverageTypeJAXBElement = factory.createCoverage(coverageType);
      marshaller.marshal(coverageTypeJAXBElement, destinationSonarXmlFile);
    }catch(JAXBException je){
      throw new MojoExecutionException("Marshalling problem", je);
    }

  }
}

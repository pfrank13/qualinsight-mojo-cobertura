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

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.CoverageData;
import net.sourceforge.cobertura.coveragedata.LineData;
import net.sourceforge.cobertura.coveragedata.ProjectData;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
      //There is no real lookup but the project doesn't have Tuple or KeyValuePair
      final Map<String, String> sonarSourcesPathPrefixMap = new HashMap<String, String>();
      for(String sourcesPath : sourcesPaths){
        sonarSourcesPathPrefixMap.put(sourcesPath, sourcesPath.substring(projectPath.length()));
      }

      final Marshaller marshaller = jaxbContext.createMarshaller();
      final ObjectFactory factory = new ObjectFactory();

      final CoverageType coverageType = new CoverageType();
      coverageType.setVersion(1);

      @SuppressWarnings("unchecked")
      final Collection<ClassData> classData = (Collection<ClassData>) projectData.getClasses();
      for(ClassData classDatum : classData){
        final FileType fileType = new FileType();
        coverageType.getFile().add(fileType);

        //Important to set this correctly for HTML reports to be able to link to source code files properly
        fileType.setPath(findPrefix(sonarSourcesPathPrefixMap, classDatum.getSourceFileName()) + classDatum.getSourceFileName());

        LineData lineData;
        LineToCoverType lineToCoverType;
        for(CoverageData line : classDatum.getLines()){
          lineData = (LineData)line;
          lineToCoverType = new LineToCoverType();
          fileType.getLineToCover().add(lineToCoverType);

          lineToCoverType.setLineNumber(lineData.getLineNumber());
          lineToCoverType.setCoveredBranches(lineData.getNumberOfCoveredBranches());
          lineToCoverType.setBranchesToCover(lineData.getNumberOfValidBranches());
          lineToCoverType.setCovered(lineData.isCovered());
        }
      }

      final JAXBElement<CoverageType> coverageTypeJAXBElement = factory.createCoverage(coverageType);
      marshaller.marshal(coverageTypeJAXBElement, destinationSonarXmlFile);
    }catch(JAXBException je){
      throw new MojoExecutionException("Marshalling problem", je);
    }

  }

  /**
   * Find the correct file prefix path for the given sourceFileName, choose the first one if no file exists that matches
   * the prefixes present in the project
   *
   * @param sonarSourcesPathPrefixMap
   * @param sourceFileName
   * @return
   */
  String findPrefix(final Map<String, String> sonarSourcesPathPrefixMap, final String sourceFileName){
    String defaultPrefix = null;
    File fileToCheck;
    for(Map.Entry<String, String> sonarSourcesPathPrefixEntry : sonarSourcesPathPrefixMap.entrySet()){
      fileToCheck = new File(sonarSourcesPathPrefixEntry.getKey() + sourceFileName);
      if(fileToCheck.exists()){
        return sonarSourcesPathPrefixEntry.getValue();
      }else {
        //Should we even bother defaulting?
        //Default to the first encountered prefix
        if(defaultPrefix == null){
          defaultPrefix = sonarSourcesPathPrefixEntry.getValue();
        }
      }
    }

    return defaultPrefix;
  }
}

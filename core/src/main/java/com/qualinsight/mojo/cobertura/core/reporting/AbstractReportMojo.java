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
package com.qualinsight.mojo.cobertura.core.reporting;

import java.io.File;

import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.dsl.Arguments;
import net.sourceforge.cobertura.dsl.ArgumentsBuilder;
import net.sourceforge.cobertura.dsl.Cobertura;
import net.sourceforge.cobertura.dsl.ReportFormat;
import net.sourceforge.cobertura.reporting.Report;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import com.qualinsight.mojo.cobertura.transformation.CoberturaToSonarQubeReportConverter;
import com.qualinsight.mojo.cobertura.transformation.JaxbCoberturaToSonarQubeReportConverter;
import com.qualinsight.mojo.cobertura.transformation.XslCoberturaToSonarQubeReportConverter;

public abstract class AbstractReportMojo extends AbstractMojo {

    /**
     * Coverage report file name generated by Cobertura.
     */
    public static final String COBERTURA_COVERAGE_FILE_NAME = "coverage.xml";

    /**
     * Default name of the converted coverage report file that will be generated by the Mojo.
     */
    public static final String CONVERTED_COVERAGE_FILE_NAME = "converted-coverage.xml";

    @Parameter(defaultValue = "${project.basedir}/", required = false, readonly = true)
    private String projectPath;

    @Parameter(defaultValue = "${project.basedir}/src/main/java/", required = false)
    private String sourcesPath;

    @Parameter(required = false)
    private String[] sourcesPaths;

    @Parameter(defaultValue = "UTF-8", required = false)
    private String encoding;

    @Parameter(required = false)
    private String[] formats = {
        "xml"
    };

    @Parameter(defaultValue = "true", required = false)
    private boolean convertToSonarQubeOutput;

    @Parameter(defaultValue = "false", required = false)
    private boolean calculateMethodComplexity;

    @Parameter(defaultValue = "true", required = false)
    private final boolean useXslTransform = true;

    protected void prepareFileSystem(final File destinationDirectory) throws MojoExecutionException {
        getLog().debug("Preparing Cobertura report generation directories");
        if (!destinationDirectory.exists() && !destinationDirectory.mkdirs()) {
            final String message = "An error occured during directories preparation: ";
            getLog().error(message);
            throw new MojoExecutionException(message);
        }
    }

    protected void processReporting(final Arguments arguments) {
        getLog().debug("Generating Cobertura report");
        final Cobertura cobertura = new Cobertura(arguments);
        final Report report = cobertura.report();
        for (final String format : this.formats) {
            report.export(ReportFormat.getFromString(format));
        }
    }

    protected void convertToSonarQubeReport(final ProjectData projectData) throws MojoExecutionException {
        if (this.convertToSonarQubeOutput) {
            boolean foundXmlFormat = false;
            for (final String format : this.formats) {
                if ("xml".equalsIgnoreCase(format)) {
                    final CoberturaToSonarQubeReportConverter coberturaToSonarQubeReportConverter;
                    final File conversionOutputFile = new File(coverageReportPath() + CONVERTED_COVERAGE_FILE_NAME);
                    if(useXslTransform()) {
                        final File conversionInputFile = new File(coverageReportPath() + COBERTURA_COVERAGE_FILE_NAME);

                        coberturaToSonarQubeReportConverter = new XslCoberturaToSonarQubeReportConverter(getLog(), conversionInputFile, conversionOutputFile);
                    }else{
                        coberturaToSonarQubeReportConverter = new JaxbCoberturaToSonarQubeReportConverter(getLog(), projectData, conversionOutputFile);
                    }

                    coberturaToSonarQubeReportConverter.convertReport(_getSourcesPaths(), projectPath);
                    foundXmlFormat = true;
                }
            }
            if (!foundXmlFormat) {
                getLog().warn("Conversion to SonarQube generic test coverage format skipped: report format should be 'xml' but was '" + this.formats + "'.");
            }
        }
    }

    protected String projectPath() {
        return this.projectPath;
    }

    protected String sourcesPath() {
        return this.sourcesPath;
    }

    protected String[] sourcesPaths(){
        return this.sourcesPaths;
    }

    protected String encoding() {
        return this.encoding;
    }

    protected String[] formats() {
        return this.formats;
    }

    protected boolean useXslTransform(){
        return this.useXslTransform;
    }

    abstract String coverageReportPath();

    Arguments buildCoberturaReportArguments(final File sourcesDirectory, final File destinationDirectory, final File dataFile) {
        getLog().debug("Building Cobertura report generation arguments");
        final ArgumentsBuilder builder = new ArgumentsBuilder();
        builder.setBaseDirectory(sourcesDirectory.getAbsolutePath())
            .setDestinationDirectory(destinationDirectory.getAbsolutePath())
            .setDataFile(dataFile.getAbsolutePath())
            .setEncoding(encoding())
            .calculateMethodComplexity(this.calculateMethodComplexity);

        for(String sourcesPath : _getSourcesPaths()){
            builder.addSources(sourcesPath, true);
        }

        return builder.build();
    }

    String[] _getSourcesPaths(){
        final String[] _sourcesPaths;
        if(this.sourcesPaths != null && this.sourcesPaths.length > 0){
            _sourcesPaths = this.sourcesPaths;
        }else{
            _sourcesPaths = new String[]{this.sourcesPath};
        }

        return _sourcesPaths;
    }
}

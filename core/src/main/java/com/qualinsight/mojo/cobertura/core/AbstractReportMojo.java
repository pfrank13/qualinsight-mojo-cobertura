/*
 * qualinsight-mojo-cobertura
 * Copyright (c) 2015, QualInsight
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
package com.qualinsight.mojo.cobertura.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import net.sourceforge.cobertura.dsl.Arguments;
import net.sourceforge.cobertura.dsl.Cobertura;
import net.sourceforge.cobertura.dsl.ReportFormat;
import net.sourceforge.cobertura.reporting.Report;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import com.qualinsight.mojo.cobertura.transformation.CoberturaToSonarQubeCoverageReportConversionProcessingException;
import com.qualinsight.mojo.cobertura.transformation.CoberturaToSonarQubeCoverageReportConverter;

abstract class AbstractReportMojo extends AbstractMojo {

    public static final String BASE_COVERAGE_FILE_NAME = "coverage.xml";

    public static final String CONVERTED_COVERAGE_FILE_NAME = "converted-coverage.xml";

    @Parameter(defaultValue = "${project.basedir}/", required = false, readonly = true)
    private String projectDirectoryPath;

    @Parameter(defaultValue = "${project.basedir}/src/main/", required = false)
    private String baseDirectoryPath;

    @Parameter(defaultValue = "UTF-8", required = false)
    private String encoding;

    @Parameter(defaultValue = "xml", required = false)
    private String format;

    @Parameter(defaultValue = "true", required = false)
    private Boolean convertToSonarQubeOutput;

    protected void prepareFileSystem(final File destinationDirectory) throws MojoExecutionException {
        getLog().debug("Preparing Cobertura report generation directories");
        try {
            Files.createDirectories(destinationDirectory.toPath());
        } catch (final IOException e) {
            final String message = "An error occured during directories preparation: ";
            getLog().error(message, e);
            throw new MojoExecutionException(message, e);
        }
    }

    protected void processReporting(final Arguments arguments) {
        getLog().debug("Generating Cobertura report");
        final Cobertura cobertura = new Cobertura(arguments);
        final Report report = cobertura.report();
        report.export(ReportFormat.getFromString(this.format));
    }

    protected void convertToSonarQubeReport() throws MojoExecutionException {
        if (this.convertToSonarQubeOutput) {
            if ("xml".equalsIgnoreCase(this.format)) {
                final File conversionInputFile = new File(getDestinationDirectoryPath() + BASE_COVERAGE_FILE_NAME);
                final File conversionOutputFile = new File(getDestinationDirectoryPath() + CONVERTED_COVERAGE_FILE_NAME);
                convertReport(conversionInputFile, conversionOutputFile);
            } else {
                getLog().warn("Conversion to SonarQube generic test coverage format skipped: report format should be 'xml' but was '" + this.format + "'.");
            }
        }
    }

    private void convertReport(final File conversionInputFile, final File conversionOutputFile) throws MojoExecutionException {
        getLog().debug("Converting Cobertura report to SonarQube generic test coverage report format");
        try {
            new CoberturaToSonarQubeCoverageReportConverter().withInputFile(conversionInputFile)
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

    protected String getProjectDirectoryPath() {
        return this.projectDirectoryPath;
    }

    protected String getBaseDirectoryPath() {
        return this.baseDirectoryPath;
    }

    protected String getEncoding() {
        return this.encoding;
    }

    protected String getFormat() {
        return this.format;
    }

    abstract Arguments buildReportingArguments(final File baseDirectory, final File destinationDirectory, final File baseDataFile);

    abstract String getDestinationDirectoryPath();

}

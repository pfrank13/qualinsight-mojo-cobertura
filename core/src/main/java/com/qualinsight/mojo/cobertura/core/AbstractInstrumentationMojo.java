/*
 * This file is part of qualinsight-mojo-cobertura-core.
 *
 * qualinsight-mojo-cobertura-core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * qualinsight-mojo-cobertura-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with qualinsight-mojo-cobertura-core.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.qualinsight.mojo.cobertura.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import net.sourceforge.cobertura.dsl.Arguments;
import net.sourceforge.cobertura.dsl.ArgumentsBuilder;
import net.sourceforge.cobertura.dsl.Cobertura;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

abstract class AbstractInstrumentationMojo extends AbstractMojo {

    public static final String BASE_DATA_FILE = "cobertura.ser";

    @Parameter(defaultValue = "${project.build.directory}/classes/", required = false)
    private String classesDirectoryPath;

    @Parameter(defaultValue = "${project.build.directory}/cobertura/backup-classes/", required = false)
    private String backupClassesDirectoryPath;

    @Parameter(defaultValue = "${project.build.directory}/classes/", required = false)
    private String destinationDirectoryPath;

    @Parameter(defaultValue = "true", required = false)
    private Boolean ignoreTrivial;

    @Parameter(defaultValue = "false", required = false)
    private Boolean failOnError;

    @Parameter(defaultValue = "false", required = false)
    private Boolean threadsafeRigorous;

    @Parameter(defaultValue = "UTF-8", required = false)
    private String encoding;

    @Parameter(required = false)
    private String ignoreMethodAnnotation;

    @Parameter(required = false)
    private String ignoreClassAnnotation;

    @Parameter(required = false)
    private String ignoreRegularExpression;

    @Parameter(required = false)
    private String includeClassesRegularExpression;

    @Parameter(required = false)
    private String excludeClassesRegularExpression;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final File classesDirectory = new File(this.classesDirectoryPath);
        final File backupClassesDirectory = new File(this.backupClassesDirectoryPath);
        final File destinationDirectory = new File(this.destinationDirectoryPath);
        final File baseDataFile = new File(BASE_DATA_FILE);
        try {
            if (backupClassesDirectory.exists()) {
                FileUtils.forceDelete(backupClassesDirectory);
            }
            FileUtils.copyDirectory(classesDirectory, backupClassesDirectory);
            Files.deleteIfExists(baseDataFile.toPath());
        } catch (final IOException e) {
            final String message = "An error occured during directories preparation:";
            getLog().error(message, e);
            throw new MojoExecutionException(message, e);
        }

        ArgumentsBuilder builder = new ArgumentsBuilder();
        builder = builder.setBaseDirectory(classesDirectory.getAbsolutePath())
            .setDestinationDirectory(destinationDirectory.getAbsolutePath())
            .setDataFile(baseDataFile.getAbsolutePath())
            .setEncoding(this.encoding)
            .ignoreTrivial(this.ignoreTrivial)
            .failOnError(this.failOnError)
            .threadsafeRigorous(this.threadsafeRigorous)
            .addFileToInstrument(classesDirectory.getAbsolutePath());
        if (!StringUtils.isBlank(this.ignoreRegularExpression)) {
            builder = builder.addIgnoreRegex(this.ignoreRegularExpression);
        }
        if (!StringUtils.isBlank(this.ignoreClassAnnotation)) {
            builder = builder.addIgnoreClassAnnotation(this.ignoreClassAnnotation);
        }
        if (!StringUtils.isBlank(this.ignoreMethodAnnotation)) {
            builder = builder.addIgnoreMethodAnnotation(this.ignoreMethodAnnotation);
        }
        if (!StringUtils.isBlank(this.includeClassesRegularExpression)) {
            builder = builder.addIncludeClassesRegex(this.includeClassesRegularExpression);
        }
        if (!StringUtils.isBlank(this.excludeClassesRegularExpression)) {
            builder = builder.addExcludeClassesRegex(this.excludeClassesRegularExpression);
        }
        final Arguments arguments = builder.build();
        try {
            new Cobertura(arguments).instrumentCode()
                .saveProjectData();
        } catch (final Throwable e) {
            final String message = "An error occured during code instrumentation:";
            getLog().error(message, e);
            throw new MojoExecutionException(message, e);
        }

    }

}

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
package com.qualinsight.mojo.cobertura.core.check;

import com.qualinsight.mojo.cobertura.core.instrumentation.AbstractInstrumentationMojo;

import net.sourceforge.cobertura.check.CoverageResultEntry;
import net.sourceforge.cobertura.dsl.Arguments;
import net.sourceforge.cobertura.dsl.ArgumentsBuilder;
import net.sourceforge.cobertura.dsl.Cobertura;
import net.sourceforge.cobertura.reporting.CoverageThresholdsReport;
import net.sourceforge.cobertura.reporting.ReportName;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * @author pfrank
 */
public abstract class AbstractCheckMojo extends AbstractMojo {
  @Parameter(defaultValue = "${project.basedir}/", required = false, readonly = true)
  private String projectPath;

  @Parameter(defaultValue = "0", required = false)
  private int totalLineCoverage;

  @Parameter(defaultValue = "0", required = false)
  private int totalBranchCoverage;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    final Cobertura cobertura = new Cobertura(buildCoberturaCheckArguments());
    cobertura.checkThresholds();
    final CoverageThresholdsReport coverageThresholdsReport = (CoverageThresholdsReport) cobertura.report().getByName(ReportName.THRESHOLDS_REPORT);
    if(getLog().isDebugEnabled()){
      for(CoverageResultEntry coverageResultEntry : coverageThresholdsReport.getCoverageResultEntries()) {
        getLog().info(
            coverageResultEntry.getName() + " " + coverageResultEntry.getCoverageType() + " " + coverageResultEntry
                .getCoverageLevel() + " " + coverageResultEntry.getCurrentCoverage() + " " + coverageResultEntry
                .isBelowExpectedCoverage() + " " + coverageResultEntry.getExpectedCoverage());
      }
    }
    for(CoverageResultEntry coverageResultEntry : coverageThresholdsReport.getCoverageResultEntries()){
      if(coverageResultEntry.isBelowExpectedCoverage()){
        throw new MojoFailureException(this, "Coverage Failed", coverageResultEntry.getCoverageType() + " " + coverageResultEntry.getName() + " " + coverageResultEntry.getCurrentCoverage() + " is less than " + coverageResultEntry.getExpectedCoverage());
      }
    }
  }

  Arguments buildCoberturaCheckArguments(){
    return new ArgumentsBuilder().setDataFile(getDataFilePath() + AbstractInstrumentationMojo.DATA_FILE_NAME)
                                 .setTotalLineCoverageThreshold(totalLineCoverage / 100D)
                                 .setTotalBranchCoverageThreshold(totalBranchCoverage / 100D)
                                 .build();
  }

  protected abstract String getDataFilePath();
}

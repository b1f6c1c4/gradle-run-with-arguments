package com.github.b1f6c1c4.gradle_run_with_arguments;

import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.openapi.project.Project;

interface IConfigurer {

    RunnerAndConfigurationSettings create(Project project);

    String retrieve(RunnerAndConfigurationSettings runConfig);

    boolean modify(RunnerAndConfigurationSettings runConfig, String args);
}

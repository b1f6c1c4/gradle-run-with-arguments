package com.github.b1f6c1c4.gradle_run_with_arguments;

import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.project.Project;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;

class JarConfigurer implements IConfigurer {

    @Override
    public RunnerAndConfigurationSettings create(Project project) {
        var runManager = RunManagerEx.getInstanceEx(project);
        try {
            var o = Reflection.Get(runManager, "idToType", "cachedValue");
            var t = Reflection.Call(o, "get", "JarApplication");
            var fs = Reflection.Get(t, "factories");
            var f = Array.get(fs, 0);
            if (!(f instanceof ConfigurationFactory)) {
                return null;
            }
            var c = runManager.createConfiguration("./gradlew [run]", (ConfigurationFactory) f);
            var cc = c.getConfiguration();
            var pp = project.getBasePath();
            if (pp == null) {
                return null;
            }
            Reflection.Set(cc, Paths.get(pp, "gradle", "wrapper", "gradle-wrapper.jar").toString(), "myBean", "JAR_PATH");
            Reflection.Set(cc, "run", "myBean", "PROGRAM_PARAMETERS");
            Reflection.Set(cc, pp, "myBean", "WORKING_DIRECTORY");
            return c;
        } catch (IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    @Override
    public String retrieve(RunnerAndConfigurationSettings runConfig) {
        var cfg = runConfig.getConfiguration();
        try {
            var par = Reflection.Get(cfg, "myBean", "PROGRAM_PARAMETERS");
            if (par == null)
                par = "";
            if (!(par instanceof String))
                return null;
            var spar = (String) par;
            if (spar.trim().matches("run --args \".*\""))
                return spar.substring(12, spar.length() - 1);
            return "";
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    @Override
    public boolean modify(RunnerAndConfigurationSettings runConfig, String args) {
        var cfg = runConfig.getConfiguration();
        try {
            // TODO: don't override
            if (args.trim().isEmpty())
                Reflection.Set(cfg, "run", "myBean", "PROGRAM_PARAMETERS");
            else
                Reflection.Set(cfg, "run --args \"" + args + "\"", "myBean", "PROGRAM_PARAMETERS");
            return true;
        } catch (IllegalAccessException e) {
            return false;
        }
    }
}

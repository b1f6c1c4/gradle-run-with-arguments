package com.github.b1f6c1c4.gradle_run_with_arguments;

import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.project.Project;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

class GradleConfigurer implements IConfigurer {

    @Override
    public RunnerAndConfigurationSettings create(Project project) {
        var runManager = RunManagerEx.getInstanceEx(project);
        try {
            var o = Reflection.Get(runManager, "idToType", "cachedValue");
            var t = Reflection.Call(o, "get", "GradleRunConfiguration");
            var fs = Reflection.Get(t, "myFactories");
            var f = Array.get(fs, 0);
            if (!(f instanceof ConfigurationFactory)) {
                return null;
            }
            var c = runManager.createConfiguration("gradle [run]", (ConfigurationFactory) f);
            var cc = c.getConfiguration();
            var pp = project.getBasePath();
            if (pp == null) {
                return null;
            }
            var nms = new ArrayList<String>();
            nms.add("run");
            Reflection.Set(cc, nms, "mySettings", "myTaskNames");
            Reflection.Set(cc, pp, "mySettings", "myExternalProjectPath");
            return c;
        } catch (IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    @Override
    public String retrieve(RunnerAndConfigurationSettings runConfig) {
        var cfg = runConfig.getConfiguration();
        try {
            var par = Reflection.Get(cfg, "mySettings", "myScriptParameters");
            if (par == null)
                par = "";
            if (!(par instanceof String))
                return null;
            var spar = (String) par;
            if (spar.trim().matches("--args \".*\""))
                return spar.substring(8, spar.length() - 1);
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
                Reflection.Set(cfg, "", "mySettings", "myScriptParameters");
            else
                Reflection.Set(cfg, "--args \"" + args + "\"", "mySettings", "myScriptParameters");
            return true;
        } catch (IllegalAccessException e) {
            return false;
        }
    }
}

package com.github.b1f6c1c4.gradle_run_with_arguments;

import com.intellij.execution.ExecutionTargetManager;
import com.intellij.execution.ExecutorRegistry;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Ref;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.*;
import java.util.stream.Collectors;

public class RunWithArguments extends AnAction {

    @Override
    public void update(AnActionEvent e) {
        var project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (project == null)
            return;

        var init = retrieve(project);
        if (init == null)
            return;

        var disposable = Disposer.newDisposable();
        var tf = new JTextField(init, 20);
        var panel = LabeledComponent.create(tf, "Arguments");

        var popup = JBPopupFactory.getInstance().createComponentPopupBuilder(panel, tf)
                .setFocusable(true)
                .setRequestFocus(true)
                .setCancelOnOtherWindowOpen(true)
                .setCancelOnClickOutside(true)
                .setShowBorder(true)
                .createPopup();

        Disposer.register(popup, disposable);

        new DumbAwareAction() {
            @Override
            public void actionPerformed(@NotNull AnActionEvent ee) {
                modify(project, tf.getText());
                popup.cancel();
                execute(project);
            }
        }.registerCustomShortcutSet(CustomShortcutSet.fromString("ENTER", "shift ENTER"), tf, popup);

        popup.setMinimumSize(new Dimension(300, 0));
        popup.showCenteredInCurrentWindow(project);
    }

    private RunnerAndConfigurationSettings getConfig(Project project) {
        var runManager = RunManagerEx.getInstanceEx(project);
        var runConfigs = runManager.getAllSettings()
                .stream()
                .filter((c) -> c.getType().getId().equals("GradleRunConfiguration"))
                .collect(Collectors.toList());
        if (runConfigs.isEmpty()) {
            Messages.showMessageDialog(project, "Error: please setup a Gradle configuration first!", "Run with Arguments", Messages.getErrorIcon());
            return null;
        }
        if (runConfigs.size() > 1) {
            Messages.showMessageDialog(project, "Error: please setup only one Gradle configuration!", "Run with Arguments", Messages.getErrorIcon());
            return null;
        }
        return runConfigs.get(0);
    }

    private String retrieve(Project project) {
        var runConfig = getConfig(project);
        if (runConfig == null)
            return null;

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
            Messages.showMessageDialog(project, "Error: unknown error!", "Run with Arguments", Messages.getErrorIcon());
            return null;
        }
    }

    private void modify(Project project, String args) {
        var runConfig = getConfig(project);
        if (runConfig == null)
            return;

        var cfg = runConfig.getConfiguration();
        try {
            // TODO: don't override
            if (args.trim().isEmpty())
                Reflection.Set(cfg, "", "mySettings", "myScriptParameters");
            else
                Reflection.Set(cfg, "--args \"" + args + "\"", "mySettings", "myScriptParameters");
        } catch (IllegalAccessException e) {
            Messages.showMessageDialog(project, "Error: unknown error!", "Run with Arguments", Messages.getErrorIcon());
        }
    }

    private void execute(Project project) {
        var executor = ExecutorRegistry.getInstance().getExecutorById(DefaultRunExecutor.EXECUTOR_ID);
        if (executor == null) {
            Messages.showMessageDialog(project, "Error: unable to find executor!", "Run with Arguments", Messages.getErrorIcon());
            return;
        }

        var runner = ProgramRunner.findRunnerById(executor.getId());
        if (runner == null) {
            Messages.showMessageDialog(project, "Error: unable to find runner!", "Run with Arguments", Messages.getErrorIcon());
            return;
        }

        var runConfig = getConfig(project);
        if (runConfig == null)
            return;

        var target = ExecutionTargetManager.getActiveTarget(project);
        ExecutionUtil.runConfiguration(runConfig, executor, target);
    }
}

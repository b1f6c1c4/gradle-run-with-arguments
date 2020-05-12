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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

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
        var runConfig = runManager.getSelectedConfiguration();
        if (runConfig != null) {
            return runConfig;
        }
        var ans = JOptionPane.showConfirmDialog(null, "Create a configuration for you?", "Run with Arguments", JOptionPane.YES_NO_OPTION);
        if (ans == JOptionPane.NO_OPTION)
            return null;
        var msg = "Use JAR Application?\n" +
                "YES: System.out.print() will function properly, but you can't debug \n" +
                "NO: System.out.print() will break, but you can debug using gradle";
        ans = JOptionPane.showConfirmDialog(null, msg, "Run with Arguments", JOptionPane.YES_NO_CANCEL_OPTION);
        if (ans == JOptionPane.CANCEL_OPTION)
            return null;
        var cfg = (ans == JOptionPane.YES_OPTION) ? new JarConfigurer() : new GradleConfigurer();
        var c = cfg.create(project);
        if (c == null) {
            Messages.showMessageDialog(project, "Error: unknown error!", "Run with Arguments", Messages.getErrorIcon());
            return null;
        }
        runManager.addConfiguration(c);
        runManager.setSelectedConfiguration(c);
        msg = (ans == JOptionPane.YES_OPTION) ? "\nDon't forget to Patch your gradle-wrapper.jar!" : "";
        Messages.showMessageDialog(project, "Configuration created. Click the blue triangle again to run." + msg, "Run with Arguments", Messages.getInformationIcon());
        return null;
    }

    private IConfigurer getConfigurer(RunnerAndConfigurationSettings runConfig) {
        if (runConfig == null)
            return null;

        var id = runConfig.getType().getId();
        switch (id) {
            case "JarApplication":
                return new JarConfigurer();
            case "GradleRunConfiguration":
                return new GradleConfigurer();
            default:
                return null;
        }
    }

    private String retrieve(Project project) {
        var runConfig = getConfig(project);
        if (runConfig == null)
            return null;
        var cfg = getConfigurer(runConfig);
        if (cfg == null) {
            Messages.showMessageDialog(project, "Error: configuration type not supported!", "Run with Arguments", Messages.getErrorIcon());
            return null;
        }

        var res = cfg.retrieve(runConfig);
        if (res == null) {
            Messages.showMessageDialog(project, "Error: unknown error!", "Run with Arguments", Messages.getErrorIcon());
        }
        return res;
    }

    private void modify(Project project, String args) {
        var runConfig = getConfig(project);
        if (runConfig == null)
            return;
        var cfg = getConfigurer(runConfig);
        if (cfg == null) {
            Messages.showMessageDialog(project, "Error: configuration type not supported!", "Run with Arguments", Messages.getErrorIcon());
            return;
        }

        var res = cfg.modify(runConfig, args);
        if (!res) {
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

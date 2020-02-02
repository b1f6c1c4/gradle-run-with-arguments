package com.github.b1f6c1c4.gradle_run_with_arguments;

import com.intellij.execution.ExecutionTargetManager;
import com.intellij.execution.ExecutorRegistry;
import com.intellij.execution.RunManagerEx;
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
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Callable;
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

        var disposable = Disposer.newDisposable();
        var tf = new JTextField("", 20);
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
            public void actionPerformed(AnActionEvent ee) {
                var to = toExecute(project, tf.getText());
                popup.cancel();
                if (to != null) {
                    try {
                        to.call();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }.registerCustomShortcutSet(CustomShortcutSet.fromString("ENTER", "shift ENTER"), tf, popup);

        popup.setMinimumSize(new Dimension(300, 0));
        popup.showCenteredInCurrentWindow(project);
    }

    private Callable<Object> toExecute(Project project, String args) {
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

        var executor = ExecutorRegistry.getInstance().getExecutorById(DefaultRunExecutor.EXECUTOR_ID);
        if (executor == null) {
            Messages.showMessageDialog(project, "Error: unable to find executor!", "Run with Arguments", Messages.getErrorIcon());
            return null;
        }

        var runner = ProgramRunner.findRunnerById(executor.getId());
        if (runner == null) {
            Messages.showMessageDialog(project, "Error: unable to find runner!", "Run with Arguments", Messages.getErrorIcon());
            return null;
        }

        var runConfig = runConfigs.get(0);
        var cfg = runConfig.getConfiguration();
        try {
            var field = cfg.getClass().getSuperclass().getDeclaredField("mySettings");
            field.setAccessible(true);
            var settings = field.get(cfg);
            field = settings.getClass().getDeclaredField("myScriptParameters");
            field.setAccessible(true);
            // TODO: don't override
            if (args.trim().isEmpty())
                field.set(settings, "");
            else
                field.set(settings, "--args \"" + args + "\"");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Messages.showMessageDialog(project, "Error: unknown error!", "Run with Arguments", Messages.getErrorIcon());
            return null;
        }

        var target = ExecutionTargetManager.getActiveTarget(project);

        return () -> {
            ExecutionUtil.runConfiguration(runConfig, executor, target);
            return null;
        };
    }
}

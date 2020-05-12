package com.github.b1f6c1c4.gradle_run_with_arguments;

import com.intellij.execution.RunManagerEx;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public abstract class AddConfiguration extends AnAction {
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

        var runManager = RunManagerEx.getInstanceEx(project);
        var c = getConfigurer().create(project);
        if (c == null) {
            Messages.showMessageDialog(project, "Error: unknown error!", "Run with Arguments", Messages.getErrorIcon());
            return;
        }
        runManager.addConfiguration(c);
        runManager.setSelectedConfiguration(c);
        var msg = getMessage();
        Messages.showMessageDialog(project, "Configuration created. Click the blue triangle again to run." + msg, "Run with Arguments", Messages.getInformationIcon());
    }

    protected abstract String getMessage();

    protected abstract IConfigurer getConfigurer();
}

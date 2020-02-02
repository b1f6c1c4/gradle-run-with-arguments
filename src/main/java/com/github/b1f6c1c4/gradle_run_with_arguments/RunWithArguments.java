package com.github.b1f6c1c4.gradle_run_with_arguments;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class RunWithArguments extends AnAction {

    @Override
    public void update(AnActionEvent e) {
        var project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        Messages.showMessageDialog(project, "Arguments:", "Run with Arguments", Messages.getQuestionIcon());
    }

}

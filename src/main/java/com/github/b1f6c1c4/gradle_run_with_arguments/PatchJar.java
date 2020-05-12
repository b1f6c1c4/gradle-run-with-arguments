package com.github.b1f6c1c4.gradle_run_with_arguments;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class PatchJar extends AnAction {

    @Override
    public void update(AnActionEvent e) {
        var file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        var visible = file != null && file.getName().equals("gradle-wrapper.jar");
        e.getPresentation().setEnabledAndVisible(visible);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (project == null)
            return;

        var file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (file == null)
            return;

        var path = file.getPath();

        // TODO: actually patch this

        Messages.showMessageDialog(project, "JAR patched", "Run with Arguments", Messages.getInformationIcon());
    }
}

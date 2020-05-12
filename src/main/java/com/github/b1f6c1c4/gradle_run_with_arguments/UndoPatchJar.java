package com.github.b1f6c1c4.gradle_run_with_arguments;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class UndoPatchJar extends AnAction {

    @Override
    public void update(AnActionEvent e) {
        var file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (file != null && file.getName().equals("gradle-wrapper.jar")) {
            var fBak = new File(file.getPath() + ".bak");
            var fileBak = LocalFileSystem.getInstance().findFileByIoFile(fBak);
            if (fileBak != null) {
                e.getPresentation().setEnabledAndVisible(true);
                return;
            }
        }
        if (file != null && file.getName().equals("gradle-wrapper.jar.bak")) {
            var fOrig = new File(file.getPath().substring(0, file.getPath().length() - 4));
            var fileOrig = LocalFileSystem.getInstance().findFileByIoFile(fOrig);
            if (fileOrig != null) {
                e.getPresentation().setEnabledAndVisible(true);
                return;
            }
        }
        e.getPresentation().setEnabledAndVisible(false);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (project == null)
            return;

        String path;
        {
            var file = e.getData(CommonDataKeys.VIRTUAL_FILE);
            if (file == null)
                return;
            path = file.getPath();
            if (path.endsWith(".bak"))
                path = path.substring(0, path.length() - 4);
        }

        try {
            var fOrig = new File(path);
            var fBak = new File(path + ".bak");

            if (!fBak.exists())
                throw new IOException("No backup file found");
            if (!fOrig.delete())
                throw new IOException("Cannot delete  " + fOrig.getAbsolutePath());
            if (!fBak.renameTo(fOrig))
                throw new IOException("Cannot rename from " + fBak.getAbsolutePath() + " to " + fOrig.getAbsolutePath());

            LocalFileSystem.getInstance().refresh(false);
            Messages.showMessageDialog(project, "The JAR has been recovered successfully.", "Run with Arguments", Messages.getInformationIcon());
        } catch (IOException ex) {
            ex.printStackTrace();
            LocalFileSystem.getInstance().refresh(true);
            Messages.showMessageDialog(project, "Error(s) occurred during recovering. Please check the logs.", "Run with Arguments", Messages.getErrorIcon());
        }
    }
}

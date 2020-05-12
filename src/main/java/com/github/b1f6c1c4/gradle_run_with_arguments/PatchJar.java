package com.github.b1f6c1c4.gradle_run_with_arguments;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.newvfs.RefreshQueue;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class PatchJar extends AnAction {

    @Override
    public void update(AnActionEvent e) {
        var file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        var isJar = file != null && file.getName().equals("gradle-wrapper.jar");
        if (isJar) {
            var fBak = new File(file.getPath() + ".bak");
            var fileBak = LocalFileSystem.getInstance().findFileByIoFile(fBak);
            if (fileBak == null) {
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
        }

        try {
            var fOrig = new File(path);
            var fBak = new File(path + ".bak");
            var fPatch = new File(path + "-patched.jar");

            try (var jar = new JarFile(path, true)) {
                var man = jar.getManifest();
                man.read(new ByteArrayInputStream(
                        ("Main-Class: org.gradle.wrapper.GradleWrapperMain\r\n" +
                                "Class-Path: gradle-wrapper.jar").getBytes(StandardCharsets.UTF_8)));
                try (var jo = new JarOutputStream(new FileOutputStream(fPatch))) {
                    JarEntry enx = null;
                    for (var en : Collections.list(jar.entries())) {
                        if (en.getName().equals("META-INF/MANIFEST.MF")) {
                            enx = en;
                            continue;
                        }

                        jo.putNextEntry(en);
                        jar.getInputStream(en).transferTo(jo);
                        jo.closeEntry();
                    }

                    if (enx != null) {
                        enx.setCompressedSize(-1);
                        jo.putNextEntry(enx);
                        man.write(jo);
                        jo.closeEntry();
                    }
                }
            }

            if (!fOrig.renameTo(fBak))
                throw new IOException("Cannot rename from " + fOrig.getAbsolutePath() + " to " + fBak.getAbsolutePath());
            if (!fPatch.renameTo(fOrig))
                throw new IOException("Cannot rename from " + fPatch.getAbsolutePath() + " to " + fOrig.getAbsolutePath());

            LocalFileSystem.getInstance().refreshAndFindFileByIoFile(fBak);
            Messages.showMessageDialog(project, "The JAR has been patched successfully.", "Run with Arguments", Messages.getInformationIcon());
        } catch (IOException ex) {
            ex.printStackTrace();
            RefreshQueue.getInstance().createSession(true, true, () -> {
                Messages.showMessageDialog(project, "Error(s) occurred during patching. Please check the logs.", "Run with Arguments", Messages.getErrorIcon());
            });
        }
    }
}

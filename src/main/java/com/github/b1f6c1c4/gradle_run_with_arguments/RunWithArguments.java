package com.github.b1f6c1c4.gradle_run_with_arguments;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.awt.RelativePoint;
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
                var txt = tf.getText();
                Messages.showMessageDialog(project, "Arguments:" + txt, "Run with Arguments", Messages.getQuestionIcon());
            }
        }.registerCustomShortcutSet(CustomShortcutSet.fromString("ENTER", "shift ENTER"), tf, popup);

        popup.setMinimumSize(new Dimension(300, 0));
        popup.showCenteredInCurrentWindow(project);
    }

}

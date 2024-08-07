package com.github.kosavpa.pcontroller.actions

import com.github.kosavpa.pcontroller.utils.PropertiesUtils
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.panel
import java.util.regex.Pattern
import javax.swing.JComponent


class OutputConfigAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        OutputConfigDialog().show()
    }

    private class OutputConfigDialog : DialogWrapper(false) {
        lateinit var path: Cell<JBTextField>

        init {
            init()
        }

        override fun createCenterPanel(): JComponent {
            return panel {
                row {
                    label("Enter path to dir")
                    path = textField()
                }
            }
        }

        override fun doValidate(): ValidationInfo? {
            val textPath = path.component.text ?: return ValidationInfo("Path is null!")

            if (System.getProperty("os.name").lowercase().contains("windows")) {
                val pattern = Pattern.compile("[A-Za-z]:.*")

                if (!pattern.matcher(textPath).matches()) {
                    isOKActionEnabled = true

                    return ValidationInfo("Path non matches!")
                }
            }

            if (System.getProperty("os.name").lowercase().contains("linux")) {
                val pattern = Pattern.compile("^/.+")

                if (!pattern.matcher(textPath).matches()) {
                    isOKActionEnabled = true

                    return ValidationInfo("Path non matches!")
                }
            }

            return null
        }

        override fun doOKAction() {
            super.doOKAction()

            if (doValidate() == null) {
                PropertiesUtils.writeDir(path.component.text.trim(), javaClass.classLoader)
            }
        }
    }
}
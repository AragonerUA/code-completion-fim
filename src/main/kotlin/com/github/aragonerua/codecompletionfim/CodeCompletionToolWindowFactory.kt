package com.github.aragonerua.codecompletionfim

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBPanel
import javax.swing.JButton
import java.awt.BorderLayout
import javax.swing.JTextArea
import com.github.aragonerua.codecompletionfim.TinyStarcoderInference
import com.github.aragonerua.codecompletionfim.CodeLlamaInference
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.ui.components.JBLabel
import java.awt.FlowLayout
import javax.swing.JComboBox
import javax.swing.JPanel

class CodeCompletionToolWindowFactory : ToolWindowFactory {

    private var selectedModel: String = "tiny_starcoder"
    private var selectedCodeGen: String = "multi-line"

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = JPanel(FlowLayout())

        val modelLabel = JBLabel("Choose a Model: ")
        val modelOptions = arrayOf("tiny_starcoder", "starcoder2", "codellama")
        val modelComboBox = JComboBox(modelOptions)

        val codeGenerationOptions = arrayOf("multi-line", "single-line", "random-span")
        val codeComboBox = JComboBox(codeGenerationOptions)

        modelComboBox.addActionListener {
            selectedModel = modelComboBox.selectedItem as String
        }

        codeComboBox.addActionListener {
            selectedCodeGen = codeComboBox.selectedItem as String
        }

        val analyzeButton = JButton("Analyze Code")
        analyzeButton.addActionListener {
            analyzeCode(project, panel)
        }

        panel.add(modelLabel)
        panel.add(modelComboBox)
        panel.add(codeComboBox)
        panel.add(analyzeButton)

        val contentManager = toolWindow.contentManager
        val content = contentManager.factory.createContent(panel, "", false)
        contentManager.addContent(content)
    }

    private fun analyzeCode(project: Project, panel: JPanel) {
        val editor = Utils.getCurrentEditor(project) ?: return
        val caretOffset = editor.caretModel.offset
        val document = editor.document

        val prefix = document.getText(TextRange(0, caretOffset))
        val suffix = document.getText(TextRange(caretOffset, document.textLength))

        val generatedSuggestion = when (selectedModel) {
            "codellama" -> CodeLlamaInference.callInference(prefix, suffix, selectedCodeGen)
            "starcoder2" -> StarcoderInference.callInference(prefix, suffix, selectedCodeGen)
            else -> TinyStarcoderInference.callInference(prefix, suffix, selectedCodeGen)
        }
        if ("Unfortunately" in generatedSuggestion) {
            val errorLabel = JBLabel("<html>${generatedSuggestion.replace("\n", "<br>")}</html>")
            panel.add(errorLabel)
        }
        else {
        Utils.showCompletionSuggestion(editor, generatedSuggestion)
        }
    }
}
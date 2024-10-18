package com.github.aragonerua.codecompletionfim

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project

object Utils {

    fun getCurrentEditor(project: Project): Editor? {
        val editorManager = FileEditorManager.getInstance(project)
        return editorManager.selectedTextEditor
    }

    fun showCompletionSuggestion(editor: Editor, suggestion: String) {
        val document = editor.document
        val caretOffset = editor.caretModel.offset

        WriteCommandAction.runWriteCommandAction(editor.project) {
            document.insertString(caretOffset, suggestion)

            editor.caretModel.moveToOffset(caretOffset + suggestion.length)
        }
    }
}

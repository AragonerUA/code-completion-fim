package com.github.aragonerua.codecompletionfim.actions

import com.github.aragonerua.codecompletionfim.TinyStarcoderInference
import com.intellij.codeInsight.completion.CodeCompletionHandlerBase
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.util.TextRange



class MyCodeCompletionAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val editor: Editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return

        // Trigger completion programmatically at the current caret position
        val completionHandler = CodeCompletionHandlerBase(CompletionType.BASIC)
        completionHandler.invokeCompletion(project, editor)
    }
}
package com.github.aragonerua.codecompletionfim.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import java.io.BufferedReader
import java.io.InputStreamReader
import org.json.JSONObject

class Action : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val editor = event.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR)
        if (editor != null) {
            val document = editor.document
            val caret = editor.caretModel.primaryCaret
            val offset = caret.offset

            // Fetch text around the cursor (before and after the caret)
            val prefix = document.getText(com.intellij.openapi.util.TextRange(0, offset))
            val suffix = document.getText(com.intellij.openapi.util.TextRange(offset, document.textLength))

            // Call Tiny Starcoder inference using the Python script
            val suggestions = getSuggestionsFromTinyStarcoder(prefix, suffix)

            // Display the suggestions in a message dialog
            Messages.showMessageDialog(
                event.project,
                "Completion: ${suggestions}",
                "Code Completion",
                Messages.getInformationIcon()
            )
        }
    }

    private fun getSuggestionsFromTinyStarcoder(prefix: String, suffix: String): String {
        try {
            // Call the Python script via ProcessBuilder
            val processBuilder = ProcessBuilder("python3", "/Users/aragonerua/Documents/GitHub/code-completion-fim/src/python/inference.py", prefix, suffix)
            processBuilder.redirectErrorStream(true)
            val process = processBuilder.start()

            // Read the output from the Python script
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                output.append(line)
            }

            // Wait for the process to finish
            process.waitFor()

            // Parse the output (assuming JSON format)
            val jsonResponse = JSONObject(output.toString())
            return jsonResponse.getString("completion")
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error: ${e.message}"
        }
    }
}
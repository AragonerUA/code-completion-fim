package com.github.aragonerua.codecompletionfim

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

object CodeLlamaInference {

    fun callInference(prefix: String, suffix: String, selectedCodeGen: String): String {
        return try {
            println("Prefix: $prefix")
            println("Suffix: $suffix")

            val scriptPath = "/Users/aragonerua/Documents/GitHub/code-completion-fim/src/python/inference_codellama.py"

            val processBuilder = ProcessBuilder(
                "/Users/aragonerua/Documents/GitHub/code-completion-fim/venv/bin/python",
                scriptPath
            )
            processBuilder.redirectErrorStream(true)
            val process = processBuilder.start()

            val jsonInput = JSONObject()
            jsonInput.put("prefix", prefix)
            jsonInput.put("suffix", suffix)

            jsonInput.put("selectedCodeGen", selectedCodeGen)

            process.outputStream.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    println("Sending JSON Input: $jsonInput")
                    writer.write(jsonInput.toString())
                    writer.flush()
                }
            }

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                output.append(line)
                output.append("\n")
            }

            println("Raw Output from Python: $output")

            val exitCode = process.waitFor()
            println("Process exit code: $exitCode")

            if (exitCode != 0) {
                val firstIndex = output.toString().indexOf("Unfortunately")
                val lastIndex = output.toString().lastIndex
                val indicesRange = firstIndex..lastIndex
                return output.toString().slice(indicesRange)

            }

            if (output.toString().trim().startsWith("{")) {
                val jsonResponse = JSONObject(output.toString())
                println(jsonResponse.getString("completion"))
                return jsonResponse.getString("completion")
            } else {
                println("Error: Invalid JSON output from Python script.")
                return "Invalid JSON output: ${output.toString()}"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error: ${e.message}"
        }
    }
}
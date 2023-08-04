package fr.imacaron.keylogger

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import kotlinx.coroutines.*
import java.io.File

class KeyLoggerReplayHandler: AnAction() {

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)!!
        val relativePath = editor.virtualFile.path.removePrefix(editor.project?.basePath ?: "")
        val f = File(editor.project?.basePath + "/keylogger/" + relativePath + ".keylog")
        e.presentation.isVisible = f.exists() && !(projects.find { it.name == editor.project?.name }?.listeners?.find { it.originalFile == editor.virtualFile.path }?.isRecording ?: false)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)!!
        val text = editor.document.text
        val relativePath = editor.virtualFile.path.removePrefix(editor.project?.basePath ?: "")
        val f = File(editor.project?.basePath + "/keylogger/" + relativePath + ".keylog")
        val lines = f.readLines()
        println(lines)

        GlobalScope.launch {
            WriteCommandAction.runWriteCommandAction(editor.project!!) {
                editor.document.setText("")
            }
            lines.forEach {
                val match = "^([0-9]+):(.*)->(.*)$".toRegex().find(it) ?: return@launch
                println(match.groupValues)
                val index = match.groups[1]!!.value.toInt()
                println(index)
                WriteCommandAction.runWriteCommandAction(editor.project!!) {
                    editor.document.deleteString(index, index + match.groups[2]!!.value.length)
                }
                WriteCommandAction.runWriteCommandAction(editor.project!!) {
                    editor.document.insertString(index, match.groups[3]!!.value.desanitize())
                }
                delay(100)
            }
            WriteCommandAction.runWriteCommandAction(editor.project!!) {
                editor.document.setText(text)
            }
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT
}

fun String.desanitize(): String = this.replace("\\n", "\n").replace("\\-\\>", "->")
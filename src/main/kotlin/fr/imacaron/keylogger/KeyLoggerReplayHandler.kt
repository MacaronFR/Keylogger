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
        e.presentation.isEnabled = projects.find { it.name == e.project?.name } != null
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)!!
        val text = editor.document.text
        val relativePath = editor.virtualFile.path.removePrefix(editor.project?.basePath ?: "")
        val f = File(editor.project?.basePath + "/keylogger/" + relativePath + ".keylog")
        val lines = f.readLines()

        GlobalScope.launch {
            WriteCommandAction.runWriteCommandAction(editor.project!!) {
                editor.document.setText("")
            }
            lines.forEach {
                val match = "^([0-9]+):(.*)->(.*)$".toRegex().find(it) ?: return@launch
                val index = match.groups[1]!!.value.toInt()
                val toDelete = match.groups[2]!!.value
                val toAdd = match.groups[3]!!.value.desanitize()
                val selection = editor.selectionModel
                if(toDelete.length > 1) {
                    WriteCommandAction.runWriteCommandAction(editor.project!!) {
                        selection.setSelection(index, index + toDelete.length)
                    }
                    delay(100)
                }
                WriteCommandAction.runWriteCommandAction(editor.project!!) {
                    editor.document.deleteString(index, index + toDelete.length)
                }
                WriteCommandAction.runWriteCommandAction(editor.project!!) {
                    editor.document.insertString(index, toAdd)
                    val caret = editor.caretModel
                    caret.primaryCaret.moveToOffset(index + toAdd.length)
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
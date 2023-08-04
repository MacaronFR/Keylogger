package fr.imacaron.keylogger

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys

class KeyLoggerStopHandler: AnAction() {

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)!!
        projects.find { it.name == e.project?.name }?.listeners?.find { it.originalFile == editor.virtualFile.path }?.let {
            e.presentation.isVisible = it.isRecording
            return
        }
        e.presentation.isVisible = false
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)!!
        projects.find { it.name == e.project?.name }?.listeners?.find { it.originalFile == editor.virtualFile.path }?.let {
            it.isRecording = false
            editor.document.removeDocumentListener(it)
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT
}
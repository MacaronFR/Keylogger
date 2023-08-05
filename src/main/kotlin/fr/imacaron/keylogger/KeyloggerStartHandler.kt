package fr.imacaron.keylogger

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import kotlinx.coroutines.runBlocking
import java.io.File

class KeyloggerStartHandler: AnAction() {

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)!!
        projects.find { it.name == e.project?.name }?.listeners?.find { it.originalFile == editor.virtualFile.path }?.let {
            if(it.isRecording) {
                e.presentation.isVisible = false
            }else {
                e.presentation.isVisible = true
                runBlocking {
                    projects.find { it.name == e.project?.name }?.listeners?.remove(it)
                }
            }
        } ?: run {
            e.presentation.isVisible = true
        }
        e.presentation.isEnabled = projects.find { it.name == e.project?.name } != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        e.actionManager
        val editor = e.getRequiredData(CommonDataKeys.EDITOR)
        val project = e.getRequiredData(CommonDataKeys.PROJECT)

        val relativePath = editor.virtualFile.path.removePrefix(project.basePath ?: "")
        val f = File(project.basePath + "/keylogger/" + relativePath + ".keylog")

        if(f.exists()) {
            if(f.isFile) {
                f.delete()
            }else {
                f.deleteRecursively()
            }
        }
        f.parentFile.mkdirs()
        f.createNewFile()
        f.writeText("0:->${editor.document.text.sanitize()}\n")

        val listener = LoggingDocumentListener(f, true, editor.virtualFile.path)

        editor.document.addDocumentListener(listener)

        runBlocking(projectsContext) {
            projects.find { it.name == project.name }?.listeners?.add(listener)
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }
}

fun CharSequence.sanitize(): String = this.replace("->".toRegex(), "\\-\\>").replace("\n", "\\n")
package fr.imacaron.keylogger

import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

class KeyLoggerPlugin: ProjectActivity, DynamicPluginListener {
    override suspend fun execute(project: Project) {
        withContext(projectsContext) {
            projects.add(Project(project.name, project.basePath!!, mutableListOf()))
        }
    }

}

@OptIn(DelicateCoroutinesApi::class)
val projectsContext = newSingleThreadContext("Projects Context")

val projects: MutableList<fr.imacaron.keylogger.Project> = mutableListOf()
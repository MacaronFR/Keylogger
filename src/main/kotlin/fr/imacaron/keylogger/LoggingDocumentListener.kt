package fr.imacaron.keylogger

import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import java.io.File

class LoggingDocumentListener(private val f: File, var isRecording: Boolean, val originalFile: String): DocumentListener {
    override fun beforeDocumentChange(event: DocumentEvent) {
        super.beforeDocumentChange(event)
        f.appendText("${event.offset}:${event.oldFragment}->${event.newFragment.sanitize()}\n")
    }
}
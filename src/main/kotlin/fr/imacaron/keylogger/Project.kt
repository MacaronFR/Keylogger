package fr.imacaron.keylogger

data class Project(
    val name: String,
    val path: String,
    val listeners: MutableList<LoggingDocumentListener>
)

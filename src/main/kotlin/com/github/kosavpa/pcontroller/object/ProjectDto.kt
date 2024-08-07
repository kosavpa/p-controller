package com.github.kosavpa.pcontroller.`object`

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ProjectDto(val name: String) {
    private lateinit var dateTime: String

    companion object {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")!!
    }

    constructor(name: String, dateTime: LocalDateTime) : this(name) {
        this.dateTime = formatter.format(dateTime)
    }
}
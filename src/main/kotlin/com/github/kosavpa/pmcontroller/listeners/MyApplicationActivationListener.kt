package com.github.kosavpa.pmcontroller.listeners

import com.github.kosavpa.pmcontroller.`object`.ProjectDto
import com.google.gson.GsonBuilder
import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.IdeFrame
import java.io.File
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

internal class MyApplicationActivationListener : ApplicationActivationListener {
    private var date: LocalDateTime = LocalDateTime.now()

    private var absolutePathFile = ""

    override fun applicationActivated(ideFrame: IdeFrame) {
        writeProjectInfo(ideFrame.project)
    }

    private fun writeProjectInfo(project: Project?) {
        if (isNeedWriteProjectInfo(project)) {
            checkAbsolutePathFile()

            val projectJson = createJson(project!!.name)

            File(absolutePathFile).appendText("\n$projectJson")
        }
    }

    private fun getTimeStampFromAbsolutePath(): LocalDate {
        return LocalDate.from(DateTimeFormatter.ofPattern("dd-MM-yyyy").parse(getTimeStampFromFileName()))
    }

    private fun checkAbsolutePathFile() {
        if (isNeedCreateNewFile()) {
            createFileAndSetPath()
        }
    }

    private fun isNeedCreateNewFile(): Boolean {
        return absolutePathFile.isBlank()
                || Period.between(getTimeStampFromAbsolutePath(), LocalDate.now())[ChronoUnit.DAYS] != 0L
                || !File(absolutePathFile).exists()
    }

    private fun getTimeStampFromFileName(): String {
        return absolutePathFile.substring(
            absolutePathFile.indexOf("(") + 1,
            absolutePathFile.indexOf(")")
        )
    }

    private fun getFileName(): String {
        return String.format(
            readPath("fileName"),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        )
    }

    private fun createFileAndSetPath() {
        val pathToFile = getPathToFile()

        val file = File(pathToFile)

        file.createNewFile()

        absolutePathFile = pathToFile
    }

    private fun getPathToFile(): String {
        return readPath("dirPath") + File.separator + getFileName()
    }

    private fun isNeedWriteProjectInfo(project: Project?): Boolean {
        val period = Duration.between(date, LocalDateTime.now())

        return project != null && period[ChronoUnit.SECONDS] >= 30
    }

    private fun createJson(name: String): String {
        val gson = GsonBuilder()
            .setPrettyPrinting()
            .setDateFormat("dd.MM.yyyy hh:MM")
            .create()

        return gson.toJson(ProjectDto(name, Date()))
    }

    private fun readPath(propName: String): String {
        return javaClass.classLoader.getResourceAsStream("conf.properties")
            .use { Properties().apply { load(it) } }
            .getProperty(propName)
    }
}
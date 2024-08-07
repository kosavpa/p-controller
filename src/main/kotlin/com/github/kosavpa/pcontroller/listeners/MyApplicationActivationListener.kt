package com.github.kosavpa.pcontroller.listeners

import com.github.kosavpa.pcontroller.`object`.ProjectDto
import com.github.kosavpa.pcontroller.utils.PropertiesUtils
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

internal class MyApplicationActivationListener : ApplicationActivationListener {
    private var date: LocalDateTime = LocalDateTime.now()

    private var fileName = ""

    override fun applicationActivated(ideFrame: IdeFrame) {
        writeProjectInfo(ideFrame.project)
    }

    private fun writeProjectInfo(project: Project?) {
        if (isNeedWriteProjectInfo(project)) {
            checkAbsolutePathFile()

            val projectJson = createJson(project!!.name)

            File(
                PropertiesUtils.readProp("dirPath", javaClass.classLoader),
                fileName
            ).appendText("\n$projectJson")
        }
    }

    private fun checkAbsolutePathFile() {
        if (isNeedCreateNewFile()) {
            createFileAndSetPath()
        }
    }

    private fun isNeedCreateNewFile(): Boolean {
        return fileName.isBlank()
                || Period.between(getTimeStampFromAFileName(), LocalDate.now())[ChronoUnit.DAYS] != 0L
                || !File(fileName).exists()
    }

    private fun getTimeStampFromAFileName(): LocalDate {
        return LocalDate.from(DateTimeFormatter.ofPattern("dd-MM-yyyy").parse(getTimeStampFromFileName()))
    }

    private fun getTimeStampFromFileName(): String {
        return fileName.substring(
            fileName.indexOf("(") + 1,
            fileName.indexOf(")")
        )
    }

    private fun createFileAndSetPath() {
        var file = File(getDirPath())

        if (!file.exists()) {
            file.mkdirs()
        }

        fileName = getFileName()

        file = File(file.absolutePath, fileName)

        if (!file.exists()) {
            file.createNewFile()
        }
    }

    private fun getDirPath(): String {
        var dirPath = PropertiesUtils.readProp("dirPath", javaClass.classLoader)

        if (dirPath.isBlank()) {
            dirPath = PropertiesUtils.writeInDefaultDir(javaClass.classLoader)
        }

        return dirPath
    }

    private fun getFileName(): String {
        return String.format(
            PropertiesUtils.readProp("fileName", javaClass.classLoader),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        )
    }

    private fun isNeedWriteProjectInfo(project: Project?): Boolean {
        val period = Duration.between(date, LocalDateTime.now())

        return project != null && period[ChronoUnit.SECONDS] > 1800
    }

    private fun createJson(name: String): String {
        val gson = GsonBuilder()
            .setPrettyPrinting()
            .create()

        return gson.toJson(ProjectDto(name, LocalDateTime.now()))
    }
}
package com.github.kosavpa.pcontroller.utils

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class PropertiesUtils {
    companion object {
        private const val propFileInJar = "conf.properties"

        fun readProp(propName: String, classLoader: ClassLoader): String {
            return if (propName == "dirPath") {
                val confFile = File(getConfDir(classLoader), readProp("defaultConfFile", classLoader))

                if (confFile.exists()) {
                    confFile
                        .inputStream()
                        .use { Properties().apply { load(it) } }
                        .getProperty("dirPath")
                } else {
                    ""
                }
            } else {
                classLoader.getResourceAsStream(propFileInJar)
                    .use { Properties().apply { load(it) } }
                    .getProperty(propName)
            }
        }

        fun writeDir(propValue: String, classLoader: ClassLoader) {
            val defaultDir = createDefaultConfDirAndFile(classLoader)

            writeDirProp(defaultDir.absolutePath, propValue)
        }

        fun writeInDefaultDir(classLoader: ClassLoader): String {
            val defaultFile = createDefaultConfDirAndFile(classLoader)

            val defaultLogDir = createFile(defaultFile.parent + "/log", false)

            writeDirProp(defaultFile.absolutePath, defaultLogDir.absolutePath)

            return defaultLogDir.absolutePath
        }

        private fun writeDirProp(confPath: String, dirProp: String) {
            val properties = Properties()

            properties.setProperty("dirPath", dirProp)
            properties.store(Files.newOutputStream(Path.of(confPath)), null)
        }

        private fun createDefaultConfDirAndFile(classLoader: ClassLoader): File {
            val defaultDir = createFile(getConfDir(classLoader), false)

            return createFile(
                defaultDir.absolutePath + File.separator + readProp("defaultConfFile", classLoader),
                true
            )
        }

        private fun createFile(path: String, isFile: Boolean): File {
            val file = File(path)

            if (isFile) {
                file.createNewFile()
            } else {
                file.mkdirs()
            }

            return file
        }

        private fun getConfDir(classLoader: ClassLoader): String {
            if (System.getProperty("os.name").lowercase().contains("windows")) {
                return classLoader.getResourceAsStream(propFileInJar)
                    .use { Properties().apply { load(it) } }
                    .getProperty("windowsConfigDir")
            }

            if (System.getProperty("os.name").lowercase().contains("linux")) {
                return classLoader.getResourceAsStream(propFileInJar)
                    .use { Properties().apply { load(it) } }
                    .getProperty("linuxConfigDir")
            }

            throw RuntimeException("Not supported OS")
        }
    }
}
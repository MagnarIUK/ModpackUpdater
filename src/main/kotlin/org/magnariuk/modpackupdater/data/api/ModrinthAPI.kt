package org.magnariuk.modpackupdater.data.api

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.magnariuk.modpackupdater.data.classes.ProfileProject
import org.magnariuk.modpackupdater.data.classes.ProjectVersion
import org.magnariuk.modpackupdater.data.classes.VersionManifest
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files.copy


class ModrinthAPI {



    fun download(project: ProfileProject, version: String, loader: String, resultFolder: File) {
        val all_versions: List<ProjectVersion>? = getVersions(project.metadata.project.id)
        if(all_versions != null) {
            val latest_version = filter_versions(all_versions, version, loader)
            if (latest_version != null) {
                val title = project.metadata.project.title
                println("Getting $title")
                downloadFile(latest_version, resultFolder)
            } else{
                println("${project.metadata.project.title} was not found on $loader $version version")
            }
        } else{
            println("Versions not found: ${project.metadata.project.title} | $version | $loader")

        }

    }

    fun downloadFile(p_file: ProjectVersion, outputDir: File){
        val url = URL(p_file.files?.get(0)?.url)
        val connection = url.openConnection() as HttpURLConnection
        try{
            connection.requestMethod = "GET"
            connection.connect()

            println("${connection.responseCode}")
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                if(!outputDir.exists()){
                    outputDir.mkdirs()
                }

                val file = p_file.files?.get(0)?.filename?.let { File(outputDir, it) }

                connection.inputStream.use { inputStream ->
                    if (file != null) {
                        FileOutputStream(file).use { outputStream ->
                            copyStream(inputStream, outputStream)
                        }
                    }
                }
                println("File downloaded successfully: ${file?.absolutePath}")
            } else{
                println( "Failed to download file. HTTP response code: ${connection.responseCode}")
            }
        }  catch (e: Exception) {
            e.printStackTrace()
            println( "Error downloading file: ${e.message}")
        } finally {
            connection.disconnect()
        }
    }
        private fun copyStream(inputStream: InputStream, outputStream: FileOutputStream) {
            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
        }





    fun filter_versions(versions: List<ProjectVersion>, version: String, loader: String): ProjectVersion?{
        val iterator = versions.iterator()
        val vers: MutableList<ProjectVersion> = mutableListOf<ProjectVersion>()
        while (iterator.hasNext()) {
            val ver = iterator.next()
            if(ver.loaders?.contains(loader)!! && ver.game_versions?.contains(version)!!) {
                return ver
            }
        }
        return null

    }

    fun getVersions(project: String): List<ProjectVersion>? {
        val versions_url = URL("https://api.modrinth.com/v2/project/$project/version")
        val connection = versions_url.openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "GET"
            connection.connect()
            println("version: ${connection.responseCode} |")
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.use { inputStream ->
                    InputStreamReader(inputStream).use { reader ->
                        val gson = Gson()
                        val listType = object : TypeToken<List<ProjectVersion>>() {}.type
                        gson.fromJson<List<ProjectVersion>>(reader, listType)
                    }
                }
            } else {
                println("Error: ${connection.responseCode}")
                null
            }
        } catch (e: Exception) {
        e.printStackTrace()
        null
    } finally {
        connection.disconnect()
    }
    }


}
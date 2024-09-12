package org.magnariuk.modpackupdater.data.api

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javafx.beans.binding.Bindings.isNotEmpty
import org.magnariuk.modpackupdater.data.classes.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.file.Files.copy


class ModrinthAPI {


    fun isProjectHere(projects_list: List<ProfileProject>, needed_project: String): Boolean {
        val i = projects_list.iterator()
        while (i.hasNext()) {
            val n = i.next()
            if(n.metadata.project.id == needed_project) {
                return true
            }
        }
        return false
    }



    fun search(search_data: String): SearchResults? {
        val facets = URLEncoder.encode("[[\"project_type:mod\"]]", "UTF-8");

        val versions_url = URL("https://api.modrinth.com/v2/search?query=${search_data}&facets=$facets")
        val connection = versions_url.openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "GET"
            connection.connect()
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.use { inputStream ->
                    InputStreamReader(inputStream).use { reader ->
                        BufferedReader(reader).use { bufferedReader ->
                            val jsonText = bufferedReader.readText()
                            println(jsonText)

                            val gson = Gson()
                            gson.fromJson<SearchResults>(jsonText, SearchResults::class.java)
                        }
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

    fun parseInput(input: String): List<String> {
        return if (input.contains("name=")) {
            extractNamesFromMap(input)
        } else {
            extractNamesFromList(input)
        }
    }

    fun extractNamesFromMap(input: String): List<String> {
        val regex = Regex("""name=([^\s,]+)""")
        return regex.findAll(input).map { it.groupValues[1] }.toList()
    }

    fun extractNamesFromList(input: String): List<String> {
        return input.removeSurrounding("[", "]")
            .split(", ")
            .map { it.trim() }
    }


    fun searchProject(data: FabricMod): SearchProject? {
        if(data.id != null){
            val search_result = search(data.id)
            println("Trying search by id: ${data.id}")
            val project_list_iterator = search_result?.hits?.iterator()
            if(data.authors != null){
                val authors = parseInput(data.authors.toString())
                println("Project's authors: $authors")

                while (project_list_iterator?.hasNext()!!) {
                    val project = project_list_iterator.next()
                    println("Found project: ${project.title} - ${project.project_id}\nBy ${project.author}")

                    if(containsIgnoreCase(authors, project.author)){
                        return project
                    }
            }


            }

        } else if(data.name != null){
            val search_result = search(data.name)
        }

        return null
    }

    fun containsIgnoreCase(list: List<String>, target: String): Boolean {
        return list.any { it.equals(target, ignoreCase = true) }
    }

    fun downloadF(project: SearchProject, version: String, loader: String, resultFolder: File, useOnlyStableVersion: Boolean = false) {
        val all_versions: List<ProjectVersion>? = getVersions(project.project_id)
        if(all_versions != null) {
            val latest_version = filter_versions(all_versions, version, loader, useOnlyStableVersion)
            if (latest_version != null) {

                val title = project.title
                println("Found version ${latest_version.version_number}")
                downloadFile(latest_version, resultFolder)

                if(!latest_version.dependencies!!.isNotEmpty()) {
                    println("Checking dependencies")
                    val dependecyIterator =  latest_version.dependencies.iterator()
                    while (dependecyIterator.hasNext()) {
                        val dependency = dependecyIterator.next()
                        val latest_dependency_version =
                            getVersions(dependency.project_id)?.let { filter_versions(it, version, loader, useOnlyStableVersion) }
                        println("Found dependency: ${latest_dependency_version?.name}")
                        if(latest_dependency_version != null){
                            println("Downloading dependency")
                            downloadFile(latest_dependency_version, resultFolder)
                        }

                    }
                    println()
                }

            } else{
                println("Was not found on $loader for $version version")
            }
        } else{
            println("Versions not found: ${project.title} | $version | $loader \n Unexcpected result")

        }
        println("____________________________________________")

    }


    fun download(project: ProfileProject, version: String, loader: String, resultFolder: File, projects_list: List<ProfileProject>, useOnlyStableVersion: Boolean = false) {
        val all_versions: List<ProjectVersion>? = getVersions(project.metadata.project.id)
        if(all_versions != null) {
            val latest_version = filter_versions(all_versions, version, loader, useOnlyStableVersion)
            if (latest_version != null) {

                val title = project.metadata.project.title
                println("Found version ${latest_version.version_number}")
                downloadFile(latest_version, resultFolder)

                if(!latest_version.dependencies!!.isNotEmpty()) {
                    println("Checking dependencies")
                    val dependecyIterator =  latest_version.dependencies.iterator()
                    while (dependecyIterator.hasNext()) {
                        val dependency = dependecyIterator.next()
                        val latest_dependency_version =
                            getVersions(dependency.project_id)?.let { filter_versions(it, version, loader, useOnlyStableVersion) }
                        println("Found dependency: ${latest_dependency_version?.name}")
                        if (!isProjectHere(projects_list, dependency.project_id)){
                            if(latest_dependency_version != null){
                                println("Downloading dependency")
                                downloadFile(latest_dependency_version, resultFolder)
                            }
                        } else{
                            println("Dependency is already in mods list")
                        }
                    }
                    println()
                }

            } else{
                println("Was not found on $loader for $version version")
            }
        } else{
            println("Versions not found: ${project.metadata.project.title} | $version | $loader \n Unexcpected result")

        }
        println("____________________________________________")

    }

    fun downloadFile(p_file: ProjectVersion, outputDir: File){
        println("Reaching for the file")
        val url = URL(p_file.files?.get(0)?.url)
        val connection = url.openConnection() as HttpURLConnection
        try{
            connection.requestMethod = "GET"
            connection.connect()

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
                var fileSizeInKB = 0.0
                if (file != null) {
                    fileSizeInKB = Math.round(file.length().toDouble().div(1024)*100)/100.toDouble()
                }
                println("File downloaded successfully: ${file?.absolutePath} | ${fileSizeInKB} kb\n")
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





    fun filter_versions(versions: List<ProjectVersion>, version: String, loader: String, shouldBeStable: Boolean): ProjectVersion?{
        val iterator = versions.iterator()
        val vers: MutableList<ProjectVersion> = mutableListOf<ProjectVersion>()
        while (iterator.hasNext()) {
            val ver = iterator.next()
            if(shouldBeStable){
                if(ver.loaders?.contains(loader)!! && ver.game_versions?.contains(version)!! && ver.version_type == "release") {
                    return ver
                }
            } else{
                if(ver.loaders?.contains(loader)!! && ver.game_versions?.contains(version)!!) {
                    return ver
                }
            }

        }
        return null

    }

    fun getVersions(project: String): List<ProjectVersion>? {
        println("Reaching to Modrinth for versions")
        val versions_url = URL("https://api.modrinth.com/v2/project/$project/version")
        val connection = versions_url.openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "GET"
            connection.connect()
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
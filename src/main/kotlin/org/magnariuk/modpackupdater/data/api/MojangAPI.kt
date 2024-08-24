package org.magnariuk.modpackupdater.data.api
import com.google.gson.Gson
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.magnariuk.modpackupdater.data.classes.Cache
import org.magnariuk.modpackupdater.data.classes.VersionManifest
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL


class MojangAPI {

    fun getVersionsList(versionManifest: VersionManifest, showSnapshots: Boolean = false): ObservableList<String> {
        val releases = FXCollections.observableArrayList<String>()
        if (!showSnapshots){

            val iterator = versionManifest.versions.iterator()

            while (iterator.hasNext()){
                val version = iterator.next()
                if(version.type == "release"){
                    releases.add(version.id)
                }
            }
        } else{
            val iterator = versionManifest.versions.iterator()

            while (iterator.hasNext()){
                val version = iterator.next()
                if(version.type == "release" || version.type == "snapshot"){
                    releases.add(version.id)
                }
            }
        }
        return releases
    }



    fun getVersions(): VersionManifest?{
        val url = URL("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json")
        val connection = url.openConnection() as HttpURLConnection

        return try {
            connection.requestMethod = "GET"
            connection.connect()

            // Перевірка коду відповіді
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.use { inputStream ->
                    inputStream.reader().use { reader ->
                        val gson = Gson()
                        gson.fromJson(reader, VersionManifest::class.java)
                    }
                }
            } else {
                // Обробка ситуації, коли запит не успішний
                println("Error: ${connection.responseCode}")
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            // Завжди закривайте з'єднання
            connection.disconnect()
        }
    }
}
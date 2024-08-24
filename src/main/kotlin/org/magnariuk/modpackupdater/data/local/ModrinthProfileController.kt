package org.magnariuk.modpackupdater.data.local

import com.google.gson.Gson
import org.magnariuk.modpackupdater.data.classes.Cache
import org.magnariuk.modpackupdater.data.classes.ModrinthProfile
import java.io.FileReader
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

class ModrinthProfileController {
    fun getProfile(profile_json_path: Path): ModrinthProfile? {
        if (Files.exists(profile_json_path)) {
            val gson = Gson()
            return try {
                FileReader(profile_json_path.toFile()).use { reader ->
                    gson.fromJson(reader, ModrinthProfile::class.java)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }
        } else{
            return null
        }
    }

}
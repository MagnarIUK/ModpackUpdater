package org.magnariuk.modpackupdater.data.local

import com.google.gson.Gson
import org.magnariuk.modpackupdater.data.classes.Cache
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class CacheController {
    private val cachePath: Path = Paths.get(System.getProperty("user.dir"), "cache.json")

    fun saveCache(cache: Cache){
        val gson = Gson()
        try {
            FileWriter(cachePath.toFile()).use { writer ->
                var data = gson.toJson(cache)

                writer.write(data.toString())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    fun getCache(): Cache? {
        if (Files.exists(cachePath)) {
            val gson = Gson()
            return try {
                FileReader(cachePath.toFile()).use { reader ->
                    gson.fromJson(reader, Cache::class.java)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }
        } else{
            val cache = Cache()
            saveCache(cache)
            return cache

        }

    }


}
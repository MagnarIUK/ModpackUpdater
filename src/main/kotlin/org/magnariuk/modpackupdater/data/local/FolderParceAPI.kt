package org.magnariuk.modpackupdater.data.local

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import org.magnariuk.modpackupdater.data.classes.Cache
import org.magnariuk.modpackupdater.data.classes.FabricMod
import java.io.*
import java.nio.file.Files
import java.util.jar.JarFile

class FolderParceAPI {

    fun process(directory: File): List<FabricMod>?{

        if (directory.isDirectory){

            val files = getFilesByExtension(directory)
            val it = files.iterator()
            val mods = mutableListOf<FabricMod>()
            while(it.hasNext()){
                val file = it.next()
                val fmjs = getFabricModJsonStream(file)
                val pfmj = fmjs?.let { parseFabricModJson(it) }
                pfmj?.let {mods.add(it) }




            }
            return mods
        }
        return null

    }

    fun getFilesByExtension(directory: File): List<File> {
        return directory.walkTopDown()
            .filter { it.isFile && it.extension == "jar" }
            .toList()
    }


        fun getFabricModJsonStream(jarFile: File): ByteArray? {
            JarFile(jarFile).use { jar ->
                val entry = jar.getJarEntry("fabric.mod.json")
                return if (entry != null) {
                    jar.getInputStream(entry).readBytes()
                } else {
                    null
                }
            }
        }

    fun parseFabricModJson(fabricModJson: ByteArray): FabricMod? {
        val gson = Gson()
        return try {
            // Створюємо новий InputStream з масиву байтів
            ByteArrayInputStream(fabricModJson).use { newStream ->
                val jsonContent = newStream.bufferedReader().use { it.readText() }
                gson.fromJson(jsonContent, FabricMod::class.java)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            null
        }
    }
    /*fun parseFabricModJson(fabricModJson: InputStream): FabricMod? {
            val gson = Gson()
            return try {
                fabricModJson.reader().use { reader ->
                    gson.fromJson(reader, FabricMod::class.java)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }
    }*/

}
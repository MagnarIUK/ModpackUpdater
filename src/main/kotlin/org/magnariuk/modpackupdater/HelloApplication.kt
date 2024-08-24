package org.magnariuk.modpackupdater

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Background
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.Stage
import org.magnariuk.modpackupdater.data.api.ModrinthAPI
import org.magnariuk.modpackupdater.data.api.MojangAPI
import org.magnariuk.modpackupdater.data.classes.ProfileProject
import org.magnariuk.modpackupdater.data.local.CacheController
import org.magnariuk.modpackupdater.data.local.ModrinthProfileController

import java.awt.Desktop
import java.io.*
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*

class HelloApplication : Application() {

    private val logFile = File(generateLogFileName()).apply {
        parentFile?.mkdirs()
    }




    private fun generateLogFileName(): String {
        val dateFormat = SimpleDateFormat("HH_mm_ss_dd_MM_yy")
        val dateStr = dateFormat.format(Date())
        return "logs/log_$dateStr.txt"
    }


    companion object {
        const val VERSION = "0.0.1 beta"
    }

    fun showDonePopup(count: Int) {
        val popupStage = Stage()
        popupStage.initModality(Modality.APPLICATION_MODAL)
        popupStage.title = "Done"

        val messageLabel = Label("$count mods were processed").apply {
            isWrapText = true
            maxWidth = 280.0
            alignment = Pos.CENTER
        }
        val closeButton = Button("Close").apply {
            setOnAction { popupStage.close() }
            alignment = Pos.CENTER
        }
        /*val logButton = Button("Open Logs").apply {
            setOnAction { Desktop.getDesktop().open(logFile) }
            alignment = Pos.CENTER
        }*/

        val buttonsLayout = HBox(10.0, closeButton).apply {
            alignment = Pos.CENTER
        }

        val layout = VBox(10.0, messageLabel, buttonsLayout).apply {
            alignment = Pos.CENTER
            VBox.setMargin(closeButton, Insets(10.0, 0.0, 10.0, 0.0))
        }

        val popupScene = Scene(layout, 300.0, 100.0)
        popupStage.scene = popupScene
        popupStage.show()
    }

    fun showPopup(title: String, message: String) {
        val popupStage = Stage()
        popupStage.initModality(Modality.APPLICATION_MODAL)
        popupStage.title = title

        val messageLabel = Label(message).apply {
            isWrapText = true
            maxWidth = 280.0
            alignment = Pos.CENTER
        }
        val closeButton = Button("Close").apply {
            setOnAction { popupStage.close() }
            alignment = Pos.CENTER
        }
        val layout = VBox(10.0, messageLabel, closeButton).apply {
            alignment = Pos.CENTER
            VBox.setMargin(closeButton, Insets(10.0, 0.0, 10.0, 0.0))
        }

        val popupScene = Scene(layout, 300.0, 100.0)
        popupStage.scene = popupScene
        popupStage.show()
    }


    class RedirectOutputStream(private val list: ListView<String>) : OutputStream() {
        override fun write(b: Int) {
            // Додаємо текст до TextArea
            list.items.add(b.toString())
        }
    }



    override fun start(stage: Stage) {

        /*val fxmlLoader = FXMLLoader(HelloApplication::class.java.getResource("hello-view.fxml"))
        val scene = Scene(fxmlLoader.load(), 320.0, 240.0)
        stage.title = "Hello!"
        stage.scene = scene
        stage.show()*/
        val cacheController = CacheController()
        val mojangAPI = MojangAPI()
        val modrinthAPI = ModrinthAPI()

        val tabPane = TabPane().apply {
            background = Background.EMPTY
        }

        val updaterAnchorPane = AnchorPane()

        val modrinthProfileDirectoryLabel = Label("Select Modrinth Profile Folder:")
        val modrinthProfileDirectoryPathField = TextField().apply {
            prefWidth = 300.0
        }


        val browseModrinthProfileJson = Button("Browse").apply {
            setOnAction {
                val fileChooser = FileChooser().apply {
                    title = "Select Modrinth \"profile.json\""
                    extensionFilters.add(FileChooser.ExtensionFilter("Modrinth Profile Json", "*.json"))
                    val initialDir = File(modrinthProfileDirectoryPathField.text).parentFile
                    initialDirectory = if (initialDir != null && initialDir.exists()) initialDir else File(System.getProperty("user.home"))
                }
                val selectedDirectory = fileChooser.showOpenDialog(null)
                selectedDirectory?.let {
                    modrinthProfileDirectoryPathField.text = it.absolutePath
                    val cache = cacheController.getCache()
                    if (cache != null) {
                        cache.modrinthFolderPath = it.absolutePath
                        cacheController.saveCache(cache)
                    }
                }
            }
        }
        val openModrinthProfileDirectory = Button("Open").apply {
            setOnAction {
                val file = File(modrinthProfileDirectoryPathField.text)
                if (file.exists()) {
                    try {
                        Desktop.getDesktop().open(file)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } else {
                    showPopup("Error", "Please select a valid Modrinth Profile Folder")
                }
            }
        }

        val folderHBox = HBox(10.0, modrinthProfileDirectoryPathField, browseModrinthProfileJson, openModrinthProfileDirectory)

        val resultFolderLabel = Label("Select Result Folder:")
        val resultFolderPathField = TextField().apply {
            prefWidth = 300.0
        }


        val browseResultFolderButton = Button("Browse").apply {
            setOnAction {
                val directoryChooser = DirectoryChooser().apply {
                    title = "Select Result Folder"
                    val initialDir = File(resultFolderPathField.text)
                    initialDirectory = if (initialDir.exists() && initialDir.isDirectory) initialDir else File(System.getProperty("user.home"))

                }
                val selectedDirectory = directoryChooser.showDialog(null)
                selectedDirectory?.let {
                    resultFolderPathField.text = it.absolutePath
                    val cache = cacheController.getCache()
                    if (cache != null) {
                        cache.resultFolderPath = it.absolutePath
                        cacheController.saveCache(cache)
                    }
                }
            }
        }

        val openResultFolderButton = Button("Open").apply {
            setOnAction {
                val folder = File(resultFolderPathField.text)
                if (folder.exists() && folder.isDirectory) {
                    try {
                        Desktop.getDesktop().open(folder)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } else {
                    showPopup("Error", "Please select a valid Folder")
                }
            }
        }





        val resultFolderHBox = HBox(10.0, resultFolderPathField, browseResultFolderButton, openResultFolderButton)

        val versionComboBox = ComboBox<String>().apply {
            promptText = "Version"
            prefWidth = 150.0

        }


        val vrs = mojangAPI.getVersions()

        val showSnapshotsCheckBox = CheckBox("Show snapshots").apply {
            setOnAction {
                if(isSelected){
                    versionComboBox.items = vrs?.let { mojangAPI.getVersionsList(it, true) }
                    versionComboBox.selectionModel.clearSelection()
                    versionComboBox.promptText = "Version"





                } else if(!isSelected){
                    versionComboBox.items = vrs?.let { mojangAPI.getVersionsList(it, false) }
                    versionComboBox.selectionModel.clearSelection()
                    versionComboBox.promptText = "Version"




                }
            }
        }

        val defaultCache = cacheController.getCache()
        if (defaultCache != null) {
            modrinthProfileDirectoryPathField.text = defaultCache.modrinthFolderPath
            resultFolderPathField.text = defaultCache.resultFolderPath
            showSnapshotsCheckBox.isSelected = defaultCache.showSnapshots
        }

        if (showSnapshotsCheckBox.isSelected) {
            versionComboBox.items = vrs?.let { mojangAPI.getVersionsList(it, true) }
        } else{
            versionComboBox.items = vrs?.let { mojangAPI.getVersionsList(it, false) }

        }




        val versionHBox = HBox(10.0, versionComboBox, showSnapshotsCheckBox)

        val updateButton = Button("Update").apply {
            maxWidth = Double.MAX_VALUE
            setOnAction {

                if(File(modrinthProfileDirectoryPathField.text).exists() && File(resultFolderPathField.text).exists() && versionComboBox.selectionModel.selectedItem != null) {
                    val startTime = System.nanoTime()
                    val modrinthProfileController = ModrinthProfileController()
                    val profile = modrinthProfileController.getProfile(Paths.get(modrinthProfileDirectoryPathField.text))?.projects
                    val profile_files = profile?.values?.toList()?.iterator()
                    var to_download: MutableList<ProfileProject> = mutableListOf()
                    while (profile_files?.hasNext()!!) {
                        val p = profile_files.next()
                        if(p.metadata.type == "modrinth" && p.metadata.project.project_type == "mod"){
                            to_download.add(p)
                        }
                    }
                    modrinthAPI.download(to_download.get(0), versionComboBox.selectionModel.selectedItem.toString(), "fabric", File(resultFolderPathField.text))

                    val all = to_download.count()
                    var count: Int = 1
                    val d_iterator = to_download.iterator()
                    while (d_iterator.hasNext()) {
                        val p = d_iterator.next()
                        println("[$count/$all]")
                        modrinthAPI.download(p, versionComboBox.selectionModel.selectedItem.toString(), "fabric", File(resultFolderPathField.text))
                        count++

                    }

                    val endTime = System.nanoTime()
                    val duration = endTime - startTime
                    val durationInSeconds = duration / 1_000_000_000.0
                    println("Time taken: $durationInSeconds seconds")
                    println("____________________________\n\n")
                    showDonePopup(count)
                } else{
                    showPopup("ERROR", "Check all selections")
                }




            }
        }
        val mainUpdaterMenuVbox = VBox(10.0, modrinthProfileDirectoryLabel, folderHBox, resultFolderLabel, resultFolderHBox, versionHBox, updateButton).apply {
            alignment = Pos.CENTER
            padding = Insets(10.0)
        }


        val logListView = ListView<String>().apply {
            setPrefSize(200.0, 200.0) // Ширина і висота ListView
        }




        val logUpdaterMenuVBox = VBox(10.0)


        val updaterMenuHBox = HBox(10.0, mainUpdaterMenuVbox, logUpdaterMenuVBox)


        val updaterVBox = VBox(10.0, updaterMenuHBox).apply {
            alignment = Pos.CENTER
            padding = Insets(10.0)
        }

        updaterAnchorPane.children.add(updaterVBox)

        val updaterTab = Tab("Updater", updaterAnchorPane).apply {
            isClosable = false
        }

        val settingsAnchorPane = AnchorPane()
        val settingsTab = Tab("Settings", settingsAnchorPane).apply {
            isClosable = false
        }

        tabPane.tabs.addAll(updaterTab, settingsTab)

        val gitHubLink = Hyperlink("GitHub").apply {
            setOnAction { hostServices.showDocument("https://github.com/MagnarIUK/ModpackUpdater") }
        }

        val telegramLink = Hyperlink("Telegram").apply {
            setOnAction { hostServices.showDocument("https://t.me/magnar_hnt") }
        }

        val versionText = Text(VERSION)

        val infoTextFlow = TextFlow(
            gitHubLink,
            Text(" | "),
            telegramLink,
            versionText
        )

        val affiliationLabel = Label("Not affiliated with Mojang or Microsoft")

        val infoVBox = VBox(5.0, infoTextFlow, affiliationLabel).apply {
            alignment = Pos.BOTTOM_LEFT
            padding = Insets(10.0)
        }

        val mainVBox = VBox(10.0, tabPane, infoVBox).apply {
            padding = Insets(10.0)
        }

        val scene = Scene(mainVBox, 800.0, 400.0)

        stage.scene = scene
        stage.title = "Modpack Updater"
        stage.show()

    }
}

private class LogOutputStream(private val file: File) : OutputStream() {
    private val writer = FileWriter(file, true)

    override fun write(b: Int) {
        writer.write(b)
    }

    override fun flush() {
        writer.flush()
    }

    override fun close() {
        writer.close()
    }


}



fun main() {
    Application.launch(HelloApplication::class.java)
}
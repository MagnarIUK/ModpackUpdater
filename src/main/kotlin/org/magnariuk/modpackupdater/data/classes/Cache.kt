package org.magnariuk.modpackupdater.data.classes


data class Cache(
    var modrinthFolderPath: String = "",
    var resultFolderPath: String = "",
    var showSnapshots: Boolean = false,
    var Settings: Settings = Settings(),

    )

data class Settings(
    var useOnlyStableVersions: Boolean = false,
)
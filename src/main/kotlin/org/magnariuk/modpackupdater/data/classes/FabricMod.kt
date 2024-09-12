package org.magnariuk.modpackupdater.data.classes

data class FabricMod(
    val schemaVersion: Int,
    val id: String?,
    val name: String?,
    val version: String,
    val environment: String,
    val license: String,
    val icon: String,
    val authors: Any,
)
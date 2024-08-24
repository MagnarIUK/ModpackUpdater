package org.magnariuk.modpackupdater.data.classes

data class VersionManifest(
    val latest: Latest,
    val versions: List<Version>
)

data class Latest(
    val release: String,
    val snapshot: String,
)
data class Version(
    val id: String,
    val type: String,
    val url: String,
    val time: String,
    val releaseTime: String,
    val sha1: String,
    val complianceLevel: Int,
)
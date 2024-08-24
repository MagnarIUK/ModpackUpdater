package org.magnariuk.modpackupdater.data.classes

import java.io.File

data class ModrinthProfile(
    val uuid: String,
    val install_stage: String,
    val path: String,
    val metadata: ProfileMetadata,
    val java: Java?,
    val fullscreen: Any?,
    val projects: Map<String, ProfileProject>,
    val modrinth_update_version: Any?,

)

data class ProfileProject(
    val sha512: String,
    val disabled: Boolean,
    val metadata: ProjectMetadata,
    val filename: String,
)

data class ProjectMetadata(
    val type: String,
    val project: Project,
    val title: String?,
    val members: List<Member>,
    val project_type: String,
    val update_version: Any?,
    val incompatible: Boolean,
    )

data class ProjectVersion(
    val id: String?,
    val project_id: String?,
    val author_id: String?,
    val featured: Boolean?,
    val name: String?,
    val version_number: String?,
    val changelog: String?,
    val changelog_url: String?,
    val date_published: String?,
    val downloads: Int?,
    val version_type: String?,
    val status: String?,
    val requested_status: String?,
    val files: List<ModFile>?,
    val dependencies: List<ModDependency>?,
    val game_versions: List<String>?,
    val loaders: List<String>?,
)

data class ModDependency (
    val version_id: String,
    val project_id: String,
    val file_name: String?,
    val dependency_type: String,
)

data class ModFile(
    val hashes: HashMap<String, String>,
    val url: String,
    val filename: String,
    val primary: Boolean,
    val size: Int,
    val file_type: Any?,
)

data class Project(
    val id: String,
    val slug: String,
    val project_type: String,
    val team: String,
    val title: String,
    val description: String,
    val body: String,
    val published: String,
    val updated: String,
    val client_side: String,
    val server_side: String,
    val downloads: Int,
    val followers: Int,
    val categories: List<String>,
    val additional_categories: List<String>,
    val game_versions: List<String>,
    val loaders: List<String>,
    val versions: List<String>,
    val icon_url: String,
)


data class Member(
    val team_id: String,
    val user: User,
    val role: String,
    val ordering: Int,

)
data class User(
    val id: String,
    val username: String,
    val name: String,
    val avatar_url: String,
    val bio: Any?,
    val created: String,
    val role: String,
)
data class ProfileMetadata(
    val name: String,
    val icon: String,
    val groups: List<String>,
    val game_version: String,
    val loader: String,
    val loader_version: LoaderVersion,
    val date_created: String,
    val date_modified: String,
    val last_played: String,
    val submitted_time_played: Int,
    val recent_time_played: Int,
)

data class Java(
    val name: String?,
)
data class LoaderVersion(
    val id: String,
    val url: String,
    val stable: Boolean,
)
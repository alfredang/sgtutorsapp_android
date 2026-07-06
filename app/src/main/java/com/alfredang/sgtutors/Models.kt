package com.alfredang.sgtutors

import kotlinx.serialization.Serializable

// Mirrors the server's PUBLIC tutor shape (server/src/services/sanitize.ts).
// Only whitelisted, non-sensitive fields ever reach this app.

@Serializable
data class SubjectTag(
    val id: Int,
    val name: String,
    val slug: String,
)

@Serializable
data class PublicTutor(
    val id: String,
    val displayName: String,
    val gender: String,
    val race: String,
    val nationality: String,
    val region: String,
    val linkedinUrl: String? = null,
    val photoUrl: String? = null,
    val subjects: List<SubjectTag> = emptyList(),
    val levels: List<SubjectTag> = emptyList(),
    val highestQualification: String,
    val education: String,
    val profileText: String,
    val studentsTaught: Int,
    val experienceYears: Int,
    val avgRating: Double? = null,
    val reviewCount: Int,
    val isVerified: Boolean,
    val isFeatured: Boolean,
) {
    val regionLabel: String
        get() = REGION_LABELS[region] ?: region.replaceFirstChar(Char::uppercase)

    val genderLabel: String
        get() = gender.replaceFirstChar(Char::uppercase)

    val initials: String
        get() = displayName.split(" ").filter { it.isNotBlank() }
            .take(2).map { it.first().uppercase() }.joinToString("")
}

@Serializable
data class Paged<T>(
    val items: List<T>,
    val page: Int,
    val pageSize: Int,
    val total: Int,
)

@Serializable
data class Review(
    val id: String,
    val reviewerName: String,
    val rating: Int,
    val comment: String,
    val createdAt: String,
)

@Serializable
data class Subject(
    val id: Int,
    val name: String,
    val slug: String,
    val category: String,
)

@Serializable
data class Level(
    val id: Int,
    val name: String,
    val slug: String,
    val sortOrder: Int,
)

@Serializable
data class EnquiryInput(
    val name: String,
    val email: String,
    val phone: String,
    val message: String,
    val turnstileToken: String,
)

val REGION_LABELS = linkedMapOf(
    "north" to "North",
    "north_east" to "North-East",
    "north_west" to "North-West",
    "east" to "East",
    "west" to "West",
    "central" to "Central",
)

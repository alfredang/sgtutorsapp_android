package com.alfredang.sgtutors.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import coil.compose.SubcomposeAsyncImage
import com.alfredang.sgtutors.Level
import com.alfredang.sgtutors.PublicTutor
import com.alfredang.sgtutors.REGION_LABELS
import com.alfredang.sgtutors.Subject
import com.alfredang.sgtutors.TutorSearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorsHomeScreen(
    model: TutorSearchViewModel,
    onTutorClick: (PublicTutor) -> Unit,
) {
    LaunchedEffect(Unit) { model.initialLoad() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SG Tutors", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                OutlinedTextField(
                    value = model.query,
                    onValueChange = model::setQueryText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    placeholder = { Text("Search name, subject or keyword") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
            }

            item { FilterBar(model) }

            if (model.featured.isNotEmpty() && !model.hasFilters) {
                item {
                    Row(
                        Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.Star, contentDescription = null,
                            tint = Brand.Featured, modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Featured Tutors",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(model.featured.size) { i ->
                            FeaturedTutorCard(model.featured[i]) { onTutorClick(model.featured[i]) }
                        }
                    }
                }
            }

            item {
                Text(
                    text = if (model.total > 0) {
                        "${model.total} tutor" + (if (model.total == 1) "" else "s")
                    } else "",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
                )
            }

            val error = model.loadError
            if (error != null) {
                item {
                    StatusMessage(
                        title = "Couldn't load tutors",
                        detail = error,
                        actionLabel = "Retry",
                        onAction = model::retry,
                    )
                }
            } else if (model.tutors.isEmpty() && !model.isLoading) {
                item {
                    StatusMessage(
                        title = "No tutors found",
                        detail = "Try removing a filter or using a different keyword.",
                    )
                }
            } else {
                items(model.tutors.size) { i ->
                    val tutor = model.tutors[i]
                    TutorRow(tutor) { onTutorClick(tutor) }
                    if (i < model.tutors.lastIndex) {
                        HorizontalDivider(Modifier.padding(start = 84.dp))
                    }
                    if (i == model.tutors.lastIndex && model.canLoadMore) {
                        LaunchedEffect(model.tutors.size) { model.loadMore() }
                    }
                }
                if (model.isLoading) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterBar(model: TutorSearchViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterDropdown(
            title = model.subject?.name ?: "Subject",
            isActive = model.subject != null,
            options = listOf<Pair<String, Subject?>>("All Subjects" to null) +
                model.subjects.sortedBy { it.category }.map { "${it.name} (${it.category})" to it },
            onSelect = model::setSubjectFilter,
        )
        FilterDropdown(
            title = model.level?.name ?: "Level",
            isActive = model.level != null,
            options = listOf<Pair<String, Level?>>("All Levels" to null) +
                model.levels.map { it.name to it },
            onSelect = model::setLevelFilter,
        )
        FilterDropdown(
            title = REGION_LABELS[model.region] ?: "Region",
            isActive = model.region != null,
            options = listOf<Pair<String, String?>>("All Regions" to null) +
                REGION_LABELS.map { (slug, label) -> label to slug },
            onSelect = model::setRegionFilter,
        )
        FilterDropdown(
            title = model.gender?.replaceFirstChar(Char::uppercase) ?: "Gender",
            isActive = model.gender != null,
            options = listOf("Any Gender" to null, "Female" to "female", "Male" to "male"),
            onSelect = model::setGenderFilter,
        )
    }
}

@Composable
private fun <T> FilterDropdown(
    title: String,
    isActive: Boolean,
    options: List<Pair<String, T?>>,
    onSelect: (T?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        FilterChip(
            selected = isActive,
            onClick = { expanded = true },
            label = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (label, value) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        expanded = false
                        onSelect(value)
                    },
                )
            }
        }
    }
}

@Composable
fun TutorAvatar(tutor: PublicTutor, size: Int) {
    val placeholder: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                tutor.initials,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = (size * 0.38).sp,
            )
        }
    }
    if (tutor.photoUrl.isNullOrBlank()) {
        placeholder()
    } else {
        SubcomposeAsyncImage(
            model = tutor.photoUrl,
            contentDescription = tutor.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(size.dp).clip(CircleShape),
            loading = { placeholder() },
            error = { placeholder() },
        )
    }
}

@Composable
fun TutorRow(tutor: PublicTutor, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TutorAvatar(tutor, 56)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    tutor.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (tutor.isVerified) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.Filled.Verified,
                        contentDescription = "Verified tutor",
                        tint = Brand.Verified,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Text(
                tutor.subjects.take(3).joinToString(" · ") { it.name },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (tutor.avgRating != null) {
                    Icon(
                        Icons.Filled.Star, contentDescription = null,
                        tint = Brand.Star, modifier = Modifier.size(13.dp),
                    )
                    Text(
                        " %.1f   ".format(tutor.avgRating),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    Icons.Filled.Work, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(13.dp),
                )
                Text(
                    " ${tutor.experienceYears} yr   ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Icon(
                    Icons.Filled.Place, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(13.dp),
                )
                Text(
                    " ${tutor.regionLabel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FeaturedTutorCard(tutor: PublicTutor, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.width(150.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TutorAvatar(tutor, 44)
                Icon(
                    Icons.Filled.Star,
                    contentDescription = "Featured",
                    tint = Brand.Featured,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                tutor.displayName,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                tutor.subjects.take(2).joinToString(" · ") { it.name },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (tutor.avgRating != null) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Star, contentDescription = null,
                        tint = Brand.Star, modifier = Modifier.size(12.dp),
                    )
                    Text(
                        " %.1f (%d)".format(tutor.avgRating, tutor.reviewCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
fun StatusMessage(
    title: String,
    detail: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(
            detail,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(12.dp))
            androidx.compose.material3.Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}

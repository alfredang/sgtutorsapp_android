package com.alfredang.sgtutors.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.alfredang.sgtutors.ApiClient
import com.alfredang.sgtutors.PublicTutor
import com.alfredang.sgtutors.Review
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorDetailScreen(
    tutorId: String,
    placeholder: PublicTutor?,
    onBack: () -> Unit,
) {
    var tutor by remember { mutableStateOf<PublicTutor?>(null) }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var reviewTotal by remember { mutableIntStateOf(0) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var showEnquiry by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(tutorId, reloadKey) {
        loadError = null
        try {
            tutor = ApiClient.tutor(tutorId)
            val page = ApiClient.reviews(tutorId)
            reviews = page.items
            reviewTotal = page.total
        } catch (e: Exception) {
            if (tutor == null && placeholder == null) {
                loadError = e.message ?: "Something went wrong"
            }
        }
    }

    val shown = tutor ?: placeholder

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tutor Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            if (shown != null) {
                Surface(shadowElevation = 8.dp) {
                    Button(
                        onClick = { showEnquiry = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Icon(Icons.Filled.Email, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Send Enquiry", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when {
            shown != null -> Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Header(shown)
                StatsRow(shown)
                TagsCard("Subjects", shown.subjects.map { it.name })
                TagsCard("Levels", shown.levels.map { it.name })
                AboutCard(shown)
                if (reviews.isNotEmpty()) ReviewsCard(reviews, reviewTotal)
                Spacer(Modifier.height(8.dp))
            }

            loadError != null -> StatusMessage(
                title = "Couldn't load tutor",
                detail = loadError!!,
                actionLabel = "Retry",
                onAction = { reloadKey += 1 },
            )

            else -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }

    if (showEnquiry && shown != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showEnquiry = false },
            sheetState = sheetState,
        ) {
            EnquirySheet(tutor = shown, onDone = { showEnquiry = false })
        }
    }
}

@Composable
private fun Header(tutor: PublicTutor) {
    val uri = LocalUriHandler.current
    Column(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TutorAvatar(tutor, 96)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                tutor.displayName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            if (tutor.isVerified) {
                Spacer(Modifier.width(6.dp))
                Icon(
                    Icons.Filled.Verified,
                    contentDescription = "Verified tutor",
                    tint = Brand.Verified,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (tutor.isFeatured) Badge("Featured", Brand.Featured)
            if (tutor.isVerified) Badge("Verified", Brand.Verified)
        }
        val rating = tutor.avgRating
        if (rating != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RatingStars(rating.roundToInt(), size = 18)
                Spacer(Modifier.width(6.dp))
                Text(
                    "%.1f · %d review%s".format(
                        rating, tutor.reviewCount, if (tutor.reviewCount == 1) "" else "s",
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        val linkedin = tutor.linkedinUrl
        if (!linkedin.isNullOrBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { uri.openUri(linkedin) },
            ) {
                Icon(
                    Icons.Filled.Link, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "LinkedIn Profile",
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
fun RatingStars(filled: Int, size: Int, max: Int = 5) {
    Row {
        (1..max).forEach { i ->
            Icon(
                if (i <= filled) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = null,
                tint = Brand.Star,
                modifier = Modifier.size(size.dp),
            )
        }
    }
}

@Composable
private fun Badge(text: String, color: androidx.compose.ui.graphics.Color) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = color,
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), CircleShape)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    )
}

@Composable
private fun StatsRow(tutor: PublicTutor) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Stat(Modifier.weight(1f), "${tutor.experienceYears}", "Years Exp.") {
            Icon(Icons.Filled.Work, null, tint = MaterialTheme.colorScheme.primary, modifier = it)
        }
        Stat(Modifier.weight(1f), "${tutor.studentsTaught}", "Students") {
            Icon(Icons.Filled.Groups, null, tint = MaterialTheme.colorScheme.primary, modifier = it)
        }
        Stat(Modifier.weight(1f), tutor.regionLabel, "Region") {
            Icon(Icons.Filled.Place, null, tint = MaterialTheme.colorScheme.primary, modifier = it)
        }
    }
}

@Composable
private fun Stat(
    modifier: Modifier,
    value: String,
    label: String,
    icon: @Composable (Modifier) -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            icon(Modifier.size(20.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsCard(title: String, tags: List<String>) {
    SectionCard(title) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            tags.forEach { tag ->
                Text(
                    tag,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                            CircleShape,
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun AboutCard(tutor: PublicTutor) {
    SectionCard("About") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            InfoRow("Qualification", tutor.highestQualification)
            InfoRow("Education", tutor.education)
            InfoRow("Gender", tutor.genderLabel)
            if (tutor.profileText.isNotEmpty()) {
                HorizontalDivider()
                Text(tutor.profileText, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(100.dp),
        )
        Text(value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ReviewsCard(reviews: List<Review>, total: Int) {
    SectionCard("Reviews ($total)") {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            reviews.forEachIndexed { index, review ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            review.reviewerName,
                            style = MaterialTheme.typography.titleSmall,
                        )
                        RatingStars(review.rating, size = 13)
                    }
                    Text(
                        review.comment,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (index < reviews.lastIndex) HorizontalDivider()
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
            content()
        }
    }
}

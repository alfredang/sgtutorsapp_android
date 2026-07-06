package com.alfredang.sgtutors.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alfredang.sgtutors.ApiClient
import com.alfredang.sgtutors.EnquiryInput
import com.alfredang.sgtutors.PublicTutor
import kotlinx.coroutines.launch

private val PHONE_REGEX = Regex("""^(\+65 ?)?[689]\d{7}$""")

@Composable
fun EnquirySheet(tutor: PublicTutor, onDone: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var sent by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val emailValid = email.contains("@") && email.contains(".") && email.length >= 6
    val phoneValid = PHONE_REGEX.matches(phone.trim())
    val formValid = name.trim().length >= 2 && emailValid && phoneValid &&
        message.trim().length >= 10

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(start = 20.dp, end = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (sent) {
            Column(
                Modifier.fillMaxWidth().padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Brand.Verified,
                    modifier = Modifier.size(64.dp),
                )
                Text(
                    "Enquiry Sent",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "${tutor.displayName} has been notified by email and will contact you directly.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Button(onClick = onDone) { Text("Done") }
            }
            return@Column
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            TutorAvatar(tutor, 44)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(tutor.displayName, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Your enquiry is emailed to this tutor",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        OutlinedTextField(
            value = name, onValueChange = { name = it },
            label = { Text("Name") }, singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Email") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = phone, onValueChange = { phone = it },
            label = { Text("Singapore mobile (e.g. 9123 4567)") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = message, onValueChange = { message = it },
            label = { Text("Message") },
            placeholder = { Text("Tell the tutor what you need — subject, level, schedule, location…") },
            minLines = 4,
            modifier = Modifier.fillMaxWidth(),
            supportingText = {
                Text("At least 10 characters. Your contact details are shared only with this tutor.")
            },
        )

        val error = errorMessage
        if (error != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Warning, contentDescription = null,
                    tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Button(
            onClick = {
                scope.launch {
                    isSending = true
                    errorMessage = null
                    try {
                        ApiClient.sendEnquiry(
                            tutorId = tutor.id,
                            input = EnquiryInput(
                                name = name.trim(),
                                email = email.trim().lowercase(),
                                phone = phone.replace(" ", ""),
                                message = message.trim(),
                                turnstileToken = "android-app",
                            ),
                        )
                        sent = true
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Something went wrong"
                    }
                    isSending = false
                }
            },
            enabled = formValid && !isSending,
            modifier = Modifier.fillMaxWidth().height(50.dp),
        ) {
            if (isSending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Send Enquiry", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

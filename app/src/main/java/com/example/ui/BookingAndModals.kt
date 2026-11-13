/**
 * ShebaDoc (সেবাডক) - Booking Flow, Consultation SLA Room, TrxID Recovery & Registry Modals
 * Made by Md Shabbir Mia
 */
package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppLanguage
import com.example.data.model.Appointment
import com.example.data.model.DoctorProfile
import com.example.data.model.FamilyMember
import com.example.data.model.Prescription
import com.example.data.model.formatBdt
import com.example.data.model.formatForLang
import com.example.data.model.tr
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookAppointmentDialog(
    doctor: DoctorProfile,
    familyMembers: List<FamilyMember>,
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onConfirmBooking: (
        patientName: String,
        patientRelation: String,
        appointmentDate: String,
        timeSlot: String,
        consultationType: String,
        isFollowUp: Boolean,
        paymentMethod: String,
        paymentTrxId: String,
        symptomsSummary: String,
        vitalsBp: String,
        vitalsTempF: String
    ) -> Unit
) {
    var patientName by remember {
        mutableStateOf(familyMembers.firstOrNull()?.fullName ?: "Md. Rahim Uddin")
    }
    var patientRelation by remember {
        mutableStateOf(familyMembers.firstOrNull()?.relationEn ?: "Self")
    }
    val dateOptions = listOf("2026-11-13 (Today)", "2026-11-14 (Tomorrow)", "2026-11-16 (Mon)")
    var selectedDate by remember { mutableStateOf(dateOptions.first()) }
    var selectedSlot by remember { mutableStateOf(doctor.availableTimeSlots.first()) }
    var consultationType by remember { mutableStateOf("CHAMBER") }
    var isFollowUp by remember { mutableStateOf(false) }
    var paymentMethod by remember { mutableStateOf("BKASH") }
    var paymentTrxId by remember {
        mutableStateOf("BKX-${(100000..999999).random()}")
    }
    var symptomsSummary by remember {
        mutableStateOf(
            if (lang == AppLanguage.BN) "গত ৩ দিন যাবত সমস্যা এবং নিয়মিত চেকআপ"
            else "Experiencing symptoms for 3 days & specialist evaluation"
        )
    }
    var vitalsBp by remember { mutableStateOf("125/82 mmHg") }
    var vitalsTempF by remember { mutableStateOf("98.6 °F") }
    var unverifiedAuditAcknowledged by remember { mutableStateOf(doctor.isBmdcVerified) }

    val activeFee = if (isFollowUp) doctor.followUpFeeBdt else doctor.newVisitFeeBdt
    val queueWaitMins = (doctor.nextAvailableSerial - doctor.currentRunningSerial).coerceAtLeast(1) * doctor.avgMinutesPerPatient

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tr(lang, "Instant Chamber Serial & Booking", "তাৎক্ষণিক চেম্বার সিরিয়াল ও বুকিং"),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = tr(lang, doctor.nameEn, doctor.nameBn),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Live Serial Guarantee Banner + Escrow Protection
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tr(
                                    lang,
                                    "ASSIGNED SERIAL TOKEN #${doctor.nextAvailableSerial.formatForLang(lang)}",
                                    "আপনার নির্ধারিত সিরিয়াল টোকেন #${doctor.nextAvailableSerial.formatForLang(lang)}"
                                ),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = tr(
                                    lang,
                                    "Running: #${doctor.currentRunningSerial} • Wait: ~$queueWaitMins mins • 100% Escrow Protected",
                                    "চলছে: #${doctor.currentRunningSerial.formatForLang(lang)} • অপেক্ষা: ~${queueWaitMins.formatForLang(lang)} মি. • ১০০% রিফান্ড গ্যারান্টি"
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                            )
                        }
                        Text(
                            text = formatBdt(activeFee, lang),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (familyMembers.isNotEmpty()) {
                    Text(
                        text = tr(lang, "1. Select Family Member Profile", "১. পরিবারের সদস্য নির্বাচন করুন"),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        familyMembers.forEach { member ->
                            val selected = patientName == member.fullName
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    patientName = member.fullName
                                    patientRelation = member.relationEn
                                },
                                label = {
                                    Text("${member.fullName} (${tr(lang, member.relationEn, member.relationBn)})")
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                OutlinedTextField(
                    value = patientName,
                    onValueChange = { patientName = it },
                    label = { Text(tr(lang, "Patient Full Name", "রোগীর পূর্ণ নাম")) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("booking_patient_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = tr(
                        lang,
                        "2. Consultation Mode (Includes Rural Low-Data Audio)",
                        "২. পরামর্শের মাধ্যম (গ্রামাঞ্চলের জন্য লো-ডাটা অডিও সহ)"
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val modes = listOf(
                        "CHAMBER" to tr(lang, "Chamber Visit", "চেম্বার ভিজিট"),
                        "VIDEO" to tr(lang, "HD Video Call", "ভিডিও কনসালটেশন"),
                        "AUDIO_LOW_DATA" to tr(lang, "Low-Data Audio (3G/2G)", "লো-ডাটা অডিও কল")
                    )
                    modes.forEach { (code, label) ->
                        FilterChip(
                            selected = consultationType == code,
                            onClick = { consultationType = code },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .clickable { isFollowUp = !isFollowUp }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isFollowUp,
                        onCheckedChange = { isFollowUp = it },
                        modifier = Modifier.testTag("followup_discount_checkbox")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tr(
                                lang,
                                "Follow-Up / Report Show Visit (50% Fee Off)",
                                "রিপোর্ট দেখানো / ৩০ দিনের ফলো-আপ ভিজিট (৫০% ফি ছাড়)"
                            ),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = tr(
                                lang,
                                "New Visit: ${formatBdt(doctor.newVisitFeeBdt, lang)} → Follow-Up: ${formatBdt(doctor.followUpFeeBdt, lang)} + 20% Lab Coupon",
                                "নতুন ভিজিট: ${formatBdt(doctor.newVisitFeeBdt, lang)} → রিপোর্ট দেখানো: ${formatBdt(doctor.followUpFeeBdt, lang)} + ২০% ল্যাব ছাড়"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = tr(lang, "3. Select Date & Chamber Slot", "৩. তারিখ ও চেম্বার সময় নির্বাচন করুন"),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    dateOptions.forEach { d ->
                        FilterChip(
                            selected = selectedDate == d,
                            onClick = { selectedDate = d },
                            label = { Text(d) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    doctor.availableTimeSlots.forEach { slot ->
                        FilterChip(
                            selected = selectedSlot == slot,
                            onClick = { selectedSlot = slot },
                            label = { Text(slot) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = tr(
                        lang,
                        "4. Pre-Visit Vitals & Symptoms (Sent Ahead to Doctor)",
                        "৪. প্রি-ভিজিট ভাইটালস ও লক্ষণ (ডাক্তারের কাছে আগেভাগে পৌঁছাবে)"
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = vitalsBp,
                        onValueChange = { vitalsBp = it },
                        label = { Text(tr(lang, "BP (mmHg)", "রক্তচাপ (BP)")) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = vitalsTempF,
                        onValueChange = { vitalsTempF = it },
                        label = { Text(tr(lang, "Temp (°F)", "তাপমাত্রা (°F)")) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = symptomsSummary,
                    onValueChange = { symptomsSummary = it },
                    label = { Text(tr(lang, "Chief Complaints / Symptoms", "প্রধান সমস্যা ও লক্ষণসমূহ")) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = tr(
                        lang,
                        "5. Transparent Payment & 0%-Deduction Escrow",
                        "৫. পেমেন্ট মাধ্যম ও ১০০% এসক্রো রিফান্ড গ্যারান্টি"
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val payOptions = listOf(
                        "BKASH" to tr(lang, "bKash", "বিকাশ (bKash)"),
                        "NAGAD" to tr(lang, "Nagad", "নগদ (Nagad)"),
                        "CHAMBER_CASH" to tr(lang, "Chamber Cash", "চেম্বারে ক্যাশ"),
                        "CARD" to tr(lang, "Visa/Mastercard", "কার্ড")
                    )
                    payOptions.forEach { (code, label) ->
                        FilterChip(
                            selected = paymentMethod == code,
                            onClick = {
                                paymentMethod = code
                                paymentTrxId = "${code.take(3)}-${(100000..999999).random()}"
                            },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = paymentTrxId,
                    onValueChange = { paymentTrxId = it },
                    label = { Text(tr(lang, "Verified TrxID / Receipt Token", "ট্রানজেকশন আইডি (TrxID)")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (!doctor.isBmdcVerified) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                            .clickable { unverifiedAuditAcknowledged = !unverifiedAuditAcknowledged }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = unverifiedAuditAcknowledged,
                            onCheckedChange = { unverifiedAuditAcknowledged = it },
                            modifier = Modifier.testTag("unverified_bmdc_ack_checkbox")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tr(
                                    lang,
                                    "⚠ BMDC Verification Pending / Flagged",
                                    "⚠ বিএমডিসি সনদ এখনও যাচাই প্রক্রিয়াধীন"
                                ),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = tr(
                                    lang,
                                    "${doctor.bmdcWarningNoteEn} Tap to acknowledge before booking.",
                                    "${doctor.bmdcWarningNoteBn} জেনে-বুঝে বুকিং করতে টিক দিন।"
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val canConfirmBooking = patientName.isNotBlank() &&
                    paymentTrxId.isNotBlank() &&
                    (doctor.isBmdcVerified || unverifiedAuditAcknowledged)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text(tr(lang, "Cancel", "বাতিল"))
                    }
                    Button(
                        enabled = canConfirmBooking,
                        onClick = {
                            onConfirmBooking(
                                patientName,
                                patientRelation,
                                selectedDate.substringBefore(" "),
                                selectedSlot,
                                consultationType,
                                isFollowUp,
                                paymentMethod,
                                paymentTrxId,
                                symptomsSummary,
                                vitalsBp,
                                vitalsTempF
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .weight(1.6f)
                            .height(50.dp)
                            .testTag("confirm_booking_button")
                    ) {
                        Text(
                            text = tr(
                                lang,
                                "Confirm Serial #${doctor.nextAvailableSerial} • ${formatBdt(activeFee, lang)}",
                                "সিরিয়াল #${doctor.nextAvailableSerial.formatForLang(lang)} নিশ্চিত করুন • ${formatBdt(activeFee, lang)}"
                            ),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LiveConsultationRoomModal(
    appointment: Appointment,
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onCompleteAndGetRx: () -> Unit,
    onDoctorNoShowRefund: () -> Unit
) {
    var elapsedSeconds by remember { mutableIntStateOf(42) }
    var activeBandwidthMode by remember { mutableStateOf(appointment.consultationType) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            elapsedSeconds += 1
        }
    }

    val mins = elapsedSeconds / 60
    val secs = elapsedSeconds % 60
    val timerFormatted = "%02d:%02d / 10:00 MIN SLA".format(mins, secs)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = tr(
                                    lang,
                                    "10-Min SLA Protected Consultation Room",
                                    "১০ মিনিটের গ্যারান্টিযুক্ত কনসালটেশন রুম"
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = timerFormatted,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Monochrome Simulated Video/Audio Stage
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = tr(lang, appointment.doctorNameEn, appointment.doctorNameBn),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "${tr(lang, appointment.specialtyEn, appointment.specialtyBn)} • Serial #${appointment.serialNumber.formatForLang(lang)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = when (activeBandwidthMode) {
                                    "AUDIO_LOW_DATA" -> tr(
                                        lang,
                                        "32kbps Ultra-Stable Rural Audio Active (Zero Dropouts)",
                                        "৩২kbps লো-ডাটা অডিও মোড চালু (দুর্বল নেটওয়ার্কেও কল কাটবে না)"
                                    )
                                    "VIDEO" -> tr(
                                        lang,
                                        "HD Encrypted Telemedicine Video Stream Active",
                                        "এইচডি এনক্রিপ্টেড ভিডিও কনসালটেশন চালু"
                                    )
                                    else -> tr(
                                        lang,
                                        "Live Chamber Room Check-In Active",
                                        "চেম্বার রুম চেক-ইন সক্রিয়"
                                    )
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = tr(
                        lang,
                        "Switch Network Mode Instantly (Solves 3G Call Drops):",
                        "নেটওয়ার্ক দুর্বল হলে মোড পরিবর্তন করুন (কল ড্রপ রোধে):"
                    ),
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "VIDEO" to tr(lang, "HD Video", "এইচডি ভিডিও"),
                        "AUDIO_LOW_DATA" to tr(lang, "32kbps Low-Data Audio", "৩২kbps লো-ডাটা অডিও"),
                        "CHAMBER" to tr(lang, "Chamber Desk", "চেম্বার ডেস্ক")
                    ).forEach { (mode, label) ->
                        FilterChip(
                            selected = activeBandwidthMode == mode,
                            onClick = { activeBandwidthMode = mode },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = tr(
                                lang,
                                "Pre-Synced Vitals Visible on Doctor's Screen:",
                                "ডাক্তারের স্ক্রিনে প্রদর্শিত রোগীর ভাইটালস:"
                            ),
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "BP: ${appointment.vitalsBp} | Temp: ${appointment.vitalsTempF}\n${appointment.symptomsSummary}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onCompleteAndGetRx,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = tr(
                            lang,
                            "Complete Visit & Generate Bilingual Digital Rx",
                            "কনসালটেশন সম্পন্ন করুন ও বাংলা ই-প্রেসক্রিপশন নিন"
                        ),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDoctorNoShowRefund,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = tr(
                            lang,
                            "Doctor Absent / Disconnected → 100% Instant Escrow Refund",
                            "ডাক্তার অনুপস্থিত / সংযোগ বিচ্ছিন্ন → ১০০% তাৎক্ষণিক রিফান্ড"
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StuckTrxIdRecoveryModal(
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onReconcile: (paymentMethod: String, trxId: String, amountBdt: Int, notes: String) -> Unit
) {
    var paymentMethod by remember { mutableStateOf("BKASH") }
    var trxId by remember { mutableStateOf("BKX948271TIMEOUT") }
    var amountText by remember { mutableStateOf("1000") }
    var notes by remember {
        mutableStateOf(
            if (lang == AppLanguage.BN) "বিকাশ থেকে টাকা কেটে নিয়েছে কিন্তু নেটওয়ার্ক টাইমআউটে সিরিয়াল কনফার্ম হয়নি"
            else "Money deducted from bKash during network timeout before serial confirmation"
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tr(
                            lang,
                            "bKash / Nagad Stuck TrxID Auto-Recovery",
                            "বিকাশ / নগদ কেটে নেওয়া TrxID তাৎক্ষণিক রিকভারি"
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = tr(
                        lang,
                        "Solves Play Store Complaint #2: Instant 100% wallet reversal (৳0 fee deducted) when mobile data drops during MFS payment.",
                        "প্লে-স্টোরের প্রধান অভিযোগের সমাধান: পেমেন্ট করার সময় নেটওয়ার্ক কেটে গিয়ে টাকা আটকে গেলে তাৎক্ষণিক ১০০% রিফান্ড (৳০ চার্জ কর্তন)।"
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("BKASH", "NAGAD", "CARD").forEach { ch ->
                        FilterChip(
                            selected = paymentMethod == ch,
                            onClick = { paymentMethod = ch },
                            label = { Text(ch) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = trxId,
                    onValueChange = { trxId = it },
                    label = { Text(tr(lang, "Deducted bKash/Nagad TrxID", "কেটে নেওয়া বিকাশ/নগদ TrxID")) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stuck_trx_input")
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                    label = { Text(tr(lang, "Deducted Amount (BDT ৳)", "কেটে নেওয়া টাকার পরিমাণ (৳)")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(tr(lang, "Issue Details", "সমস্যার বিবরণ")) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text(tr(lang, "Cancel", "বাতিল"))
                    }
                    Button(
                        onClick = {
                            onReconcile(
                                paymentMethod,
                                trxId,
                                amountText.toIntOrNull() ?: 1000,
                                notes
                            )
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("verify_recover_trx_button")
                    ) {
                        Text(tr(lang, "Instant 100% Recover", "তাৎক্ষণিক ১০০% রিকভার"))
                    }
                }
            }
        }
    }
}

@Composable
fun BmdcVerifyModal(
    doctor: DoctorProfile,
    lang: AppLanguage,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (doctor.isBmdcVerified) Icons.Default.Verified else Icons.Default.Security,
                        contentDescription = "BMDC Status",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (doctor.isBmdcVerified) {
                                tr(lang, "BMDC Official Verified Record", "বিএমডিসি অফিশিয়াল যাচাইকৃত সনদ")
                            } else {
                                tr(lang, "BMDC Unverified / Pending Audit Warning", "বিএমডিসি অযাচাইকৃত / পেন্ডিং সতর্কবার্তা")
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = doctor.bmdcRegNo,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (doctor.isBmdcVerified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = tr(lang, doctor.bmdcWarningNoteEn, doctor.bmdcWarningNoteBn),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (doctor.isBmdcVerified) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(12.dp))

                InfoRowMono(
                    label = tr(lang, "Practitioner Name", "চিকিৎসকের নাম"),
                    value = tr(lang, doctor.nameEn, doctor.nameBn)
                )
                InfoRowMono(
                    label = tr(lang, "Reported Qualifications", "উল্লেখিত ডিগ্রিসমূহ"),
                    value = tr(lang, doctor.degreesEn, doctor.degreesBn)
                )
                InfoRowMono(
                    label = tr(lang, "BMDC Registration Year", "বিএমডিসি নিবন্ধন সাল"),
                    value = "${doctor.bmdcVerifiedYear.formatForLang(lang)} (${doctor.experienceYears.formatForLang(lang)} yrs exp)"
                )
                InfoRowMono(
                    label = tr(lang, "Primary Hospital / Chamber", "প্রধান চেম্বার ও হাসপাতাল"),
                    value = "${tr(lang, doctor.hospitalEn, doctor.hospitalBn)} — ${tr(lang, doctor.chamberRoomEn, doctor.chamberRoomBn)}"
                )
                InfoRowMono(
                    label = tr(lang, "Registry Standing", "রেজিস্ট্রেশন স্ট্যাটাস"),
                    value = if (doctor.isBmdcVerified) {
                        tr(lang, "ACTIVE • VERIFIED BY SHEBADOC AUDIT", "সক্রিয় • সেবাডক কর্তৃক শতভাগ যাচাইকৃত")
                    } else {
                        tr(lang, "UNVERIFIED / PENDING CERTIFICATE AUDIT — CAUTION", "অযাচাইকৃত / সনদ যাচাই প্রক্রিয়াধীন — সতর্কতা")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(tr(lang, "Close Registry Audit", "বন্ধ করুন"))
                }
            }
        }
    }
}

@Composable
fun PrescriptionDetailModal(
    rx: Prescription,
    lang: AppLanguage,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tr(lang, rx.doctorNameEn, rx.doctorNameBn),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = tr(lang, rx.specialtyEn, rx.specialtyBn),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "BMDC Reg: ${rx.bmdcRegNo}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Rx",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = tr(
                        lang,
                        "Patient: ${rx.patientName} (Age: ${rx.patientAge})",
                        "রোগীর নাম: ${rx.patientName} (বয়স: ${rx.patientAge.formatForLang(lang)} বছর)"
                    ),
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = tr(lang, "CLINICAL DIAGNOSIS", "রোগ নির্ণয় (Diagnosis)"),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tr(lang, rx.diagnosisEn, rx.diagnosisBn),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = tr(
                        lang,
                        "MEDICINES & DOSAGE SCHEDULE (১+০+১)",
                        "ওষুধের তালিকা ও সেবনবিধি (সকাল + দুপুর + রাত)"
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = rx.medicinesSummaryBn,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = rx.medicinesSummaryEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                InfoRowMono(
                    label = tr(lang, "Recommended Lab Investigations", "প্রয়োজনীয় ল্যাব টেস্ট"),
                    value = rx.recommendedTests
                )
                InfoRowMono(
                    label = tr(lang, "Clinical Advice & Diet", "পরামর্শ ও পথ্য"),
                    value = tr(lang, rx.adviceEn, rx.adviceBn)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tr(
                                    lang,
                                    "20% DIAGNOSTIC LAB DISCOUNT VOUCHER",
                                    "২০% ডায়াগনস্টিক ল্যাব টেস্ট ডিসকাউন্ট কুপন"
                                ),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = tr(
                                    lang,
                                    "Valid at Popular, Ibn Sina, Square & Labaid Diagnostics",
                                    "পপুলার, ইবনে সিনা, স্কয়ার ও ল্যাবএইড ডায়াগনস্টিকে প্রযোজ্য"
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                            )
                        }
                        Text(
                            text = rx.discountCouponCode,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(tr(lang, "Done", "সম্পন্ন"))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddFamilyMemberModal(
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onSaveMember: (
        fullName: String,
        relationEn: String,
        relationBn: String,
        age: Int,
        gender: String,
        bloodGroup: String,
        chronicConditions: String,
        phone: String
    ) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    val relations = listOf(
        Triple("Father", "বাবা", "Male"),
        Triple("Mother", "মা", "Female"),
        Triple("Self", "নিজে", "Male"),
        Triple("Spouse", "স্বামী/স্ত্রী", "Female"),
        Triple("Child", "সন্তান", "Male")
    )
    var selectedRelation by remember { mutableStateOf(relations.first()) }
    var ageText by remember { mutableStateOf("58") }
    val bloodGroups = listOf("O+", "A+", "B+", "AB+", "O-", "B-")
    var selectedBlood by remember { mutableStateOf("B+") }
    var chronicConditions by remember { mutableStateOf("Hypertension, Diabetes") }
    var phone by remember { mutableStateOf("+8801711000000") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    text = tr(lang, "Add Family Health Profile", "পরিবারের সদস্যের হেলথ প্রোফাইল যোগ করুন"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text(tr(lang, "Full Name (e.g., Abdul Karim)", "পূর্ণ নাম (যেমনঃ আব্দুল করিম)")) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("family_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = tr(lang, "Relation", "সম্পর্ক"),
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    relations.forEach { rel ->
                        FilterChip(
                            selected = selectedRelation.first == rel.first,
                            onClick = { selectedRelation = rel },
                            label = { Text(tr(lang, rel.first, rel.second)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = ageText,
                    onValueChange = { ageText = it.filter { ch -> ch.isDigit() } },
                    label = { Text(tr(lang, "Age (Years)", "বয়স (বছর)")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = tr(lang, "Blood Group", "রক্তের গ্রুপ"),
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    bloodGroups.forEach { bg ->
                        FilterChip(
                            selected = selectedBlood == bg,
                            onClick = { selectedBlood = bg },
                            label = { Text(bg) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = chronicConditions,
                    onValueChange = { chronicConditions = it },
                    label = { Text(tr(lang, "Chronic Conditions (Diabetes, BP, Asthma)", "পূর্ববর্তী রোগ (ডায়াবেটিস, প্রেসার, হাঁপানি)")) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(tr(lang, "Emergency Phone", "জরুরি মোবাইল নম্বর")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(tr(lang, "Cancel", "বাতিল"))
                    }
                    Button(
                        onClick = {
                            onSaveMember(
                                fullName.ifBlank { "Parent / Family Member" },
                                selectedRelation.first,
                                selectedRelation.second,
                                ageText.toIntOrNull() ?: 50,
                                selectedRelation.third,
                                selectedBlood,
                                chronicConditions,
                                phone
                            )
                        },
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("save_family_member_button")
                    ) {
                        Text(tr(lang, "Save Profile", "সংরক্ষণ করুন"))
                    }
                }
            }
        }
    }
}

@Composable
fun UpdateVitalsModal(
    appointment: Appointment,
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onSaveVitals: (bp: String, tempF: String, symptoms: String) -> Unit
) {
    var bp by remember { mutableStateOf(appointment.vitalsBp) }
    var tempF by remember { mutableStateOf(appointment.vitalsTempF) }
    var symptoms by remember { mutableStateOf(appointment.symptomsSummary) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MedicalInformation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tr(lang, "Update Pre-Visit Vitals", "প্রি-ভিজিট ভাইটালস আপডেট করুন"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = bp,
                    onValueChange = { bp = it },
                    label = { Text(tr(lang, "Blood Pressure (mmHg)", "রক্তচাপ (BP)")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tempF,
                    onValueChange = { tempF = it },
                    label = { Text(tr(lang, "Body Temperature (°F)", "শরীরের তাপমাত্রা (°F)")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = symptoms,
                    onValueChange = { symptoms = it },
                    label = { Text(tr(lang, "Chief Complaints / Notes for Doctor", "ডাক্তারের জন্য প্রধান লক্ষণ ও নোট")) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text(tr(lang, "Cancel", "বাতিল"))
                    }
                    Button(
                        onClick = { onSaveVitals(bp, tempF, symptoms) },
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Text(tr(lang, "Sync to Chamber", "চেম্বারে পাঠান"))
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRowMono(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 5.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

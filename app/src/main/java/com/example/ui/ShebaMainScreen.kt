/**
 * ShebaDoc (সেবাডক) - Main Patient Dashboard, Live Queue, Digital Rx & Protection Hub
 * Made by Md Shabbir Mia
 */
package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.data.model.AppLanguage
import com.example.data.model.Appointment
import com.example.data.model.BangladeshMedicalCatalog
import com.example.data.model.DoctorProfile
import com.example.data.model.FamilyMember
import com.example.data.model.PatientLoginCredentialSession
import com.example.data.model.Prescription
import com.example.data.model.RefundClaim
import com.example.data.model.formatBdt
import com.example.data.model.formatForLang
import com.example.data.model.tr
import com.example.data.repository.ShebaRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ShebaMainScreen(
    currentUserId: String,
    lang: AppLanguage,
    onToggleLanguage: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onSignedOut: () -> Unit
) {
    val viewModel: ShebaViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                ShebaViewModel(ShebaRepository(db), currentUserId)
            }
        }
    )

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()
    val selectedTriage by viewModel.selectedTriageSymptom.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBannerMessage.collectAsStateWithLifecycle()
    val appointmentsState by viewModel.appointmentsState.collectAsStateWithLifecycle()
    val prescriptionsState by viewModel.prescriptionsState.collectAsStateWithLifecycle()
    val familyMembersState by viewModel.familyMembersState.collectAsStateWithLifecycle()
    val refundClaimsState by viewModel.refundClaimsState.collectAsStateWithLifecycle()

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var doctorToBook by remember { mutableStateOf<DoctorProfile?>(null) }
    var doctorToVerifyBmdc by remember { mutableStateOf<DoctorProfile?>(null) }
    var selectedPrescription by remember { mutableStateOf<Prescription?>(null) }
    var showAddFamilyModal by remember { mutableStateOf(false) }
    var appointmentForVitals by remember { mutableStateOf<Appointment?>(null) }
    var appointmentInConsultationRoom by remember { mutableStateOf<Appointment?>(null) }
    var showStuckTrxRecoveryModal by remember { mutableStateOf(false) }

    if (selectedTab != 0) {
        BackHandler {
            selectedTab = 0
        }
    }

    val familyList = (familyMembersState as? UiState.Success)?.data ?: emptyList()
    val appointmentsList = (appointmentsState as? UiState.Success)?.data ?: emptyList()
    val activeQueueCount = appointmentsList.count { it.status == "IN_QUEUE" || it.status == "UPCOMING" }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = tr(lang, "SHEBADOC", "সেবাডক"),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = tr(
                                    lang,
                                    "100% Escrow Protected • Zero-Issue BD Health",
                                    "১০০% রিফান্ড গ্যারান্টি • নির্ভুল টেলিমেডিসিন"
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .testTag("language_toggle_button")
                                .clickable { onToggleLanguage() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = "Language Switch",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (lang == AppLanguage.BN) "বাংলা" else "EN",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = onToggleTheme,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme"
                            )
                        }

                        IconButton(
                            onClick = {
                                signOut(
                                    credentialManager = credentialManager,
                                    scope = coroutineScope,
                                    onSignOutComplete = onSignedOut
                                )
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("sign_out_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = tr(lang, "Sign out", "সাইন আউট")
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                val navItems = listOf(
                    Triple(0, tr(lang, "Doctors", "ডাক্তার"), Icons.Default.MedicalServices),
                    Triple(
                        1,
                        tr(lang, "Queue (${activeQueueCount.formatForLang(lang)})", "সিরিয়াল (${activeQueueCount.formatForLang(lang)})"),
                        Icons.Default.AccessTime
                    ),
                    Triple(2, tr(lang, "Rx & Fixes", "Rx ও সমাধান"), Icons.Default.Description),
                    Triple(3, tr(lang, "Protection", "সুরক্ষা"), Icons.Default.FamilyRestroom)
                )
                navItems.forEach { (index, label, icon) ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(imageVector = icon, contentDescription = label) },
                        label = {
                            Text(
                                text = label,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_$index")
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 3) {
                FloatingActionButton(
                    onClick = { showAddFamilyModal = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_family_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Add Family Member")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tr(lang, "Add Family Member", "সদস্য যোগ করুন"),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp)
            ) {
            if (statusBanner != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = statusBanner.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearStatusBanner() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            when (selectedTab) {
                0 -> DoctorsAndTriageTab(
                    lang = lang,
                    searchQuery = searchQuery,
                    onSearchChange = viewModel::updateSearchQuery,
                    selectedCategory = selectedCategory,
                    onSelectCategory = viewModel::selectCategory,
                    selectedCity = selectedCity,
                    onSelectCity = viewModel::selectCity,
                    selectedTriage = selectedTriage,
                    onSelectTriage = viewModel::applySymptomTriage,
                    activeQueueCount = activeQueueCount,
                    onOpenQueueTab = { selectedTab = 1 },
                    onOpenFixesTab = { selectedTab = 2 },
                    onOpenTrxRecovery = { showStuckTrxRecoveryModal = true },
                    onBookDoctor = { doctorToBook = it },
                    onVerifyBmdc = { doctorToVerifyBmdc = it }
                )

                1 -> LiveSerialQueueTab(
                    lang = lang,
                    appointmentsState = appointmentsState,
                    onAdvanceSerial = { viewModel.advanceQueueSerial(it, lang) },
                    onUpdateVitals = { appointmentForVitals = it },
                    onOpenConsultationRoom = { appointmentInConsultationRoom = it },
                    onCompleteAndIssueRx = {
                        viewModel.completeConsultationAndGenerateRx(it, lang) {
                            selectedTab = 2
                        }
                    },
                    onInstantDoctorNoShowRefund = {
                        viewModel.triggerInstantNoShowRefund(it, lang)
                    },
                    onExploreDoctors = { selectedTab = 0 }
                )

                2 -> DigitalRxAndSolvedIssuesTab(
                    lang = lang,
                    prescriptionsState = prescriptionsState,
                    onOpenPrescription = { selectedPrescription = it },
                    onJumpToTab = { selectedTab = it }
                )

                3 -> ProtectionAndFamilyTab(
                    lang = lang,
                    refundClaimsState = refundClaimsState,
                    familyMembersState = familyMembersState,
                    onOpenTrxRecoveryModal = { showStuckTrxRecoveryModal = true },
                    onAddMemberClick = { showAddFamilyModal = true },
                    onDeleteMember = { viewModel.deleteFamilyMember(it, lang) },
                    onSwitchAccountOrSignOut = {
                        signOut(
                            credentialManager = credentialManager,
                            scope = coroutineScope,
                            onSignOutComplete = onSignedOut
                        )
                    }
                )
            }
        }
        }
    }

    if (doctorToBook != null) {
        BookAppointmentDialog(
            doctor = doctorToBook!!,
            familyMembers = familyList,
            lang = lang,
            onDismiss = { doctorToBook = null },
            onConfirmBooking = { patientName, relation, date, slot, mode, isFollowUp, payMethod, trxId, symptoms, bp, temp ->
                val targetDoc = doctorToBook!!
                doctorToBook = null
                viewModel.bookDoctorAppointment(
                    doctor = targetDoc,
                    patientName = patientName,
                    patientRelation = relation,
                    appointmentDate = date,
                    timeSlot = slot,
                    consultationType = mode,
                    isFollowUp = isFollowUp,
                    paymentMethod = payMethod,
                    paymentTrxId = trxId,
                    symptomsSummary = symptoms,
                    vitalsBp = bp,
                    vitalsTempF = temp,
                    lang = lang,
                    onBookedSuccess = { selectedTab = 1 }
                )
            }
        )
    }

    if (doctorToVerifyBmdc != null) {
        BmdcVerifyModal(
            doctor = doctorToVerifyBmdc!!,
            lang = lang,
            onDismiss = { doctorToVerifyBmdc = null }
        )
    }

    if (selectedPrescription != null) {
        PrescriptionDetailModal(
            rx = selectedPrescription!!,
            lang = lang,
            onDismiss = { selectedPrescription = null }
        )
    }

    if (showAddFamilyModal) {
        AddFamilyMemberModal(
            lang = lang,
            onDismiss = { showAddFamilyModal = false },
            onSaveMember = { fullName, relEn, relBn, age, gender, blood, chronic, phone ->
                showAddFamilyModal = false
                viewModel.addFamilyMember(fullName, relEn, relBn, age, gender, blood, chronic, phone, lang)
            }
        )
    }

    if (appointmentForVitals != null) {
        UpdateVitalsModal(
            appointment = appointmentForVitals!!,
            lang = lang,
            onDismiss = { appointmentForVitals = null },
            onSaveVitals = { bp, temp, sym ->
                val appt = appointmentForVitals!!
                appointmentForVitals = null
                viewModel.updateAppointmentVitals(appt, bp, temp, sym, lang)
            }
        )
    }

    if (appointmentInConsultationRoom != null) {
        val currentAppt = appointmentInConsultationRoom!!
        LiveConsultationRoomModal(
            appointment = currentAppt,
            lang = lang,
            onDismiss = { appointmentInConsultationRoom = null },
            onCompleteAndGetRx = {
                appointmentInConsultationRoom = null
                viewModel.completeConsultationAndGenerateRx(currentAppt, lang) {
                    selectedTab = 2
                }
            },
            onDoctorNoShowRefund = {
                appointmentInConsultationRoom = null
                viewModel.triggerInstantNoShowRefund(currentAppt, lang)
            }
        )
    }

    if (showStuckTrxRecoveryModal) {
        StuckTrxIdRecoveryModal(
            lang = lang,
            onDismiss = { showStuckTrxRecoveryModal = false },
            onReconcile = { payMethod, trxId, amount, notes ->
                showStuckTrxRecoveryModal = false
                viewModel.reconcileStuckTrxId(payMethod, trxId, amount, notes, lang)
                selectedTab = 3
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DoctorsAndTriageTab(
    lang: AppLanguage,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    selectedCity: String,
    onSelectCity: (String) -> Unit,
    selectedTriage: com.example.data.model.SymptomTriageOption?,
    onSelectTriage: (com.example.data.model.SymptomTriageOption?) -> Unit,
    activeQueueCount: Int,
    onOpenQueueTab: () -> Unit,
    onOpenFixesTab: () -> Unit,
    onOpenTrxRecovery: () -> Unit,
    onBookDoctor: (DoctorProfile) -> Unit,
    onVerifyBmdc: (DoctorProfile) -> Unit
) {
    var bmdcFilterMode by rememberSaveable { mutableStateOf("ALL") } // ALL, VERIFIED_ONLY, UNVERIFIED_FLAGGED

    val filteredDoctors = remember(searchQuery, selectedCategory, selectedCity, bmdcFilterMode) {
        BangladeshMedicalCatalog.doctors.filter { doc ->
            val matchesCat = selectedCategory == "ALL" || doc.specialtyCategory == selectedCategory
            val matchesCity = selectedCity == "ALL" || doc.cityEn.equals(selectedCity, ignoreCase = true)
            val matchesBmdc = when (bmdcFilterMode) {
                "VERIFIED_ONLY" -> doc.isBmdcVerified
                "UNVERIFIED_FLAGGED" -> !doc.isBmdcVerified
                else -> true
            }
            val q = searchQuery.trim().lowercase()
            val matchesSearch = q.isEmpty() ||
                doc.nameEn.lowercase().contains(q) ||
                doc.nameBn.contains(q) ||
                doc.specialtyEn.lowercase().contains(q) ||
                doc.specialtyBn.contains(q) ||
                doc.hospitalEn.lowercase().contains(q) ||
                doc.hospitalBn.contains(q) ||
                doc.symptomKeywords.any { it.lowercase().contains(q) }
            matchesCat && matchesCity && matchesBmdc && matchesSearch
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Editorial Monochrome Hero Banner + Play Store Review Protection Bar
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(145.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_telemedicine_bw_1794501390913),
                            contentDescription = "Chamber Hero",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.92f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = tr(
                                    lang,
                                    "100% INSTANT ESCROW REFUND • ৳0 FEE DEDUCTION • LIVE SERIAL",
                                    "১০০% তাৎক্ষণিক বিকাশ/নগদ রিফান্ড • ৳০ চার্জ কর্তন • লাইভ সিরিয়াল"
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = tr(
                                lang,
                                "Active Serials: ${activeQueueCount.formatForLang(lang)} • Zero-Complaint Patient Protection",
                                "সক্রিয় সিরিয়াল: ${activeQueueCount.formatForLang(lang)} টি • শতভাগ রোগী সুরক্ষা গ্যারান্টি"
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tr(
                                lang,
                                "Solved DocTime/Sebaghar user review complaints: No 14-day bKash refund wait, no stuck TrxIDs, and no blind chamber waiting.",
                                "ডকটাইম ও সেবাঘরের ইউজার রিভিউয়ের সকল সমস্যার সমাধান: নেই ১৪ দিনের রিফান্ড ভোগান্তি বা আটকে যাওয়া বিকাশ পেমেন্ট।"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onOpenFixesTab,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(tr(lang, "Review Fixes", "রিভিউ সমাধান"))
                            }
                            OutlinedButton(
                                onClick = onOpenTrxRecovery,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("home_trx_recovery_button")
                            ) {
                                Text(tr(lang, "Recover TrxID", "TrxID রিকভারি"))
                            }
                            if (activeQueueCount > 0) {
                                Button(
                                    onClick = onOpenQueueTab,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(tr(lang, "Queue", "কিউ"))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Smart Symptom Triage Matcher
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tr(
                            lang,
                            "Smart Symptom Triage (Unsure Which Specialist?)",
                            "লক্ষণ দেখে সঠিক বিশেষজ্ঞ ডাক্তার নির্বাচন করুন"
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (selectedTriage != null) {
                        Text(
                            text = tr(lang, "Clear", "সব দেখুন"),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.clickable { onSelectTriage(null) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BangladeshMedicalCatalog.symptomTriageOptions.forEach { option ->
                        val isSelected = selectedTriage?.id == option.id
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .width(235.dp)
                                .testTag("triage_${option.id}")
                                .clickable {
                                    onSelectTriage(if (isSelected) null else option)
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = tr(lang, option.titleEn, option.titleBn),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = tr(lang, option.subtitleEn, option.subtitleBn),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = tr(lang, option.urgencyLevelEn, option.urgencyLevelBn),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Search & City / Department Filters
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    placeholder = {
                        Text(
                            tr(
                                lang,
                                "Search doctor, hospital (Square, Popular, Labaid) or symptom...",
                                "ডাক্তার, হাসপাতাল (স্কয়ার, পপুলার, ল্যাবএইড) বা লক্ষণ খুঁজুন..."
                            )
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("doctor_search_input")
                )

                // BMDC Verification Transparency Filter (5 Verified vs 2 Unverified Flagged)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val bmdcOptions = listOf(
                        "ALL" to tr(lang, "All Registry Status (7)", "সব ডাক্তার (৭)"),
                        "VERIFIED_ONLY" to tr(lang, "BMDC Verified Only (5)", "শুধু বিএমডিসি ভেরিফাইড (৫)"),
                        "UNVERIFIED_FLAGGED" to tr(lang, "Unverified / Pending Audit (2)", "আনভেরিফাইড / পেন্ডিং (২)")
                    )
                    bmdcOptions.forEach { (code, label) ->
                        FilterChip(
                            selected = bmdcFilterMode == code,
                            onClick = { bmdcFilterMode = code },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("bmdc_filter_$code")
                        )
                    }
                }

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val cities = listOf(
                        "ALL" to tr(lang, "All Cities", "সব শহর"),
                        "Dhaka" to tr(lang, "Dhaka", "ঢাকা"),
                        "Chattogram" to tr(lang, "Chattogram", "চট্টগ্রাম"),
                        "Sylhet" to tr(lang, "Sylhet", "সিলেট")
                    )
                    cities.forEach { (code, label) ->
                        FilterChip(
                            selected = selectedCity == code,
                            onClick = { onSelectCity(code) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val categories = listOf(
                        "ALL" to tr(lang, "All Specialists", "সকল বিশেষজ্ঞ"),
                        "CARDIOLOGY" to tr(lang, "Cardiology", "হৃদরোগ"),
                        "MEDICINE" to tr(lang, "Medicine & Liver", "মেডিসিন ও ডায়াবেটিস"),
                        "GYNECOLOGY" to tr(lang, "Gynecology", "গাইনী ও প্রসূতি"),
                        "PEDIATRICS" to tr(lang, "Child / Pediatrics", "শিশু রোগ"),
                        "NEUROLOGY" to tr(lang, "Neuromedicine", "নিউরোমেডিসিন"),
                        "ORTHOPEDICS" to tr(lang, "Orthopedics", "হাড় ও জয়েন্ট"),
                        "DERMATOLOGY" to tr(lang, "Skin & Allergy", "চর্ম ও অ্যালার্জি")
                    )
                    categories.forEach { (code, label) ->
                        FilterChip(
                            selected = selectedCategory == code,
                            onClick = { onSelectCategory(code) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
        }

        items(filteredDoctors, key = { it.id }) { doctor ->
            DoctorSpecialistCard(
                doctor = doctor,
                lang = lang,
                onBookClick = { onBookDoctor(doctor) },
                onVerifyBmdcClick = { onVerifyBmdc(doctor) }
            )
        }
    }
}

@Composable
private fun DoctorSpecialistCard(
    doctor: DoctorProfile,
    lang: AppLanguage,
    onBookClick: () -> Unit,
    onVerifyBmdcClick: () -> Unit
) {
    val chamberStatusLabel = when (doctor.chamberStatus) {
        "IN_CHAMBER" -> tr(lang, "IN CHAMBER NOW", "এখন চেম্বারে আছেন")
        "ON_TIME" -> tr(lang, "ON TIME", "সঠিক সময়ে")
        "DELAYED_20M" -> tr(lang, "DELAYED 20M", "২০ মিনিট বিলম্ব")
        else -> tr(lang, "IN OT", "অপারেশন থিয়েটারে")
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("doctor_card_${doctor.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (doctor.isBmdcVerified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.clickable { onVerifyBmdcClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (doctor.isBmdcVerified) Icons.Default.Verified else Icons.Default.Security,
                            contentDescription = "BMDC Status",
                            modifier = Modifier.size(14.dp),
                            tint = if (doctor.isBmdcVerified) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (doctor.isBmdcVerified) {
                                "${doctor.bmdcRegNo} • ${tr(lang, "BMDC VERIFIED", "বিএমডিসি ভেরিফাইড")}"
                            } else {
                                "${doctor.bmdcRegNo} • ${tr(lang, "UNVERIFIED WARNING", "অযাচাইকৃত সতর্কতা")}"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (doctor.isBmdcVerified) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text(
                        text = "$chamberStatusLabel • ${tr(lang, "Running #${doctor.currentRunningSerial}", "চলছে #${doctor.currentRunningSerial.formatForLang(lang)}")}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            if (!doctor.isBmdcVerified) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = tr(lang, "⚠ Audit Notice: ${doctor.bmdcWarningNoteEn}", "⚠ অডিট সতর্কতা: ${doctor.bmdcWarningNoteBn}"),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.img_doctor_avatar_bw_1794503612377),
                    contentDescription = tr(lang, doctor.nameEn, doctor.nameBn),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tr(lang, doctor.nameEn, doctor.nameBn),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = tr(lang, doctor.specialtyEn, doctor.specialtyBn),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = tr(lang, doctor.degreesEn, doctor.degreesBn),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${tr(lang, doctor.hospitalEn, doctor.hospitalBn)} (${tr(lang, doctor.chamberRoomEn, doctor.chamberRoomBn)})",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tr(
                            lang,
                            "New Visit: ${formatBdt(doctor.newVisitFeeBdt, lang)} | Report/Follow-up: ${formatBdt(doctor.followUpFeeBdt, lang)}",
                            "নতুন ভিজিট: ${formatBdt(doctor.newVisitFeeBdt, lang)} | রিপোর্ট/ফলো-আপ: ${formatBdt(doctor.followUpFeeBdt, lang)}"
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = tr(
                            lang,
                            "Next Serial Token: #${doctor.nextAvailableSerial} • 100% No-Show Refund Guarantee",
                            "পরবর্তী সিরিয়াল: #${doctor.nextAvailableSerial.formatForLang(lang)} • ১০০% নো-শো রিফান্ড গ্যারান্টি"
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onBookClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("book_doctor_${doctor.id}")
                ) {
                    Text(
                        text = tr(lang, "Take Serial", "সিরিয়াল নিন"),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveSerialQueueTab(
    lang: AppLanguage,
    appointmentsState: UiState<List<Appointment>>,
    onAdvanceSerial: (Appointment) -> Unit,
    onUpdateVitals: (Appointment) -> Unit,
    onOpenConsultationRoom: (Appointment) -> Unit,
    onCompleteAndIssueRx: (Appointment) -> Unit,
    onInstantDoctorNoShowRefund: (Appointment) -> Unit,
    onExploreDoctors: () -> Unit
) {
    when (appointmentsState) {
        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is UiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = appointmentsState.message)
            }
        }

        is UiState.Success -> {
            val appointments = appointmentsState.data
            if (appointments.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = tr(
                            lang,
                            "No Active Chamber Serials Yet",
                            "এখনও কোনো চেম্বার সিরিয়াল নেওয়া হয়নি"
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = tr(
                            lang,
                            "Book a specialist to track your live chamber serial number, join the 10-Min SLA room, or claim a 1-tap 100% refund if a doctor is delayed.",
                            "যেকোনো বিশেষজ্ঞ ডাক্তারের সিরিয়াল নিন এবং ঘরে বসেই লাইভ সিরিয়াল, ১০-মিনিট কনসালটেশন রুম ও তাৎক্ষণিক রিফান্ড সুবিধা পান।"
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onExploreDoctors) {
                        Text(tr(lang, "Browse Verified Doctors", "বিশেষজ্ঞ ডাক্তার দেখুন"))
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Escrow Guarantee Header
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = tr(
                                            lang,
                                            "Patient Escrow Active • Zero 14-Day Refund Wait",
                                            "রোগী এসক্রো সুরক্ষা চালু • রিফান্ডের জন্য ১৪ দিন অপেক্ষা নয়"
                                        ),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = tr(
                                            lang,
                                            "If a doctor no-shows or is delayed in OT, tap 'Doctor Absent → 100% Refund' for an instant reversal with ৳0 fee deduction.",
                                            "ডাক্তার চেম্বারে বা কলে না আসলে ১ ক্লিকে কোনো চার্জ কর্তন ছাড়াই (৳০ ফি) ১০০% তাৎক্ষণিক রিফান্ড নিন।"
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    items(appointments, key = { it.id }) { appt ->
                        val patientsAhead = (appt.serialNumber - appt.currentRunningSerial).coerceAtLeast(0)
                        val isActive = appt.status == "IN_QUEUE" || appt.status == "UPCOMING"

                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Surface(
                                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = tr(
                                                    lang,
                                                    "YOUR SERIAL #${appt.serialNumber.formatForLang(lang)}",
                                                    "আপনার সিরিয়াল #${appt.serialNumber.formatForLang(lang)}"
                                                ),
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = tr(
                                                    lang,
                                                    "Running Serial: #${appt.currentRunningSerial.formatForLang(lang)} • Ahead: ${patientsAhead.formatForLang(lang)} patients",
                                                    "চলমান সিরিয়াল: #${appt.currentRunningSerial.formatForLang(lang)} • সামনে বাকি: ${patientsAhead.formatForLang(lang)} জন"
                                                ),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (isActive) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f) else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = appt.status,
                                                style = MaterialTheme.typography.labelLarge,
                                                color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (isActive) {
                                                Text(
                                                    text = tr(
                                                        lang,
                                                        "~${appt.estimatedWaitMinutes.formatForLang(lang)} min wait",
                                                        "~${appt.estimatedWaitMinutes.formatForLang(lang)} মিনিট বাকি"
                                                    ),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onPrimary
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = tr(lang, appt.doctorNameEn, appt.doctorNameBn),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${tr(lang, appt.specialtyEn, appt.specialtyBn)} • ${tr(lang, appt.hospitalNameEn, appt.hospitalNameBn)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                InfoRowMono(
                                    label = tr(lang, "Patient & Schedule", "রোগী এবং সময়সূচী"),
                                    value = "${appt.patientName} (${appt.patientRelation}) • ${appt.appointmentDate} | ${appt.timeSlot} [${appt.consultationType}]"
                                )
                                InfoRowMono(
                                    label = tr(lang, "Escrow-Protected Fee & Receipt", "এসক্রো সুরক্ষিত ফি ও মানি রিসিট"),
                                    value = "${formatBdt(appt.feeBdt, lang)} (${if (appt.isFollowUp) tr(lang, "50% Follow-Up Discount", "৫০% ফলো-আপ ছাড়") else tr(lang, "New Visit", "নতুন ভিজিট")}) • ${appt.paymentMethod} TrxID: ${appt.paymentTrxId}"
                                )
                                InfoRowMono(
                                    label = tr(lang, "Pre-Visit Vitals & Chief Complaints", "প্রি-ভিজিট ভাইটালস ও লক্ষণ"),
                                    value = "BP: ${appt.vitalsBp} | Temp: ${appt.vitalsTempF} — ${appt.symptomsSummary}"
                                )

                                if (isActive) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { onAdvanceSerial(appt) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("advance_queue_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.FastForward,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(tr(lang, "Step Queue +1", "সিরিয়াল +১"))
                                        }

                                        OutlinedButton(
                                            onClick = { onUpdateVitals(appt) },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MonitorHeart,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(tr(lang, "Edit Vitals", "ভাইটালস"))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // 10-Min SLA Consultation Room + Complete Rx
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { onOpenConsultationRoom(appt) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("open_sla_room_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.VideoCall,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(tr(lang, "10-Min Room", "১০-মি. রুম"))
                                        }

                                        Button(
                                            onClick = { onCompleteAndIssueRx(appt) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary
                                            ),
                                            modifier = Modifier
                                                .weight(1.35f)
                                                .testTag("complete_rx_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = tr(lang, "Complete & Get Rx", "সম্পন্ন ও Rx নিন"),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // 1-Tap Instant 100% Doctor No-Show Refund Button
                                    OutlinedButton(
                                        onClick = { onInstantDoctorNoShowRefund(appt) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("instant_refund_button")
                                    ) {
                                        Text(
                                            text = tr(
                                                lang,
                                                "Doctor No-Show / Delayed → Instant 100% Refund (${formatBdt(appt.feeBdt, lang)} • ৳0 Fee)",
                                                "ডাক্তার অনুপস্থিত / বিলম্ব → ১ ক্লিকে ১০০% তাৎক্ষণিক রিফান্ড (${formatBdt(appt.feeBdt, lang)})"
                                            ),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DigitalRxAndSolvedIssuesTab(
    lang: AppLanguage,
    prescriptionsState: UiState<List<Prescription>>,
    onOpenPrescription: (Prescription) -> Unit,
    onJumpToTab: (Int) -> Unit
) {
    val prescriptions = (prescriptionsState as? UiState.Success)?.data ?: emptyList()
    var genericSearchQuery by remember { mutableStateOf("") }

    val filteredGenerics = remember(genericSearchQuery) {
        val q = genericSearchQuery.trim().lowercase()
        if (q.isEmpty()) {
            BangladeshMedicalCatalog.dgdaGenericMedicines
        } else {
            BangladeshMedicalCatalog.dgdaGenericMedicines.filter { item ->
                item.genericNameEn.lowercase().contains(q) ||
                    item.genericNameBn.contains(q) ||
                    item.indicationEn.lowercase().contains(q) ||
                    item.indicationBn.contains(q) ||
                    item.topBrandsBd.any { it.lowercase().contains(q) }
            }
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = tr(
                    lang,
                    "Bilingual Digital E-Prescription (Rx) Vault",
                    "দ্বিভাষিক ডিজিটাল ই-প্রেসক্রিপশন (Rx) ভল্ট"
                ),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = tr(
                    lang,
                    "Clear Bangla & English dosage schedules (১+০+১), generic medicine names, and 20% lab discount coupons.",
                    "স্পষ্ট বাংলা ও ইংরেজি সেবনবিধি (১+০+১), ওষুধের জেনেরিক নাম এবং ২০% ল্যাব ডিসকাউন্ট কুপন।"
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (prescriptions.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = tr(
                                lang,
                                "No Digital Prescriptions Issued Yet",
                                "এখনও কোনো ডিজিটাল প্রেসক্রিপশন তৈরি হয়নি"
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tr(
                                lang,
                                "Book an appointment and tap 'Complete & Get Rx' in the Queue tab to generate a permanent bilingual prescription.",
                                "যেকোনো ডাক্তারের সিরিয়াল নিয়ে 'সিরিয়াল' ট্যাবে গিয়ে 'সম্পন্ন ও Rx নিন' বাটনে চাপলেই ডিজিটাল প্রেসক্রিপশন তৈরি হবে।"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(prescriptions, key = { it.id }) { rx ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenPrescription(rx) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tr(lang, rx.doctorNameEn, rx.doctorNameBn),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${tr(lang, rx.specialtyEn, rx.specialtyBn)} • ${rx.bmdcRegNo}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = tr(lang, "View Rx", "Rx দেখুন"),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = tr(lang, rx.diagnosisEn, rx.diagnosisBn),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (lang == AppLanguage.BN) rx.medicinesSummaryBn else rx.medicinesSummaryEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3
                        )
                    }
                }
            }
        }

        // Section 2: DGDA Generic Medicine Price & Brand Alternative Comparator
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tr(
                    lang,
                    "DGDA Generic Medicine Price & Brand Finder",
                    "ডিজিডিএ জেনেরিক ওষুধের দাম ও বিকল্প ব্র্যান্ড তুলনা"
                ),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = tr(
                    lang,
                    "Solves Pharmacy Overcharging & Stockouts: Find approved Square, Incepta, Beximco & Renata equivalents at official unit prices.",
                    "ফার্মেসিতে নির্দিষ্ট ব্র্যান্ড না পেলে বা অতিরিক্ত দাম চাইলে অনুমোদিত স্কয়ার, ইনসেপ্টা, বেক্সিমকো ও রেনাটার বিকল্প ব্র্যান্ড ও সঠিক মূল্য দেখুন।"
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = genericSearchQuery,
                onValueChange = { genericSearchQuery = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = {
                    Text(
                        tr(
                            lang,
                            "Search generic or brand (e.g., Esomeprazole, Sergel, Maxpro, Bislol)...",
                            "জেনেরিক বা ব্র্যান্ড খুঁজুন (যেমনঃ Esomeprazole, Sergel, Maxpro)..."
                        )
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        items(filteredGenerics, key = { it.id }) { med ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${tr(lang, med.genericNameEn, med.genericNameBn)} (${med.strength})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = tr(lang, med.indicationEn, med.indicationBn),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${formatBdt(med.unitPriceBdt, lang)}/unit",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = tr(lang, "Approved Equivalent Brands:", "অনুমোদিত সমমানের ব্র্যান্ডসমূহ:") + " " + med.topBrandsBd.joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Section 3: 6 Real Online Review Complaints Analyzed & Solved + Visual Showcase
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Image(
                        painter = painterResource(id = R.drawable.img_bmdc_refund_shield_bw_1794503576748),
                        contentDescription = "All Features & Solutions Visual Overview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = tr(
                                lang,
                                "Online Review Analysis: 6 Real User Complaints Solved",
                                "অনলাইন রিভিউ বিশ্লেষণ: রোগীদের ৬টি প্রধান অভিযোগ ও সমাধান"
                            ),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tr(
                                lang,
                                "Engineered to eliminate every real failure reported across DocTime, Daktarbhai, Sebaghar & hospital chamber portals.",
                                "ডকটাইম, ডাক্তারভাই, সেবাঘর ও হাসপাতালের অ্যাপ ব্যবহারকারীদের সকল বাস্তব ভোগান্তির স্থায়ী সমাধান।"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(BangladeshMedicalCatalog.solvedPainPoints, key = { it.number }) { issue ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = issue.number,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = tr(lang, issue.appSourceTagEn, issue.appSourceTagBn),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = tr(lang, "Complaint: ${issue.problemTitleEn}", "অভিযোগ: ${issue.problemTitleBn}"),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tr(lang, "ShebaDoc Fix: ${issue.solutionTitleEn}", "সেবাডক সমাধান: ${issue.solutionTitleBn}"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tr(lang, issue.detailEn, issue.detailBn),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(onClick = { onJumpToTab(issue.targetTab) }) {
                        Text(tr(lang, issue.actionLabelEn, issue.actionLabelBn))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProtectionAndFamilyTab(
    lang: AppLanguage,
    refundClaimsState: UiState<List<RefundClaim>>,
    familyMembersState: UiState<List<FamilyMember>>,
    onOpenTrxRecoveryModal: () -> Unit,
    onAddMemberClick: () -> Unit,
    onDeleteMember: (String) -> Unit,
    onSwitchAccountOrSignOut: () -> Unit
) {
    val claims = (refundClaimsState as? UiState.Success)?.data ?: emptyList()
    val members = (familyMembersState as? UiState.Success)?.data ?: emptyList()

    var phoneField by remember { mutableStateOf(PatientLoginCredentialSession.linkedPhone) }
    var emailField by remember { mutableStateOf(Firebase.auth.currentUser?.email ?: PatientLoginCredentialSession.linkedEmail) }
    var passwordField by remember { mutableStateOf("ShebaDoc#2026") }
    var credentialsSavedNotice by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Section 0: Google + Phone + Email + Password Identity & Login Manager
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tr(
                                    lang,
                                    "Multi-Channel Login & Identity Hub",
                                    "লগইন ও অ্যাকাউন্ট নিরাপত্তা কেন্দ্র (গুগল + মোবাইল + ইমেইল)"
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = tr(
                                    lang,
                                    "Google Cloud Auth + Bangladesh Phone (+880), Email & Password",
                                    "গুগল ক্লাউড সিন্ক + বাংলাদেশ মোবাইল নম্বর (+৮৮০), ইমেইল ও পাসওয়ার্ড"
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = tr(lang, "ACTIVE", "সক্রিয়"),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = phoneField,
                        onValueChange = {
                            phoneField = it
                            credentialsSavedNotice = false
                        },
                        label = { Text(tr(lang, "Linked Login Phone Number (+880)", "লগইন মোবাইল নম্বর (+৮৮০)")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = emailField,
                        onValueChange = {
                            emailField = it
                            credentialsSavedNotice = false
                        },
                        label = { Text(tr(lang, "Linked Google / Email Address", "গুগল / ইমেইল অ্যাড্রেস")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = passwordField,
                        onValueChange = {
                            passwordField = it
                            credentialsSavedNotice = false
                        },
                        label = { Text(tr(lang, "Account Password / Security PIN", "অ্যাকাউন্ট পাসওয়ার্ড / সিকিউরিটি পিন")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (credentialsSavedNotice) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = tr(
                                lang,
                                "✓ Phone, Email & Password credentials updated and linked with Google Cloud.",
                                "✓ মোবাইল নম্বর, ইমেইল এবং পাসওয়ার্ড গুগল ক্লাউডের সাথে সংরক্ষিত হয়েছে।"
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                PatientLoginCredentialSession.linkedPhone = phoneField.trim()
                                PatientLoginCredentialSession.linkedEmail = emailField.trim()
                                PatientLoginCredentialSession.passwordMasked = "••••••••"
                                credentialsSavedNotice = true
                            },
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Text(tr(lang, "Save Phone/Email/Pass", "তথ্য সংরক্ষণ করুন"))
                        }
                        OutlinedButton(
                            onClick = onSwitchAccountOrSignOut,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(tr(lang, "Switch Login", "লগইন পরিবর্তন"))
                        }
                    }
                }
            }
        }

        // Section 1: Instant Escrow Refund & bKash/Nagad Stuck TrxID Protection Hub
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = tr(
                            lang,
                            "ZERO-DEDUCTION REFUND & MFS PROTECTION HUB",
                            "১০০% তাৎক্ষণিক রিফান্ড ও বিকাশ/নগদ সুরক্ষা কেন্দ্র"
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = tr(
                            lang,
                            "Unlike apps that take 5–14 working days and deduct bKash cash-out charges, ShebaDoc guarantees 0 BDT fee deduction and instant TrxID reconciliation.",
                            "অন্যান্য অ্যাপে রিফান্ড পেতে ৫-১৪ দিন অপেক্ষা ও ক্যাশ-আউট চার্জ কাটা হলেও সেবাডকে আপনি পাচ্ছেন ৳০ চার্জ কর্তনে তাৎক্ষণিক রিফান্ড ও TrxID রিকভারি।"
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onOpenTrxRecoveryModal,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_trx_recovery_modal_button")
                    ) {
                        Text(
                            text = tr(
                                lang,
                                "Recover Stuck bKash / Nagad TrxID Now",
                                "আটকে যাওয়া বিকাশ / নগদ TrxID রিকভার করুন"
                            ),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (claims.isNotEmpty()) {
            item {
                Text(
                    text = tr(
                        lang,
                        "Verified Instant Refund & Reversal Ledger (${claims.size.formatForLang(lang)})",
                        "তাৎক্ষণিক রিফান্ড ও পেমেন্ট রিকভারি তালিকা (${claims.size.formatForLang(lang)})"
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(claims, key = { it.id }) { claim ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${claim.refundReferenceId} (${claim.paymentMethod})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${formatBdt(claim.refundAmountBdt, lang)} • ${tr(lang, "৳0 Fee", "৳০ চার্জ")}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${tr(lang, claim.doctorNameEn, claim.doctorNameBn)} • Orig TrxID: ${claim.originalTrxId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = claim.reasonSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Section 2: Family Health Profiles
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tr(
                    lang,
                    "Family Health Profiles (One-Tap Serial Booking)",
                    "পরিবারের সদস্যদের হেলথ প্রোফাইল (এক ক্লিকে সিরিয়াল বুকিং)"
                ),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = tr(
                    lang,
                    "Book appointments for your elderly parents, spouse, or children without needing multiple phone numbers.",
                    "আলাদা মোবাইল নম্বর ছাড়াই বাবা, মা, সন্তান বা জীবনসঙ্গীর জন্য দ্রুত ডাক্তারের সিরিয়াল নিন।"
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (members.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = tr(
                                lang,
                                "No Family Profiles Added Yet",
                                "এখনও কোনো পরিবারের সদস্যের প্রোফাইল যোগ করা হয়নি"
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onAddMemberClick) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(tr(lang, "Add Father / Mother / Child Profile", "বাবা / মা / সন্তানের প্রোফাইল যোগ করুন"))
                        }
                    }
                }
            }
        } else {
            items(members, key = { it.id }) { member ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = member.fullName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${tr(lang, member.relationEn, member.relationBn)} • ${member.bloodGroup}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tr(
                                    lang,
                                    "Age: ${member.age} yrs • Conditions: ${member.chronicConditions}",
                                    "বয়স: ${member.age.formatForLang(lang)} বছর • পূর্ববর্তী রোগ: ${member.chronicConditions}"
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = member.phone,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { onDeleteMember(member.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = tr(lang, "Remove profile", "মুছে ফেলুন")
                            )
                        }
                    }
                }
            }
        }
    }
}

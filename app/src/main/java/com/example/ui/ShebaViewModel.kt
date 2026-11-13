/**
 * ShebaDoc (সেবাডক) - Reactive State Management ViewModel
 * Made by Md Shabbir Mia
 */
package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppLanguage
import com.example.data.model.Appointment
import com.example.data.model.BangladeshMedicalCatalog
import com.example.data.model.DoctorProfile
import com.example.data.model.FamilyMember
import com.example.data.model.Prescription
import com.example.data.model.RefundClaim
import com.example.data.model.SymptomTriageOption
import com.example.data.repository.ShebaRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class ShebaViewModel(
    private val repository: ShebaRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedCity = MutableStateFlow("ALL")
    val selectedCity: StateFlow<String> = _selectedCity.asStateFlow()

    private val _selectedTriageSymptom = MutableStateFlow<SymptomTriageOption?>(null)
    val selectedTriageSymptom: StateFlow<SymptomTriageOption?> = _selectedTriageSymptom.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    val appointmentsState: StateFlow<UiState<List<Appointment>>> = repository.observeAppointments(currentUserId)
        .map<List<Appointment>, UiState<List<Appointment>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing appointments", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load appointments"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val prescriptionsState: StateFlow<UiState<List<Prescription>>> = repository.observePrescriptions(currentUserId)
        .map<List<Prescription>, UiState<List<Prescription>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing prescriptions", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load prescriptions"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val familyMembersState: StateFlow<UiState<List<FamilyMember>>> = repository.observeFamilyMembers(currentUserId)
        .map<List<FamilyMember>, UiState<List<FamilyMember>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing family members", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load family profiles"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val refundClaimsState: StateFlow<UiState<List<RefundClaim>>> = repository.observeRefundClaims(currentUserId)
        .map<List<RefundClaim>, UiState<List<RefundClaim>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing refund claims", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load refund records"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        if (category == "ALL") {
            _selectedTriageSymptom.value = null
        }
    }

    fun selectCity(city: String) {
        _selectedCity.value = city
    }

    fun applySymptomTriage(option: SymptomTriageOption?) {
        _selectedTriageSymptom.value = option
        _selectedCategory.value = option?.targetCategory ?: "ALL"
    }

    fun clearStatusBanner() {
        _statusBannerMessage.value = null
    }

    fun bookDoctorAppointment(
        doctor: DoctorProfile,
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
        vitalsTempF: String,
        lang: AppLanguage,
        onBookedSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.bookAppointment(
                doctor = doctor,
                patientName = patientName,
                patientRelation = patientRelation,
                appointmentDate = appointmentDate,
                timeSlot = timeSlot,
                consultationType = consultationType,
                isFollowUp = isFollowUp,
                paymentMethod = paymentMethod,
                paymentTrxId = paymentTrxId,
                symptomsSummary = symptomsSummary,
                vitalsBp = vitalsBp,
                vitalsTempF = vitalsTempF
            )
            _isBusy.value = false
            result.onSuccess {
                _statusBannerMessage.value = if (lang == AppLanguage.BN) {
                    "সিরিয়াল টোকেন #${doctor.nextAvailableSerial} নিশ্চিত হয়েছে! ফি এসক্রোতে সুরক্ষিত।"
                } else {
                    "Serial Token #${doctor.nextAvailableSerial} confirmed! Protected by 100% Refund Escrow."
                }
                onBookedSuccess()
            }.onFailure { err ->
                _statusBannerMessage.value = err.localizedMessage ?: "Booking failed"
            }
        }
    }

    fun advanceQueueSerial(appointment: Appointment, lang: AppLanguage) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.advanceLiveQueueSerial(appointment)
            _isBusy.value = false
            result.onSuccess {
                val nextSerial = (appointment.currentRunningSerial + 1).coerceAtMost(appointment.serialNumber)
                _statusBannerMessage.value = if (lang == AppLanguage.BN) {
                    "চেম্বার সিরিয়াল আপডেট হয়েছে: বর্তমানে চলছে #$nextSerial"
                } else {
                    "Live Chamber Queue advanced: Running Serial #$nextSerial"
                }
            }.onFailure { err ->
                _statusBannerMessage.value = err.localizedMessage ?: "Queue update failed"
            }
        }
    }

    fun updateAppointmentVitals(
        appointment: Appointment,
        bp: String,
        tempF: String,
        symptoms: String,
        lang: AppLanguage
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.updatePreVisitVitals(
                appointment = appointment,
                vitalsBp = bp,
                vitalsTempF = tempF,
                symptomsSummary = symptoms
            )
            _isBusy.value = false
            result.onSuccess {
                _statusBannerMessage.value = if (lang == AppLanguage.BN) {
                    "রোগীর প্রি-ভিজিট ভাইটালস (BP ও তাপমাত্রা) ডাক্তারের জন্য সংরক্ষিত হয়েছে।"
                } else {
                    "Pre-visit vitals & chief complaints synced for the doctor."
                }
            }.onFailure { err ->
                _statusBannerMessage.value = err.localizedMessage ?: "Failed to update vitals"
            }
        }
    }

    fun completeConsultationAndGenerateRx(
        appointment: Appointment,
        lang: AppLanguage,
        onCompleted: () -> Unit
    ) {
        val matchedDoctor = BangladeshMedicalCatalog.doctors.find { it.id == appointment.doctorId }
            ?: BangladeshMedicalCatalog.doctors.first()
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.completeAppointmentAndIssuePrescription(
                appointment = appointment,
                bmdcRegNo = matchedDoctor.bmdcRegNo,
                patientAge = 34,
                diagnosisEn = matchedDoctor.defaultDiagnosisEn,
                diagnosisBn = matchedDoctor.defaultDiagnosisBn,
                medicinesSummaryEn = matchedDoctor.defaultRxEn,
                medicinesSummaryBn = matchedDoctor.defaultRxBn,
                adviceEn = matchedDoctor.defaultAdviceEn,
                adviceBn = matchedDoctor.defaultAdviceBn,
                recommendedTests = matchedDoctor.defaultTests,
                followUpDays = 14
            )
            _isBusy.value = false
            result.onSuccess {
                _statusBannerMessage.value = if (lang == AppLanguage.BN) {
                    "কনসালটেশন সম্পন্ন! দ্বিভাষিক ডিজিটাল ই-প্রেসক্রিপশন (Rx) তৈরি হয়েছে।"
                } else {
                    "Consultation completed! Bilingual Digital E-Prescription (Rx) issued."
                }
                onCompleted()
            }.onFailure { err ->
                _statusBannerMessage.value = err.localizedMessage ?: "Failed to generate prescription"
            }
        }
    }

    fun triggerInstantNoShowRefund(
        appointment: Appointment,
        lang: AppLanguage
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.triggerDoctorNoShowInstantRefund(appointment)
            _isBusy.value = false
            result.onSuccess { refId ->
                _statusBannerMessage.value = if (lang == AppLanguage.BN) {
                    "১০০% তাৎক্ষণিক রিফান্ড সম্পন্ন (৳${appointment.feeBdt} • ৳০ চার্জ কর্তন)! রেফারেন্স: $refId"
                } else {
                    "100% Instant Escrow Refund Issued (৳${appointment.feeBdt} • ৳0 Deduction)! Ref: $refId"
                }
            }.onFailure { err ->
                _statusBannerMessage.value = err.localizedMessage ?: "Refund failed"
            }
        }
    }

    fun reconcileStuckTrxId(
        paymentMethod: String,
        originalTrxId: String,
        deductedAmountBdt: Int,
        notes: String,
        lang: AppLanguage
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.reconcileStuckMfsTransaction(
                paymentMethod = paymentMethod,
                originalTrxId = originalTrxId,
                deductedAmountBdt = deductedAmountBdt,
                doctorNameEn = "MFS Stuck Payment Auto-Recovery",
                doctorNameBn = "বিকাশ/নগদ আটকে যাওয়া পেমেন্ট রিকভারি",
                notes = notes
            )
            _isBusy.value = false
            result.onSuccess { refId ->
                _statusBannerMessage.value = if (lang == AppLanguage.BN) {
                    "TrxID ($originalTrxId) যাচাই সম্পন্ন! ৳$deductedAmountBdt তাৎক্ষণিক ওয়ালেটে ফেরত দেওয়া হয়েছে ($refId)।"
                } else {
                    "TrxID ($originalTrxId) reconciled! ৳$deductedAmountBdt refunded with ৳0 fee ($refId)."
                }
            }.onFailure { err ->
                _statusBannerMessage.value = err.localizedMessage ?: "TrxID reconciliation failed"
            }
        }
    }

    fun cancelAppointment(appointment: Appointment, lang: AppLanguage) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.cancelAppointment(appointment)
            _isBusy.value = false
            result.onSuccess {
                _statusBannerMessage.value = if (lang == AppLanguage.BN) {
                    "অ্যাপয়েন্টমেন্ট বাতিল করা হয়েছে।"
                } else {
                    "Appointment has been cancelled."
                }
            }.onFailure { err ->
                _statusBannerMessage.value = err.localizedMessage ?: "Failed to cancel appointment"
            }
        }
    }

    fun addFamilyMember(
        fullName: String,
        relationEn: String,
        relationBn: String,
        age: Int,
        gender: String,
        bloodGroup: String,
        chronicConditions: String,
        phone: String,
        lang: AppLanguage
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.addFamilyMember(
                fullName = fullName,
                relationEn = relationEn,
                relationBn = relationBn,
                age = age,
                gender = gender,
                bloodGroup = bloodGroup,
                chronicConditions = chronicConditions,
                phone = phone
            )
            _isBusy.value = false
            result.onSuccess {
                _statusBannerMessage.value = if (lang == AppLanguage.BN) {
                    "পরিবারের সদস্য ($fullName) যুক্ত হয়েছে!"
                } else {
                    "Family profile ($fullName) saved!"
                }
            }.onFailure { err ->
                _statusBannerMessage.value = err.localizedMessage ?: "Failed to save family member"
            }
        }
    }

    fun deleteFamilyMember(memberId: String, lang: AppLanguage) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.deleteFamilyMember(memberId)
            _isBusy.value = false
            result.onSuccess {
                _statusBannerMessage.value = if (lang == AppLanguage.BN) {
                    "প্রোফাইল মুছে ফেলা হয়েছে।"
                } else {
                    "Family profile removed."
                }
            }
        }
    }

    private companion object {
        const val TAG = "ShebaViewModel"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}

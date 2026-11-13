package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Appointment
import com.example.data.model.DoctorProfile
import com.example.data.model.FamilyMember
import com.example.data.model.Prescription
import com.example.data.model.RefundClaim
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ShebaRepository(private val db: FirebaseFirestore) {

    // CRITICAL: Always initialize with R.string.firestore_database_id
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // 1. Observe Appointments in real time
    fun observeAppointments(userId: String = auth.currentUser?.uid.orEmpty()): Flow<List<Appointment>> = flow {
        val targetUid = userId.ifBlank {
            auth.currentUser?.uid ?: throw FirebaseFirestoreException(
                "PERMISSION_DENIED: Unauthenticated caller cannot observe appointments",
                FirebaseFirestoreException.Code.PERMISSION_DENIED
            )
        }
        val path = "users/$targetUid/appointments"
        emitAll(
            db.collection("users").document(targetUid).collection("appointments")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        Appointment::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == targetUid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    // 2. Observe Prescriptions in real time
    fun observePrescriptions(userId: String = auth.currentUser?.uid.orEmpty()): Flow<List<Prescription>> = flow {
        val targetUid = userId.ifBlank {
            auth.currentUser?.uid ?: throw FirebaseFirestoreException(
                "PERMISSION_DENIED: Unauthenticated caller cannot observe prescriptions",
                FirebaseFirestoreException.Code.PERMISSION_DENIED
            )
        }
        val path = "users/$targetUid/prescriptions"
        emitAll(
            db.collection("users").document(targetUid).collection("prescriptions")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        Prescription::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == targetUid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    // 3. Observe Family Members in real time
    fun observeFamilyMembers(userId: String = auth.currentUser?.uid.orEmpty()): Flow<List<FamilyMember>> = flow {
        val targetUid = userId.ifBlank {
            auth.currentUser?.uid ?: throw FirebaseFirestoreException(
                "PERMISSION_DENIED: Unauthenticated caller cannot observe family members",
                FirebaseFirestoreException.Code.PERMISSION_DENIED
            )
        }
        val path = "users/$targetUid/family_members"
        emitAll(
            db.collection("users").document(targetUid).collection("family_members")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        FamilyMember::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedBy { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == targetUid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    // 4. Observe Instant 0%-Deduction Refund & TrxID Recovery Claims in real time
    fun observeRefundClaims(userId: String = auth.currentUser?.uid.orEmpty()): Flow<List<RefundClaim>> = flow {
        val targetUid = userId.ifBlank {
            auth.currentUser?.uid ?: throw FirebaseFirestoreException(
                "PERMISSION_DENIED: Unauthenticated caller cannot observe refund claims",
                FirebaseFirestoreException.Code.PERMISSION_DENIED
            )
        }
        val path = "users/$targetUid/refund_claims"
        emitAll(
            db.collection("users").document(targetUid).collection("refund_claims")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        RefundClaim::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == targetUid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    // 5. Book a new Doctor Appointment with Live Serial Token
    suspend fun bookAppointment(
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
        assignedSerial: Int = doctor.nextAvailableSerial
    ): Result<String> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("appointments").document()
        val waitMins = ((assignedSerial - doctor.currentRunningSerial).coerceAtLeast(1)) * doctor.avgMinutesPerPatient
        val fee = if (isFollowUp) doctor.followUpFeeBdt else doctor.newVisitFeeBdt
        val payload = mapOf(
            "userId" to uid,
            "doctorId" to doctor.id,
            "doctorNameEn" to doctor.nameEn,
            "doctorNameBn" to doctor.nameBn,
            "specialtyEn" to doctor.specialtyEn,
            "specialtyBn" to doctor.specialtyBn,
            "hospitalNameEn" to doctor.hospitalEn,
            "hospitalNameBn" to doctor.hospitalBn,
            "patientName" to patientName.trim().ifBlank { "Patient" },
            "patientRelation" to patientRelation.trim().ifBlank { "Self" },
            "appointmentDate" to appointmentDate.trim().ifBlank { "2026-11-15" },
            "timeSlot" to timeSlot.trim().ifBlank { "06:00 PM - 07:00 PM" },
            "consultationType" to consultationType,
            "serialNumber" to assignedSerial.coerceIn(1, 500),
            "currentRunningSerial" to doctor.currentRunningSerial.coerceIn(0, 500),
            "estimatedWaitMinutes" to waitMins.coerceIn(0, 1440),
            "chamberStatus" to doctor.chamberStatus,
            "feeBdt" to fee.coerceIn(0, 50000),
            "isFollowUp" to isFollowUp,
            "paymentMethod" to paymentMethod,
            "paymentTrxId" to paymentTrxId.trim().ifBlank { "TRX-${System.currentTimeMillis().toString().takeLast(6)}" },
            "symptomsSummary" to symptomsSummary.trim().take(500),
            "vitalsBp" to vitalsBp.trim().ifBlank { "120/80 mmHg" }.take(40),
            "vitalsTempF" to vitalsTempF.trim().ifBlank { "98.6 °F" }.take(40),
            "status" to "IN_QUEUE",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    // 6. Fetch single appointment (also used to verify cross-user isolation in RuleTest)
    suspend fun getAppointmentById(targetUserId: String, appointmentId: String): Result<Appointment?> {
        val docRef = db.collection("users").document(targetUserId)
            .collection("appointments").document(appointmentId)
        return try {
            val snap = docRef.get().await()
            Result.success(
                snap.toObject(
                    Appointment::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    // 7. Advance Live Queue Serial (simulates chamber queue moving forward toward patient's turn)
    suspend fun advanceLiveQueueSerial(appointment: Appointment): Result<Unit> {
        val uid = requireUserId()
        require(appointment.id.isNotBlank()) { "Appointment ID must not be blank" }
        val docRef = db.collection("users").document(uid)
            .collection("appointments").document(appointment.id)
        val nextRunning = (appointment.currentRunningSerial + 1).coerceAtMost(appointment.serialNumber)
        val remainingPatients = (appointment.serialNumber - nextRunning).coerceAtLeast(0)
        val newWaitMins = remainingPatients * 8
        val newChamberStatus = if (remainingPatients == 0) "IN_CHAMBER" else appointment.chamberStatus

        val updates = mapOf(
            "currentRunningSerial" to nextRunning,
            "estimatedWaitMinutes" to newWaitMins,
            "chamberStatus" to newChamberStatus,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    // 8. Update Pre-Visit Vitals & Chief Complaints (for Low-Data Telemedicine / Chamber Prep)
    suspend fun updatePreVisitVitals(
        appointment: Appointment,
        vitalsBp: String,
        vitalsTempF: String,
        symptomsSummary: String
    ): Result<Unit> {
        val uid = requireUserId()
        require(appointment.id.isNotBlank()) { "Appointment ID must not be blank" }
        val docRef = db.collection("users").document(uid)
            .collection("appointments").document(appointment.id)
        val updates = mapOf(
            "vitalsBp" to vitalsBp.trim().take(40),
            "vitalsTempF" to vitalsTempF.trim().take(40),
            "symptomsSummary" to symptomsSummary.trim().take(500),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    // 9. Complete Appointment & Issue Bilingual Digital E-Prescription (Rx)
    suspend fun completeAppointmentAndIssuePrescription(
        appointment: Appointment,
        bmdcRegNo: String,
        patientAge: Int,
        diagnosisEn: String,
        diagnosisBn: String,
        medicinesSummaryEn: String,
        medicinesSummaryBn: String,
        adviceEn: String,
        adviceBn: String,
        recommendedTests: String,
        followUpDays: Int = 14
    ): Result<String> {
        val uid = requireUserId()
        require(appointment.id.isNotBlank()) { "Appointment ID must not be blank" }
        val apptRef = db.collection("users").document(uid)
            .collection("appointments").document(appointment.id)
        val rxRef = db.collection("users").document(uid)
            .collection("prescriptions").document()

        return try {
            apptRef.update(
                mapOf(
                    "currentRunningSerial" to appointment.serialNumber,
                    "estimatedWaitMinutes" to 0,
                    "status" to "COMPLETED",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            val rxPayload = mapOf(
                "userId" to uid,
                "appointmentId" to appointment.id,
                "doctorNameEn" to appointment.doctorNameEn,
                "doctorNameBn" to appointment.doctorNameBn,
                "specialtyEn" to appointment.specialtyEn,
                "specialtyBn" to appointment.specialtyBn,
                "bmdcRegNo" to bmdcRegNo.ifBlank { "BMDC-A-28491" },
                "patientName" to appointment.patientName,
                "patientAge" to patientAge.coerceIn(1, 120),
                "diagnosisEn" to diagnosisEn.trim().take(300),
                "diagnosisBn" to diagnosisBn.trim().take(400),
                "medicinesSummaryEn" to medicinesSummaryEn.trim().take(1000),
                "medicinesSummaryBn" to medicinesSummaryBn.trim().take(1200),
                "adviceEn" to adviceEn.trim().take(600),
                "adviceBn" to adviceBn.trim().take(800),
                "recommendedTests" to recommendedTests.trim().take(500),
                "followUpDays" to followUpDays.coerceIn(0, 365),
                "discountCouponCode" to "SHEBA-LAB20",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            rxRef.set(rxPayload).await()
            Result.success(rxRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, rxRef.path)
            Result.failure(e)
        }
    }

    // 10. Trigger Instant 100% Zero-Deduction Doctor No-Show / Delay Escrow Refund
    // Solves Play Store Complaint #1: Waiting 5-14 working days and losing bKash cash-out fees when a doctor no-shows
    suspend fun triggerDoctorNoShowInstantRefund(
        appointment: Appointment,
        reasonSummary: String = "Doctor No-Show / Emergency OT Delay — Instant 100% Patient Escrow Refund"
    ): Result<String> {
        val uid = requireUserId()
        require(appointment.id.isNotBlank()) { "Appointment ID must not be blank" }
        val apptRef = db.collection("users").document(uid)
            .collection("appointments").document(appointment.id)
        val claimRef = db.collection("users").document(uid)
            .collection("refund_claims").document()

        return try {
            apptRef.update(
                mapOf(
                    "status" to "CANCELLED",
                    "estimatedWaitMinutes" to 0,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            val refundRefId = "RFND-${appointment.paymentMethod.take(3)}-${System.currentTimeMillis().toString().takeLast(6)}"
            val claimPayload = mapOf(
                "userId" to uid,
                "appointmentId" to appointment.id,
                "doctorNameEn" to appointment.doctorNameEn,
                "doctorNameBn" to appointment.doctorNameBn,
                "claimType" to "DOCTOR_NO_SHOW",
                "paymentMethod" to appointment.paymentMethod,
                "originalTrxId" to appointment.paymentTrxId.ifBlank { "TRX-ESCROW" },
                "refundReferenceId" to refundRefId,
                "refundAmountBdt" to appointment.feeBdt.coerceIn(0, 50000),
                "deductionFeeBdt" to 0, // Strictly 0 BDT gateway deduction
                "reasonSummary" to reasonSummary.trim().ifBlank { "Doctor No-Show Instant Refund" }.take(500),
                "resolutionStatus" to "INSTANT_REFUNDED",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            claimRef.set(claimPayload).await()
            Result.success(refundRefId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, claimRef.path)
            Result.failure(e)
        }
    }

    // 11. Reconcile Stuck bKash / Nagad Gateway Timeout TrxID
    // Solves Play Store Complaint #2: Money deducted from MFS wallet on slow network without confirming serial
    suspend fun reconcileStuckMfsTransaction(
        paymentMethod: String,
        originalTrxId: String,
        deductedAmountBdt: Int,
        doctorNameEn: String,
        doctorNameBn: String,
        notes: String
    ): Result<String> {
        val uid = requireUserId()
        val claimRef = db.collection("users").document(uid)
            .collection("refund_claims").document()
        val refundRefId = "RECOV-${paymentMethod.take(3)}-${System.currentTimeMillis().toString().takeLast(6)}"
        val payload = mapOf(
            "userId" to uid,
            "appointmentId" to "TRX_${originalTrxId.replace(Regex("[^a-zA-Z0-9_\\-]"), "").ifBlank { "MFS" }}",
            "doctorNameEn" to doctorNameEn.ifBlank { "MFS Gateway Verification" }.take(150),
            "doctorNameBn" to doctorNameBn.ifBlank { "বিকাশ/নগদ পেমেন্ট রিকভারি" }.take(200),
            "claimType" to "STUCK_MFS_TRX",
            "paymentMethod" to paymentMethod,
            "originalTrxId" to originalTrxId.trim().ifBlank { "BKX-TIMEOUT" }.take(80),
            "refundReferenceId" to refundRefId,
            "refundAmountBdt" to deductedAmountBdt.coerceIn(100, 50000),
            "deductionFeeBdt" to 0,
            "reasonSummary" to notes.trim().ifBlank { "MFS network timeout auto-reconciled with 0 BDT deduction" }.take(500),
            "resolutionStatus" to "INSTANT_REFUNDED",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            claimRef.set(payload).await()
            Result.success(refundRefId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, claimRef.path)
            Result.failure(e)
        }
    }

    // 12. Cancel Appointment
    suspend fun cancelAppointment(appointment: Appointment): Result<Unit> {
        val uid = requireUserId()
        require(appointment.id.isNotBlank()) { "Appointment ID must not be blank" }
        val docRef = db.collection("users").document(uid)
            .collection("appointments").document(appointment.id)
        return try {
            docRef.update(
                mapOf(
                    "status" to "CANCELLED",
                    "estimatedWaitMinutes" to 0,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    // 13. Add Family Member Profile
    suspend fun addFamilyMember(
        fullName: String,
        relationEn: String,
        relationBn: String,
        age: Int,
        gender: String,
        bloodGroup: String,
        chronicConditions: String,
        phone: String
    ): Result<String> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
            .collection("family_members").document()
        val payload = mapOf(
            "userId" to uid,
            "fullName" to fullName.trim().ifBlank { "Family Member" }.take(120),
            "relationEn" to relationEn.trim().ifBlank { "Family" }.take(60),
            "relationBn" to relationBn.trim().ifBlank { "পরিবার" }.take(100),
            "age" to age.coerceIn(0, 130),
            "gender" to gender.trim().ifBlank { "Other" }.take(40),
            "bloodGroup" to bloodGroup.trim().ifBlank { "O+" }.take(20),
            "chronicConditions" to chronicConditions.trim().take(300),
            "phone" to phone.trim().take(30),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    // 14. Delete Family Member Profile
    suspend fun deleteFamilyMember(memberId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
            .collection("family_members").document(memberId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }
}

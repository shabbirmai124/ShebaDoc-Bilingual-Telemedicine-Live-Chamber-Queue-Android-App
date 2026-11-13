package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.BangladeshMedicalCatalog
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShebaRepositoryRuleTest : FirestoreEmulatorTestBase() {

    private val sampleDoctor = BangladeshMedicalCatalog.doctors.first()

    @Test
    fun bookAppointment_validPayload_createsDocumentAndEmitsInFlow() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = ShebaRepository(firestore)

        val bookResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.bookAppointment(
                doctor = sampleDoctor,
                patientName = "Rahim Mia",
                patientRelation = "Father",
                appointmentDate = "2026-11-18",
                timeSlot = "06:30 PM - 07:30 PM",
                consultationType = "CHAMBER",
                isFollowUp = true,
                paymentMethod = "BKASH",
                paymentTrxId = "BKX774190",
                symptomsSummary = "Mild chest discomfort and BP check",
                vitalsBp = "130/85 mmHg",
                vitalsTempF = "98.4 °F"
            )
        }
        assertTrue(bookResult.isSuccess)
        val appointmentId = bookResult.getOrThrow()
        assertTrue(appointmentId.isNotBlank())

        val emittedAppointments = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeAppointments(aliceUid).first { list -> list.any { it.id == appointmentId } }
        }
        val createdAppt = emittedAppointments.first { it.id == appointmentId }
        assertEquals(sampleDoctor.followUpFeeBdt, createdAppt.feeBdt)
        assertEquals("IN_QUEUE", createdAppt.status)
    }

    @Test
    fun triggerDoctorNoShowInstantRefund_and_reconcileStuckMfsTransaction_createZeroDeductionClaims() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = ShebaRepository(firestore)

        val appointmentId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.bookAppointment(
                doctor = sampleDoctor,
                patientName = "Karim Uddin",
                patientRelation = "Self",
                appointmentDate = "2026-11-19",
                timeSlot = "05:30 PM - 06:30 PM",
                consultationType = "VIDEO",
                isFollowUp = false,
                paymentMethod = "BKASH",
                paymentTrxId = "BKX881204",
                symptomsSummary = "High BP follow up",
                vitalsBp = "140/90 mmHg",
                vitalsTempF = "98.6 °F"
            ).getOrThrow()
        }

        val fetchedAppt = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getAppointmentById(aliceUid, appointmentId).getOrThrow()
        }
        assertNotNull(fetchedAppt)

        val refundResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.triggerDoctorNoShowInstantRefund(fetchedAppt!!)
        }
        assertTrue("Refund failed: ${refundResult.exceptionOrNull()}", refundResult.isSuccess)

        val mfsRecoveryResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.reconcileStuckMfsTransaction(
                paymentMethod = "NAGAD",
                originalTrxId = "NGD990123",
                deductedAmountBdt = 1000,
                doctorNameEn = sampleDoctor.nameEn,
                doctorNameBn = sampleDoctor.nameBn,
                notes = "Network timeout during Nagad checkout"
            )
        }
        assertTrue("MFS Recovery failed: ${mfsRecoveryResult.exceptionOrNull()}", mfsRecoveryResult.isSuccess)

        val claims = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeRefundClaims(aliceUid).first { it.size >= 2 }
        }
        assertTrue(claims.all { it.deductionFeeBdt == 0 })
    }

    @Test
    fun getAppointmentById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = ShebaRepository(firestore)
        val appointmentId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.bookAppointment(
                doctor = sampleDoctor,
                patientName = "Alice Patient",
                patientRelation = "Self",
                appointmentDate = "2026-11-20",
                timeSlot = "08:00 PM - 09:00 PM",
                consultationType = "CHAMBER",
                isFollowUp = false,
                paymentMethod = "BKASH",
                paymentTrxId = "BKX990112",
                symptomsSummary = "Checkup",
                vitalsBp = "120/80 mmHg",
                vitalsTempF = "98.6 °F"
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = ShebaRepository(firestore)
        var thrownException: FirebaseFirestoreException? = null
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                bobRepo.getAppointmentById(aliceUid, appointmentId)
            }
        } catch (e: Exception) {
            thrownException = e.unwrapFirestoreException() ?: (e as? FirebaseFirestoreException)
        }
        assertNotNull("Expected FirebaseFirestoreException for cross-user access", thrownException)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, thrownException?.code)
    }

    @Test
    fun observeAppointments_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        auth.signOut()
        val repository = ShebaRepository(firestore)

        var thrownException: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeAppointments(aliceUid).first()
            }
        } catch (e: Throwable) {
            thrownException = e
        }
        assertNotNull("Expected exception when observing while signed out", thrownException)
        val firestoreException = checkNotNull(thrownException?.unwrapFirestoreException()) {
            "Expected nested FirebaseFirestoreException, got $thrownException"
        }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
    }

    private companion object {
        const val ALICE_EMAIL = "alice_bd@test.com"
        const val BOB_EMAIL = "bob_bd@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}

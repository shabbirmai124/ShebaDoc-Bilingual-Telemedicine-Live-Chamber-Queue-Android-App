/**
 * ShebaDoc (সেবাডক) - Domain Models, Bilingual Engine & Bangladesh Specialist Catalog
 * Made by Md Shabbir Mia
 */
package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

enum class AppLanguage(val code: String, val label: String) {
    EN("en", "EN"),
    BN("bn", "বাংলা")
}

fun tr(lang: AppLanguage, en: String, bn: String): String =
    if (lang == AppLanguage.BN) bn else en

fun String.toBanglaDigits(): String {
    val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    val sb = StringBuilder(this.length)
    for (ch in this) {
        if (ch in '0'..'9') {
            sb.append(bnDigits[ch - '0'])
        } else {
            sb.append(ch)
        }
    }
    return sb.toString()
}

fun Int.formatForLang(lang: AppLanguage): String =
    if (lang == AppLanguage.BN) this.toString().toBanglaDigits() else this.toString()

fun formatBdt(amount: Int, lang: AppLanguage): String =
    if (lang == AppLanguage.BN) "৳${amount.toString().toBanglaDigits()}" else "৳$amount"

@IgnoreExtraProperties
data class Appointment(
    @DocumentId val id: String = "",
    val userId: String = "",
    val doctorId: String = "",
    val doctorNameEn: String = "",
    val doctorNameBn: String = "",
    val specialtyEn: String = "",
    val specialtyBn: String = "",
    val hospitalNameEn: String = "",
    val hospitalNameBn: String = "",
    val patientName: String = "",
    val patientRelation: String = "Self",
    val appointmentDate: String = "",
    val timeSlot: String = "",
    val consultationType: String = "CHAMBER", // CHAMBER, VIDEO, AUDIO_LOW_DATA
    val serialNumber: Int = 1,
    val currentRunningSerial: Int = 1,
    val estimatedWaitMinutes: Int = 15,
    val chamberStatus: String = "IN_CHAMBER", // ON_TIME, IN_CHAMBER, DELAYED_20M, IN_OT
    val feeBdt: Int = 1000,
    @get:PropertyName("isFollowUp") val isFollowUp: Boolean = false,
    val paymentMethod: String = "BKASH", // BKASH, NAGAD, CHAMBER_CASH, CARD
    val paymentTrxId: String = "",
    val symptomsSummary: String = "",
    val vitalsBp: String = "120/80 mmHg",
    val vitalsTempF: String = "98.6 °F",
    val status: String = "IN_QUEUE", // UPCOMING, IN_QUEUE, COMPLETED, CANCELLED
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Prescription(
    @DocumentId val id: String = "",
    val userId: String = "",
    val appointmentId: String = "",
    val doctorNameEn: String = "",
    val doctorNameBn: String = "",
    val specialtyEn: String = "",
    val specialtyBn: String = "",
    val bmdcRegNo: String = "",
    val patientName: String = "",
    val patientAge: Int = 30,
    val diagnosisEn: String = "",
    val diagnosisBn: String = "",
    val medicinesSummaryEn: String = "",
    val medicinesSummaryBn: String = "",
    val adviceEn: String = "",
    val adviceBn: String = "",
    val recommendedTests: String = "",
    val followUpDays: Int = 14,
    val discountCouponCode: String = "SHEBA-LAB20",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FamilyMember(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val relationEn: String = "Self",
    val relationBn: String = "নিজে",
    val age: Int = 30,
    val gender: String = "Male",
    val bloodGroup: String = "O+",
    val chronicConditions: String = "None",
    val phone: String = "+8801700000000",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class RefundClaim(
    @DocumentId val id: String = "",
    val userId: String = "",
    val appointmentId: String = "",
    val doctorNameEn: String = "",
    val doctorNameBn: String = "",
    val claimType: String = "DOCTOR_NO_SHOW", // DOCTOR_NO_SHOW, STUCK_MFS_TRX, CALL_DROP_COMPENSATION, CHAMBER_OVERDELAY
    val paymentMethod: String = "BKASH",
    val originalTrxId: String = "",
    val refundReferenceId: String = "",
    val refundAmountBdt: Int = 1000,
    val deductionFeeBdt: Int = 0, // Strictly 0 BDT gateway fee deduction
    val reasonSummary: String = "",
    val resolutionStatus: String = "INSTANT_REFUNDED", // INSTANT_REFUNDED, SERIAL_RESTORED, FREE_RESLOT_ISSUED
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class GenericMedicineItem(
    val id: String,
    val genericNameEn: String,
    val genericNameBn: String,
    val strength: String,
    val indicationEn: String,
    val indicationBn: String,
    val unitPriceBdt: Int,
    val topBrandsBd: List<String> // Square, Incepta, Beximco, Renata, Healthcare
)

object PatientLoginCredentialSession {
    var loginMethod: String = "GOOGLE_CLOUD" // GOOGLE_CLOUD, PHONE_PASSWORD, EMAIL_PHONE_PASSWORD
    var linkedPhone: String = "+8801711246890"
    var linkedEmail: String = "patient.bd@shebadoc.com"
    var passwordMasked: String = "••••••••"
}

data class DoctorProfile(
    val id: String,
    val nameEn: String,
    val nameBn: String,
    val specialtyCategory: String,
    val specialtyEn: String,
    val specialtyBn: String,
    val degreesEn: String,
    val degreesBn: String,
    val designationEn: String,
    val designationBn: String,
    val hospitalEn: String,
    val hospitalBn: String,
    val chamberRoomEn: String,
    val chamberRoomBn: String,
    val cityEn: String,
    val cityBn: String,
    val bmdcRegNo: String,
    val bmdcVerifiedYear: Int,
    val isBmdcVerified: Boolean = true,
    val bmdcWarningNoteEn: String = "Verified active specialist on official BMDC registry.",
    val bmdcWarningNoteBn: String = "বিএমডিসি অফিশিয়াল রেজিস্ট্রিতে সক্রিয় ও যাচাইকৃত বিশেষজ্ঞ।",
    val experienceYears: Int,
    val rating: Double,
    val totalPatientsServed: Int,
    val newVisitFeeBdt: Int,
    val followUpFeeBdt: Int,
    val currentRunningSerial: Int,
    val nextAvailableSerial: Int,
    val avgMinutesPerPatient: Int,
    val chamberStatus: String,
    val availableTimeSlots: List<String>,
    val supportsLowDataAudio: Boolean = true,
    val symptomKeywords: List<String>,
    val defaultDiagnosisEn: String,
    val defaultDiagnosisBn: String,
    val defaultRxEn: String,
    val defaultRxBn: String,
    val defaultAdviceEn: String,
    val defaultAdviceBn: String,
    val defaultTests: String
)

data class SymptomTriageOption(
    val id: String,
    val titleEn: String,
    val titleBn: String,
    val subtitleEn: String,
    val subtitleBn: String,
    val targetCategory: String,
    val urgencyLevelEn: String,
    val urgencyLevelBn: String
)

data class SolvedPainPoint(
    val number: String,
    val appSourceTagEn: String,
    val appSourceTagBn: String,
    val problemTitleEn: String,
    val problemTitleBn: String,
    val solutionTitleEn: String,
    val solutionTitleBn: String,
    val detailEn: String,
    val detailBn: String,
    val actionLabelEn: String,
    val actionLabelBn: String,
    val targetTab: Int // 0 = Home, 1 = Live Queue & Escrow, 2 = Rx & Generic Finder, 3 = Family & TrxID Protection
)

object BangladeshMedicalCatalog {

    val dgdaGenericMedicines = listOf(
        GenericMedicineItem(
            id = "gen_esomeprazole",
            genericNameEn = "Esomeprazole Magnesium",
            genericNameBn = "ইসোমিপ্রাজল ম্যাগনেসিয়াম",
            strength = "20 mg Cap/Tab",
            indicationEn = "Gastric Ulcer, GERD & Acidity (30m before meal)",
            indicationBn = "গ্যাস্ট্রিক আলসার ও বুক জ্বালাপোড়া (খাবার ৩০ মিনিট আগে)",
            unitPriceBdt = 7,
            topBrandsBd = listOf("Maxpro (Renata) - ৳7", "Sergel (Healthcare) - ৳7", "Nexum (Square) - ৳7", "Esonix (Incepta) - ৳7")
        ),
        GenericMedicineItem(
            id = "gen_bisoprolol",
            genericNameEn = "Bisoprolol Fumarate",
            genericNameBn = "বিসোপ্রোলল ফিউমারেট",
            strength = "2.5 mg Tab",
            indicationEn = "Hypertension, Palpitation & Angina",
            indicationBn = "উচ্চ রক্তচাপ ও বুক ধড়ফড় নিয়ন্ত্রণে",
            unitPriceBdt = 6,
            topBrandsBd = listOf("Bislol (Square) - ৳6", "Concor (Healthcare) - ৳8", "Betabis (Incepta) - ৳6", "Bipro (Beximco) - ৳6")
        ),
        GenericMedicineItem(
            id = "gen_amlodipine_telmisartan",
            genericNameEn = "Amlodipine + Telmisartan",
            genericNameBn = "অ্যামলোডিপিন + টেলমিসারটান",
            strength = "5/40 mg Tab",
            indicationEn = "Stage I/II High Blood Pressure Control",
            indicationBn = "উচ্চ রক্তচাপ (প্রেসার) নিয়ন্ত্রণে",
            unitPriceBdt = 16,
            topBrandsBd = listOf("Amdocal Plus / Telmilok (Square) - ৳16", "Coralcal/Telsartan-A (Incepta) - ৳16", "arpin-T (Beximco) - ৳16")
        ),
        GenericMedicineItem(
            id = "gen_rosuvastatin",
            genericNameEn = "Rosuvastatin Calcium",
            genericNameBn = "রসুভাস্ট্যাটিন ক্যালসিয়াম",
            strength = "10 mg Tab",
            indicationEn = "High Cholesterol & Heart Attack Prevention",
            indicationBn = "রক্তের কোলেস্টেরল কমাতে ও হৃদরোগ প্রতিরোধে",
            unitPriceBdt = 18,
            topBrandsBd = listOf("Rostil (Square) - ৳18", "Rosuva (Incepta) - ৳18", "Rolip (Renata) - ৳18")
        ),
        GenericMedicineItem(
            id = "gen_naproxen_eso",
            genericNameEn = "Naproxen + Esomeprazole",
            genericNameBn = "ন্যাপ্রোক্সেন + ইসোমিপ্রাজল",
            strength = "500/20 mg Tab",
            indicationEn = "Severe Migraine, Back/Joint Pain with Gastric Protection",
            indicationBn = "মাইগ্রেন, কোমর ও জয়েন্টের ব্যথা (গ্যাস্ট্রিক সুরক্ষাসহ)",
            unitPriceBdt = 15,
            topBrandsBd = listOf("Xenole / Anaprox-E (Square) - ৳15", "Naprosyn-Plus (Radiant) - ৳16", "Napro-A+ (Beximco) - ৳15")
        ),
        GenericMedicineItem(
            id = "gen_bilastine",
            genericNameEn = "Bilastine (Non-Sedating)",
            genericNameBn = "বিলাস্টিন (ঘুম ভাব হয় না)",
            strength = "20 mg Tab",
            indicationEn = "Allergic Rhinitis, Skin Urticaria & Itching",
            indicationBn = "সর্দি-অ্যালার্জি ও চুলকানি (ঝিমুনি ছাড়া)",
            unitPriceBdt = 12,
            topBrandsBd = listOf("Bilastin (Square) - ৳12", "Belasin (Incepta) - ৳12", "Bilan (Beximco) - ৳12")
        )
    )

    val symptomTriageOptions = listOf(
        SymptomTriageOption(
            id = "sym_chest",
            titleEn = "Chest Pressure, Palpitation or High BP",
            titleBn = "বুক ধড়ফড়, বুকে চাপ বা উচ্চ রক্তচাপ",
            subtitleEn = "Matches Cardiology & Heart Specialists",
            subtitleBn = "হৃদরোগ ও কার্ডিওলজি বিশেষজ্ঞ",
            targetCategory = "CARDIOLOGY",
            urgencyLevelEn = "Priority Chamber / Video",
            urgencyLevelBn = "অগ্রাধিকার চেম্বার / ভিডিও"
        ),
        SymptomTriageOption(
            id = "sym_fever_gastric",
            titleEn = "Fever, Gastric Ulcer, Diabetes or Weakness",
            titleBn = "জ্বর, গ্যাস্ট্রিক, ডায়াবেটিস বা শারীরিক দুর্বলতা",
            subtitleEn = "Matches Internal Medicine & Gastroenterology",
            subtitleBn = "মেডিসিন ও পরিপাকতন্ত্র বিশেষজ্ঞ",
            targetCategory = "MEDICINE",
            urgencyLevelEn = "Same-Day Serial Available",
            urgencyLevelBn = "আজই সিরিয়াল উপলব্ধ"
        ),
        SymptomTriageOption(
            id = "sym_maternal",
            titleEn = "Pregnancy Care, PCOS or Women's Health",
            titleBn = "গর্ভকালীন সেবা, পিসিওএস বা প্রসূতি স্বাস্থ্য",
            subtitleEn = "Matches Female Obs & Gynecology Specialists",
            subtitleBn = "গাইনী ও প্রসূতি রোগ বিশেষজ্ঞ",
            targetCategory = "GYNECOLOGY",
            urgencyLevelEn = "Private Female Specialist",
            urgencyLevelBn = "মহিলা বিশেষজ্ঞ ডাক্তার"
        ),
        SymptomTriageOption(
            id = "sym_child",
            titleEn = "Child Cough, Cold, Nutrition or Vaccination",
            titleBn = "শিশুর সর্দি-কাশি, জ্বর বা পুষ্টি সমস্যা",
            subtitleEn = "Matches Pediatrics & Neonatology",
            subtitleBn = "শিশু ও নবজাতক রোগ বিশেষজ্ঞ",
            targetCategory = "PEDIATRICS",
            urgencyLevelEn = "Fast-Track Child Queue",
            urgencyLevelBn = "শিশুদের দ্রুত সিরিয়াল"
        ),
        SymptomTriageOption(
            id = "sym_neuro",
            titleEn = "Severe Migraine, Vertigo, Stroke or Nerve Pain",
            titleBn = "তীব্র মাথাব্যথা, মাথা ঘোরা বা স্নায়ু ব্যথা",
            subtitleEn = "Matches Neuromedicine & Brain Specialists",
            subtitleBn = "নিউরোমেডিসিন ও মস্তিষ্ক বিশেষজ্ঞ",
            targetCategory = "NEUROLOGY",
            urgencyLevelEn = "Specialist Chamber",
            urgencyLevelBn = "বিশেষজ্ঞ চেম্বার"
        ),
        SymptomTriageOption(
            id = "sym_ortho",
            titleEn = "Back Pain, Knee Arthritis or Bone Injury",
            titleBn = "কোমর ব্যথা, হাঁটু ব্যথা বা হাড়ের সমস্যা",
            subtitleEn = "Matches Orthopedics & Spine Surgery",
            subtitleBn = "হাড় ও জয়েন্ট বিশেষজ্ঞ (অর্থোপেডিকস)",
            targetCategory = "ORTHOPEDICS",
            urgencyLevelEn = "Chamber & X-Ray Review",
            urgencyLevelBn = "চেম্বার ও এক্স-রে রিভিউ"
        ),
        SymptomTriageOption(
            id = "sym_skin",
            titleEn = "Skin Allergy, Fungal Infection or Hair Fall",
            titleBn = "চর্মরোগ, অ্যালার্জি, চুলকানি বা চুল পড়া",
            subtitleEn = "Matches Dermatology & Venereology",
            subtitleBn = "চর্ম ও অ্যালার্জি বিশেষজ্ঞ",
            targetCategory = "DERMATOLOGY",
            urgencyLevelEn = "HD Photo + Audio/Video",
            urgencyLevelBn = "এইচডি ছবি + ভিডিও কল"
        )
    )

    val solvedPainPoints = listOf(
        SolvedPainPoint(
            number = "01",
            appSourceTagEn = "PLAY STORE REVIEW COMPLAINT #1 (DocTime / Sebaghar)",
            appSourceTagBn = "প্লে-স্টোর রিভিউ অভিযোগ #১ (ডকটাইম / সেবাঘর)",
            problemTitleEn = "Doctor 'No-Show' After Payment & 5–14 Days bKash Refund Delay with Fee Deduction",
            problemTitleBn = "ফি দেওয়ার পর ডাক্তার অনলাইনে না আসা এবং বিকাশ রিফান্ড পেতে ৫-১৪ দিন অপেক্ষা ও চার্জ কর্তন",
            solutionTitleEn = "1-Tap Instant 100% Escrow Refund (৳0 Deduction Guarantee)",
            solutionTitleBn = "১ ক্লিকে তাৎক্ষণিক ১০০% এসক্রো রিফান্ড (৳০ চার্জ কর্তন গ্যারান্টি)",
            detailEn = "Other apps make you email support and wait 3–14 working days while deducting bKash cash-out fees. ShebaDoc holds your fee in Patient Escrow and lets you trigger an instant 100% refund with ৳0 deduction if a doctor is delayed or absent.",
            detailBn = "অন্যান্য অ্যাপে ডাক্তার না আসলে ইমেইল করে ৫-১৪ দিন অপেক্ষা করতে হয় এবং ক্যাশ-আউট চার্জ কেটে রাখে। সেবাডকে আপনার টাকা এসক্রোতে সুরক্ষিত থাকে এবং ১ ক্লিকে কোনো চার্জ ছাড়াই ১০০% তাৎক্ষণিক রিফান্ড পাওয়া যায়।",
            actionLabelEn = "Test Instant Refund in Queue",
            actionLabelBn = "লাইভ কিউতে তাৎক্ষণিক রিফান্ড দেখুন",
            targetTab = 1
        ),
        SolvedPainPoint(
            number = "02",
            appSourceTagEn = "PLAY STORE REVIEW COMPLAINT #2 (MFS Gateway Timeout)",
            appSourceTagBn = "প্লে-স্টোর রিভিউ অভিযোগ #২ (বিকাশ/নগদ পেমেন্ট টাইমআউট)",
            problemTitleEn = "Money Deducted from bKash/Nagad on Slow Network, But Serial Not Booked",
            problemTitleBn = "নেটওয়ার্ক সমস্যায় বিকাশ/নগদ থেকে টাকা কেটে নিয়েছে কিন্তু সিরিয়াল কনফার্ম হয়নি",
            solutionTitleEn = "Instant MFS TrxID Self-Reconciliation & Recovery Tool",
            solutionTitleBn = "বিকাশ/নগদ TrxID অটো-ভেরিফায়ার ও তাৎক্ষণিক রিকভারি টুল",
            detailEn = "Never lose money to a frozen gateway screen. Enter your deducted bKash/Nagad TrxID in the Protection Hub to immediately verify and restore your payment in Firestore.",
            detailBn = "পেমেন্ট করার সময় অ্যাপ হ্যাং হয়ে টাকা কেটে গেলে কাস্টমার কেয়ারে ঘুরতে হবে না। আমাদের প্রটেকশন হাবে আপনার TrxID দিলেই তাৎক্ষণিক পেমেন্ট ভেরিফাই ও রিফান্ড/সিরিয়াল রিকভার হবে।",
            actionLabelEn = "Open TrxID Recovery Tool",
            actionLabelBn = "TrxID রিকভারি টুল খুলুন",
            targetTab = 3
        ),
        SolvedPainPoint(
            number = "03",
            appSourceTagEn = "PLAY STORE REVIEW COMPLAINT #3 (Hospital Chamber Portals)",
            appSourceTagBn = "প্লে-স্টোর রিভিউ অভিযোগ #৩ (হাসপাতাল চেম্বার সিরিয়াল)",
            problemTitleEn = "3–4 Hours Blind Waiting in Crowded Chambers Without Knowing Running Serial",
            problemTitleBn = "চেম্বারে কত নম্বর সিরিয়াল চলছে না জেনে ৩-৪ ঘণ্টা অন্ধ অপেক্ষা",
            solutionTitleEn = "Live Chamber Serial Tracker (#11 → #15) & Doctor Room Status",
            solutionTitleBn = "লাইভ চেম্বার সিরিয়াল ট্র্যাকার (#১১ → #১৫) ও ডাক্তারের অবস্থান",
            detailEn = "Know whether the doctor is Sitting in Chamber, On Time, Delayed 20m, or in an Emergency OT before leaving home.",
            detailBn = "বাসা থেকে বের হওয়ার আগেই দেখুন ডাক্তার চেম্বারে আছেন নাকি ওটিতে এবং বর্তমানে কত নম্বর সিরিয়াল চলছে।",
            actionLabelEn = "Open Live Serial Queue",
            actionLabelBn = "লাইভ সিরিয়াল ট্র্যাকার দেখুন",
            targetTab = 1
        ),
        SolvedPainPoint(
            number = "04",
            appSourceTagEn = "PLAY STORE REVIEW COMPLAINT #4 (Rushed 2-Min Calls & Call Drops)",
            appSourceTagBn = "প্লে-স্টোর রিভিউ অভিযোগ #৪ (২ মিনিটে কল শেষ ও দুর্বল নেটওয়ার্ক)",
            problemTitleEn = "Doctors Rush Through Calls in 2 Minutes or Video Drops on 3G Mobile Data",
            problemTitleBn = "ডাক্তার মাত্র ২ মিনিটে কথা শেষ করেন কিংবা দুর্বল নেটওয়ার্কে ভিডিও কল কেটে যায়",
            solutionTitleEn = "10-Minute Guaranteed Consultation Room + 32kbps Rural Audio Mode",
            solutionTitleBn = "১০ মিনিটের গ্যারান্টিযুক্ত কনসালটেশন রুম + ৩২kbps লো-ডাটা অডিও মোড",
            detailEn = "Includes a live 10-minute SLA timer, pre-synced BP/Temp vitals so no time is wasted, and a 1-tap switch to low-bandwidth audio.",
            detailBn = "কনসালটেশন রুমে রয়েছে ১০ মিনিটের গ্যারান্টিযুক্ত টাইমার, আগে থেকেই পাঠানো প্রেশার ও লক্ষণের তালিকা এবং দুর্বল ইন্টারনেটের জন্য লো-ডাটা অডিও মোড।",
            actionLabelEn = "Launch Consultation Room in Queue",
            actionLabelBn = "কনসালটেশন রুম দেখুন",
            targetTab = 1
        ),
        SolvedPainPoint(
            number = "05",
            appSourceTagEn = "PLAY STORE REVIEW COMPLAINT #5 (Pharmacy & Prescription Issues)",
            appSourceTagBn = "প্লে-স্টোর রিভিউ অভিযোগ #৫ (ওষুধের দাম ও অস্পষ্ট প্রেসক্রিপশন)",
            problemTitleEn = "Expensive Out-of-Stock Medicine Brands & Missing Bangla Dosage Instructions",
            problemTitleBn = "প্রেসক্রিপশনে দামি বা দুষ্প্রাপ্য ব্র্যান্ডের ওষুধ এবং অস্পষ্ট খাওয়ার নিয়ম",
            solutionTitleEn = "Bilingual Digital Rx (১+০+১) + DGDA Generic Price & Brand Comparator",
            solutionTitleBn = "বাংলা ডিজিটাল ই-প্রেসক্রিপশন (১+০+১) + জেনেরিক ওষুধের দাম ও বিকল্প ব্র্যান্ড",
            detailEn = "Every prescription includes the exact Generic Name and our built-in DGDA Generic Price Finder shows Square, Incepta, Beximco, and Renata alternatives with exact unit prices.",
            detailBn = "প্রতিটি প্রেসক্রিপশনে ওষুধের জেনেরিক নাম থাকে এবং অ্যাপের জেনেরিক প্রাইস ফাইন্ডারে স্কয়ার, ইনসেপ্টা, বেক্সিমকো ও রেনাটার বিকল্প ব্র্যান্ড ও সঠিক দাম দেখা যায়।",
            actionLabelEn = "Open Rx & Generic Price Finder",
            actionLabelBn = "জেনেরিক ওষুধের তালিকা ও Rx দেখুন",
            targetTab = 2
        ),
        SolvedPainPoint(
            number = "06",
            appSourceTagEn = "PLAY STORE REVIEW COMPLAINT #6 (Report Show & Fake Degrees)",
            appSourceTagBn = "প্লে-স্টোর রিভিউ অভিযোগ #৬ (রিপোর্ট দেখানোর ফি ও ভুয়া ডিগ্রি)",
            problemTitleEn = "Charged Full Fee Again Just to Show Lab Reports + Unverified Doctor Credentials",
            problemTitleBn = "ল্যাব টেস্টের রিপোর্ট দেখাতে গিয়ে পুনরায় পুরো ফি আদায় এবং ভুয়া ডাক্তার",
            solutionTitleEn = "50% Report-Show Fee Lock + BMDC Certificate Inspector",
            solutionTitleBn = "রিপোর্ট দেখানোয় ৫০% ফি ছাড় + বিএমডিসি সনদ যাচাই",
            detailEn = "Lock in 50% discount for follow-up/report visits within 30 days and inspect every doctor's official BMDC Registration Certificate.",
            detailBn = "৩০ দিনের মধ্যে রিপোর্ট দেখানো বা ফলো-আপে ৫০% ফি ছাড় এবং ১ ক্লিকে বিএমডিসি রেজিস্ট্রেশন সনদ যাচাই।",
            actionLabelEn = "Verify Doctors & Book",
            actionLabelBn = "ডাক্তার যাচাই ও বুকিং করুন",
            targetTab = 0
        )
    )

    val doctors = listOf(
        DoctorProfile(
            id = "doc_cardio_1",
            nameEn = "Prof. Dr. A. K. M. Fazlul Haque",
            nameBn = "অধ্যাপক ডাঃ এ. কে. এম. ফজলুল হক",
            specialtyCategory = "CARDIOLOGY",
            specialtyEn = "Cardiology & Internal Medicine",
            specialtyBn = "হৃদরোগ ও মেডিসিন বিশেষজ্ঞ",
            degreesEn = "MBBS (DMC), FCPS (Medicine), MD (Cardiology - NICVD), FRCP (Edin)",
            degreesBn = "এমবিবিএস (ডিএমসি), এফসিপিএস (মেডিসিন), এমডি (কার্ডিওলজি), এফআরসিপি",
            designationEn = "Senior Consultant & Head of Interventional Cardiology",
            designationBn = "সিনিয়র কনসালটেন্ট ও বিভাগীয় প্রধান, কার্ডিওলজি বিভাগ",
            hospitalEn = "Square Hospital, Panthapath, Dhaka",
            hospitalBn = "স্কয়ার হাসপাতাল, পান্থপথ, ঢাকা",
            chamberRoomEn = "Level 4, Room 412 (Main Building)",
            chamberRoomBn = "লেভেল ৪, রুম ৪১২ (মূল ভবন)",
            cityEn = "Dhaka",
            cityBn = "ঢাকা",
            bmdcRegNo = "BMDC-A-28491",
            bmdcVerifiedYear = 1998,
            experienceYears = 26,
            rating = 4.9,
            totalPatientsServed = 18420,
            newVisitFeeBdt = 1200,
            followUpFeeBdt = 600,
            currentRunningSerial = 11,
            nextAvailableSerial = 15,
            avgMinutesPerPatient = 8,
            chamberStatus = "IN_CHAMBER",
            availableTimeSlots = listOf("05:30 PM - 06:30 PM", "06:30 PM - 07:30 PM", "08:00 PM - 09:00 PM"),
            supportsLowDataAudio = true,
            symptomKeywords = listOf("chest", "bp", "heart", "palpitation", "বুক", "রক্তচাপ", "হৃদরোগ", "প্রেসার"),
            defaultDiagnosisEn = "Essential Hypertension (Stage I) & Mild Effort Angina",
            defaultDiagnosisBn = "উচ্চ রক্তচাপ (স্টেজ-১) এবং মৃদু হৃদস্পন্দন জনিত সমস্যা",
            defaultRxEn = "1. Tab. Bisoprolol 2.5mg [Generic: Bisoprolol] — 1+0+0 (After Breakfast) — 30 Days\n2. Tab. Amlodipine + Telmisartan 5/40mg — 0+0+1 (After Dinner) — 30 Days\n3. Tab. Rosuvastatin 10mg — 0+0+1 (At Night) — 30 Days",
            defaultRxBn = "১. ট্যাবলেট বিসোপ্রোলল ২.৫ মি.গ্রা. [জেনেরিক: Bisoprolol] — ১+০+০ (সকালে নাস্তার পরে) — ৩০ দিন\n২. ট্যাবলেট অ্যামলোডিপিন + টেলমিসারটান ৫/৪০ মি.গ্রা. — ০+০+১ (রাতের খাবারের পরে) — ৩০ দিন\n৩. ট্যাবলেট রসুভাস্ট্যাটিন ১০ মি.গ্রা. — ০+০+১ (রাতে শোবার আগে) — ৩০ দিন",
            defaultAdviceEn = "Restrict table salt strictly. Walk briskly 30 minutes daily. Maintain BP log chart morning & evening.",
            defaultAdviceBn = "খাবারে কাঁচা লবণ সম্পূর্ণ বর্জন করুন। প্রতিদিন ৩০ মিনিট হাঁটুন। সকাল ও রাতে রক্তচাপ মেপে চার্টে লিখে রাখুন।",
            defaultTests = "12-Lead ECG, Echocardiogram (2D/Color Doppler), Fasting Lipid Profile, Serum Creatinine"
        ),
        DoctorProfile(
            id = "doc_med_2",
            nameEn = "Prof. Dr. Mahmudur Rahman Siddiqui",
            nameBn = "অধ্যাপক ডাঃ মাহমুদুর রহমান সিদ্দিকী",
            specialtyCategory = "MEDICINE",
            specialtyEn = "Internal Medicine, Diabetes & Hepatology",
            specialtyBn = "মেডিসিন, ডায়াবেটিস ও লিভার বিশেষজ্ঞ",
            degreesEn = "MBBS (SSMC), FCPS (Medicine), MACP (USA), DEM (BIRDEM)",
            degreesBn = "এমবিবিএস (এসএসএমসি), এফসিপিএস (মেডিসিন), ডিইএম (বারডেম)",
            designationEn = "Professor of Medicine (Ex-BSMMU / PG Hospital)",
            designationBn = "অধ্যাপক, মেডিসিন বিভাগ (সাবেক বিএসএমএমইউ / পিজি হাসপাতাল)",
            hospitalEn = "Popular Diagnostic Centre, Dhanmondi-2, Dhaka",
            hospitalBn = "পপুলার ডায়াগনস্টিক সেন্টার, ধানমন্ডি-২, ঢাকা",
            chamberRoomEn = "Building 2, Level 3, Room 305",
            chamberRoomBn = "ভবন ২, তলা ৩, রুম ৩০৫",
            cityEn = "Dhaka",
            cityBn = "ঢাকা",
            bmdcRegNo = "BMDC-A-31904",
            bmdcVerifiedYear = 2001,
            experienceYears = 23,
            rating = 4.9,
            totalPatientsServed = 24150,
            newVisitFeeBdt = 1000,
            followUpFeeBdt = 500,
            currentRunningSerial = 18,
            nextAvailableSerial = 22,
            avgMinutesPerPatient = 7,
            chamberStatus = "ON_TIME",
            availableTimeSlots = listOf("04:00 PM - 05:30 PM", "06:00 PM - 07:30 PM", "08:00 PM - 09:30 PM"),
            supportsLowDataAudio = true,
            symptomKeywords = listOf("fever", "gastric", "diabetes", "weakness", "ulcer", "জ্বর", "গ্যাস্ট্রিক", "ডায়াবেটিস", "লিভার"),
            defaultDiagnosisEn = "Acid Peptic Disease (GERD) with Uncontrolled Type-2 Diabetes",
            defaultDiagnosisBn = "গ্যাস্ট্রিক আলসার (GERD) এবং অনিয়ন্ত্রিত টাইপ-২ ডায়াবেটিস",
            defaultRxEn = "1. Cap. Esomeprazole 20mg [Generic: Esomeprazole] — 1+0+1 (30 mins Before Meal) — 14 Days\n2. Tab. Domperidone 10mg — 1+1+1 (15 mins Before Meal) — 7 Days\n3. Tab. Empagliflozin + Metformin 5/500mg — 1+0+1 (After Meal) — 30 Days",
            defaultRxBn = "১. ক্যাপসুল ইসোমিপ্রাজল ২০ মি.গ্রা. [জেনেরিক: Esomeprazole] — ১+০+১ (খাবার ৩০ মিনিট আগে) — ১৪ দিন\n২. ট্যাবলেট ডমপেরিডন ১০ মি.গ্রা. — ১+১+১ (খাবার ১৫ মিনিট আগে) — ৭ দিন\n৩. ট্যাবলেট এমপাগ্লিফ্লোজিন + মেটফরমিন ৫/৫০০ মি.গ্রা. — ১+০+১ (খাবার পরে) — ৩০ দিন",
            defaultAdviceEn = "Avoid spicy/oily food and late-night dinner. Do not lie down within 2 hours after meals. Drink 2.5L water daily.",
            defaultAdviceBn = "অতিরিক্ত তৈলাক্ত ও ভাজাপোড়া খাবার পরিহার করুন। রাতের খাবার খাওয়ার অন্তত ২ ঘণ্টা পর ঘুমাতে যাবেন।",
            defaultTests = "HbA1c, Fasting Blood Sugar & 2h ABF, SGPT (ALT), USG of Whole Abdomen"
        ),
        DoctorProfile(
            id = "doc_gyn_3",
            nameEn = "Assoc. Prof. Dr. Nusrat Jahan Chowdhury",
            nameBn = "সহযোগী অধ্যাপক ডাঃ নুসরাত জাহান চৌধুরী",
            specialtyCategory = "GYNECOLOGY",
            specialtyEn = "Obstetrics, Gynecology & Infertility",
            specialtyBn = "গাইনী, প্রসূতি ও বন্ধ্যাত্ব বিশেষজ্ঞ",
            degreesEn = "MBBS (DMC), FCPS (Obs & Gynae), MS (BSMMU), Fellowship in Laparoscopy",
            degreesBn = "এমবিবিএস (ডিএমসি), এফসিপিএস (গাইনী ও অবস), এমএস (বিএসএমএমইউ)",
            designationEn = "Associate Professor & Consultant Gynecologist",
            designationBn = "সহযোগী অধ্যাপক ও কনসালটেন্ট (গাইনী বিভাগ)",
            hospitalEn = "Evercare Hospital, Bashundhara R/A, Dhaka",
            hospitalBn = "এভারকেয়ার হাসপাতাল, বসুন্ধরা আবাসিক এলাকা, ঢাকা",
            chamberRoomEn = "OPD Wing B, Room 208",
            chamberRoomBn = "ওপিডি উইং বি, রুম ২০৮",
            cityEn = "Dhaka",
            cityBn = "ঢাকা",
            bmdcRegNo = "BMDC-A-44219",
            bmdcVerifiedYear = 2007,
            experienceYears = 17,
            rating = 4.95,
            totalPatientsServed = 15800,
            newVisitFeeBdt = 1200,
            followUpFeeBdt = 600,
            currentRunningSerial = 7,
            nextAvailableSerial = 10,
            avgMinutesPerPatient = 10,
            chamberStatus = "IN_CHAMBER",
            availableTimeSlots = listOf("11:00 AM - 01:00 PM", "05:00 PM - 07:00 PM", "07:30 PM - 09:00 PM"),
            supportsLowDataAudio = true,
            symptomKeywords = listOf("pregnancy", "pcos", "maternal", "gynecology", "গর্ভকালীন", "গাইনী", "মহিলা"),
            defaultDiagnosisEn = "Routine Antenatal Checkup & Nutritional Iron Deficiency",
            defaultDiagnosisBn = "গর্ভকালীন নিয়মিত চেকআপ এবং আয়রন ও ক্যালসিয়াম স্বল্পতা",
            defaultRxEn = "1. Cap. Carbonyl Iron + Folic Acid + Zinc — 0+1+0 (After Lunch) — 30 Days\n2. Tab. Calcium Carbonate + Vitamin D3 500mg — 0+0+1 (After Dinner with Milk/Water) — 30 Days",
            defaultRxBn = "১. ক্যাপসুল আয়রন + ফলিক অ্যাসিড + জিংক — ০+১+০ (দুপুরের খাবারের পরে) — ৩০ দিন\n২. ট্যাবলেট ক্যালসিয়াম ৫০০ মি.গ্রা. + ভিটামিন ডি৩ — ০+০+১ (রাতের খাবারের পরে) — ৩০ দিন",
            defaultAdviceEn = "Do not take Iron and Calcium tablets at the same meal. Take adequate rest on left lateral position.",
            defaultAdviceBn = "আয়রন এবং ক্যালসিয়াম ট্যাবলেট একই সাথে খাবেন না (একটি দুপুরে, অন্যটি রাতে)। বাম কাত হয়ে বিশ্রাম নিন।",
            defaultTests = "CBC with ESR, Urine R/M/E, Anomaly Scan / USG Pregnancy Profile"
        ),
        DoctorProfile(
            id = "doc_ped_4",
            nameEn = "Dr. Tanvir Ahmed Siddique",
            nameBn = "ডাঃ তানভীর আহমেদ সিদ্দিকী",
            specialtyCategory = "PEDIATRICS",
            specialtyEn = "Pediatrics, Child Nutrition & Asthma",
            specialtyBn = "শিশু স্বাস্থ্য, পুষ্টি ও হাঁপানি বিশেষজ্ঞ",
            degreesEn = "MBBS (CMC), MD (Pediatrics - BSMMU), DCH, PGPN (Boston)",
            degreesBn = "এমবিবিএস (সিএমসি), এমডি (শিশু রোগ - বিএসএমএমইউ), ডিসিএইচ",
            designationEn = "Senior Consultant, Department of Pediatrics",
            designationBn = "সিনিয়র কনসালটেন্ট, শিশু স্বাস্থ্য বিভাগ",
            hospitalEn = "Labaid Specialized Hospital, Green Road, Dhaka",
            hospitalBn = "ল্যাবএইড স্পেশালাইজড হাসপাতাল, গ্রিন রোড, ঢাকা",
            chamberRoomEn = "Level 2, Child Care Zone, Room 214",
            chamberRoomBn = "লেভেল ২, চাইল্ড কেয়ার জোন, রুম ২১৪",
            cityEn = "Dhaka",
            cityBn = "ঢাকা",
            bmdcRegNo = "BMDC-PENDING-51208",
            bmdcVerifiedYear = 2010,
            isBmdcVerified = false,
            bmdcWarningNoteEn = "BMDC License Renewal Pending Audit — Specialist certificate currently under verification.",
            bmdcWarningNoteBn = "বিএমডিসি লাইসেন্স নবায়ন যাচাই প্রক্রিয়াধীন — বিশেষজ্ঞ সনদ এখনও চূড়ান্তভাবে ভেরিফাই হয়নি।",
            experienceYears = 14,
            rating = 4.52,
            totalPatientsServed = 12300,
            newVisitFeeBdt = 900,
            followUpFeeBdt = 450,
            currentRunningSerial = 14,
            nextAvailableSerial = 17,
            avgMinutesPerPatient = 8,
            chamberStatus = "DELAYED_20M",
            availableTimeSlots = listOf("04:30 PM - 06:00 PM", "06:30 PM - 08:30 PM"),
            supportsLowDataAudio = true,
            symptomKeywords = listOf("child", "baby", "cough", "cold", "pediatric", "শিশু", "সর্দি", "কাশি"),
            defaultDiagnosisEn = "Acute Viral Nasopharyngitis (Seasonal Child Flu) & Mild Bronchospasm",
            defaultDiagnosisBn = "শিশুদের মৌসুমী ভাইরাসজনিত সর্দি-জ্বর ও মৃদু শ্বাসকষ্ট",
            defaultRxEn = "1. Syp. Paracetamol (120mg/5ml) — 1.5 TSF thrice daily (If temp > 100°F) — 5 Days\n2. Syp. Levocetirizine (2.5mg/5ml) — 1 TSF at night after food — 7 Days\n3. Normal Saline Nasal Drops — 2 drops in each nostril 4 times daily",
            defaultRxBn = "১. সিরাপ প্যারাসিটামল (১২০ মি.গ্রা./৫ মি.লি.) — ১.৫ চামচ দিনে ৩ বার (জ্বর ১০০°ফা এর বেশি হলে) — ৫ দিন\n২. সিরাপ লেভোসেটিরিজিন — ১ চামচ রাতে খাবারের পরে — ৭ দিন\n৩. নরমাল স্যালাইন ন্যাজাল ড্রপ — ২ ফোঁটা করে উভয় নাকে দিনে ৪ বার",
            defaultAdviceEn = "Do NOT give antibiotics without doctor's advice. Keep child hydrated with warm soup, water, and breastmilk.",
            defaultAdviceBn = "ডাক্তারের পরামর্শ ছাড়া অ্যান্টিবায়োটিক খাওয়াবেন না। শিশুকে পর্যাপ্ত কুসুম গরম পানি ও তরল খাবার দিন।",
            defaultTests = "CBC with Differential Count (Only if fever persists > 4 days)"
        ),
        DoctorProfile(
            id = "doc_neuro_5",
            nameEn = "Prof. Dr. Quazi Deen Mohammad Raza",
            nameBn = "অধ্যাপক ডাঃ কাজী দীন মোহাম্মদ রাজা",
            specialtyCategory = "NEUROLOGY",
            specialtyEn = "Neuromedicine, Stroke & Migraine Specialist",
            specialtyBn = "নিউরোমেডিসিন, স্ট্রোক ও মাইগ্রেন বিশেষজ্ঞ",
            degreesEn = "MBBS (DMC), MD (Neurology), FCPS (Medicine), Fellow in Stroke (Japan)",
            degreesBn = "এমবিবিএস (ডিএমসি), এমডি (নিউরোলজি), এফসিপিএস (মেডিসিন)",
            designationEn = "Chief Consultant, Neurosciences Centre",
            designationBn = "চিফ কনসালটেন্ট, নিউরোসায়েন্সেস সেন্টার",
            hospitalEn = "Ibn Sina Diagnostic & Imaging Center, Dhanmondi, Dhaka",
            hospitalBn = "ইবনে সিনা ডায়াগনস্টিক সেন্টার, ধানমন্ডি, ঢাকা",
            chamberRoomEn = "Level 5, Room 502",
            chamberRoomBn = "লেভেল ৫, রুম ৫০২",
            cityEn = "Dhaka",
            cityBn = "ঢাকা",
            bmdcRegNo = "BMDC-A-25980",
            bmdcVerifiedYear = 1996,
            experienceYears = 28,
            rating = 4.93,
            totalPatientsServed = 29400,
            newVisitFeeBdt = 1200,
            followUpFeeBdt = 600,
            currentRunningSerial = 5,
            nextAvailableSerial = 9,
            avgMinutesPerPatient = 9,
            chamberStatus = "IN_CHAMBER",
            availableTimeSlots = listOf("05:00 PM - 07:00 PM", "07:00 PM - 09:00 PM"),
            supportsLowDataAudio = true,
            symptomKeywords = listOf("migraine", "headache", "vertigo", "stroke", "nerve", "মাথাব্যথা", "মাইগ্রেন", "মাথা ঘোরা", "স্ট্রোক"),
            defaultDiagnosisEn = "Chronic Migraine without Aura & Cervical Muscle Spasm",
            defaultDiagnosisBn = "দীর্ঘমেয়াদী মাইগ্রেন জনিত মাথাব্যথা ও ঘাড়ের পেশির টান",
            defaultRxEn = "1. Tab. Naproxen + Esomeprazole 500/20mg — 1+0+1 (After Meal during severe headache) — 5 Days\n2. Tab. Flunarizine 5mg — 0+0+1 (At Bedtime for migraine prophylaxis) — 30 Days\n3. Tab. Tizanidine 2mg — 0+0+1 (After Dinner) — 7 Days",
            defaultRxBn = "১. ট্যাবলেট ন্যাপ্রোক্সেন + ইসোমিপ্রাজল ৫০০/২০ মি.গ্রা. — ১+০+১ (ভরা পেটে তীব্র মাথাব্যথা হলে) — ৫ দিন\n২. ট্যাবলেট ফ্লুনারিজিন ৫ মি.গ্রা. — ০+০+১ (রাতে ঘুমানোর আগে মাইগ্রেন প্রতিরোধে) — ৩০ দিন\n৩. ট্যাবলেট টিজানিডিন ২ মি.গ্রা. — ০+০+১ (রাতের খাবারের পরে) — ৭ দিন",
            defaultAdviceEn = "Ensure 7 hours of regular sleep. Avoid direct sunlight, fasting, and prolonged mobile screen use in dark rooms.",
            defaultAdviceBn = "প্রতিদিন অন্তত ৭ ঘণ্টা নির্দিষ্ট সময়ে ঘুমান। রোদ, দীর্ঘক্ষণ না খেয়ে থাকা এবং অন্ধকারে মোবাইল ব্যবহার এড়িয়ে চলুন।",
            defaultTests = "Serum Vitamin D (25-OH), X-Ray Cervical Spine B/V"
        ),
        DoctorProfile(
            id = "doc_ortho_6",
            nameEn = "Dr. S. M. Rezaul Karim",
            nameBn = "ডাঃ এস. এম. রেজাউল করিম",
            specialtyCategory = "ORTHOPEDICS",
            specialtyEn = "Orthopedics, Trauma & Joint Replacement",
            specialtyBn = "হাড়, জয়েন্ট, বাত ও ট্রমা সার্জারি বিশেষজ্ঞ",
            degreesEn = "MBBS (CMC), MS (Orthopedics - NITOR), AO Fellow (Singapore)",
            degreesBn = "এমবিবিএস (সিএমসি), এমএস (অর্থোপেডিকস - পঙ্গু হাসপাতাল)",
            designationEn = "Senior Consultant Orthopedic Surgeon",
            designationBn = "সিনিয়র কনসালটেন্ট অর্থোপেডিক সার্জন",
            hospitalEn = "Parkview Hospital, Panchlaish, Chattogram",
            hospitalBn = "পার্কভিউ হাসপাতাল, পাঁচলাইশ, চট্টগ্রাম",
            chamberRoomEn = "Level 3, Room 310",
            chamberRoomBn = "লেভেল ৩, রুম ৩১০",
            cityEn = "Chattogram",
            cityBn = "চট্টগ্রাম",
            bmdcRegNo = "BMDC-UNVERIFIED-39812",
            bmdcVerifiedYear = 2004,
            isBmdcVerified = false,
            bmdcWarningNoteEn = "Unverified Fellowship Claim — Flagged by ShebaDoc Registry Audit (Pending Original Certificate Submission).",
            bmdcWarningNoteBn = "অযাচাইকৃত ফেলোশিপ ডিগ্রি — সেবাডক অডিট কর্তৃক চিহ্নিত (মূল সনদ জমা দেওয়া হয়নি)।",
            experienceYears = 20,
            rating = 4.87,
            totalPatientsServed = 14200,
            newVisitFeeBdt = 1000,
            followUpFeeBdt = 500,
            currentRunningSerial = 9,
            nextAvailableSerial = 13,
            avgMinutesPerPatient = 8,
            chamberStatus = "ON_TIME",
            availableTimeSlots = listOf("05:00 PM - 07:00 PM", "07:30 PM - 09:30 PM"),
            supportsLowDataAudio = true,
            symptomKeywords = listOf("bone", "knee", "back", "joint", "arthritis", "কোমর", "হাঁটু", "হাড়", "ব্যথা"),
            defaultDiagnosisEn = "Lumbar Spondylosis & Early Osteoarthritis of Knee",
            defaultDiagnosisBn = "কোমরের হাড় ক্ষয় (লাম্বার স্পন্ডাইলোসিস) ও হাঁটুর আর্থ্রাইটিস",
            defaultRxEn = "1. Tab. Aceclofenac 100mg [Generic: Aceclofenac] — 1+0+1 (After Meal) — 7 Days\n2. Cap. Omeprazole 20mg — 1+0+1 (Before Meal) — 7 Days\n3. Cap. Cholecalciferol (Vitamin D3) 40000 IU — 1 Cap weekly after meal — 8 Weeks",
            defaultRxBn = "১. ট্যাবলেট অ্যাসিক্লোফেনাক ১০০ মি.গ্রা. [জেনেরিক: Aceclofenac] — ১+০+১ (ভরা পেটে) — ৭ দিন\n২. ক্যাপসুল ওমিপ্রাজল ২০ মি.গ্রা. — ১+০+১ (খাবার আগে) — ৭ দিন\n৩. ক্যাপসুল ভিটামিন ডি৩ ৪০০০০ আইইউ — সপ্তাহে ১টি ক্যাপসুল ভরা পেটে — ৮ সপ্তাহ",
            defaultAdviceEn = "Use high commode. Avoid lifting heavy weights or bending forward suddenly. Apply warm compress twice daily.",
            defaultAdviceBn = "উঁচু কমোড ব্যবহার করুন। সামনে ঝুঁকে ভারী কিছু তুলবেন না। দিনে ২ বার হালকা গরম সেঁক দিন।",
            defaultTests = "X-Ray Lumbosacral Spine B/V, Serum Uric Acid, Serum Calcium"
        ),
        DoctorProfile(
            id = "doc_derm_7",
            nameEn = "Dr. Farhana Sharmin Tithi",
            nameBn = "ডাঃ ফারহানা শারমিন তিথি",
            specialtyCategory = "DERMATOLOGY",
            specialtyEn = "Dermatology, Allergy & Clinical Cosmetology",
            specialtyBn = "চর্ম, অ্যালার্জি ও ক্লিনিক্যাল ডার্মাটোলজি বিশেষজ্ঞ",
            degreesEn = "MBBS (SOMC), DDV (BSMMU), FCPS (Dermatology)",
            degreesBn = "এমবিবিএস (সিলেট ওসমানী), ডিডিভি (বিএসএমএমইউ), এফসিপিএস (চর্মরোগ)",
            designationEn = "Consultant Dermatologist",
            designationBn = "কনসালটেন্ট ডার্মাটোলজিস্ট",
            hospitalEn = "Mount Adora Hospital, Akhalia, Sylhet",
            hospitalBn = "মাউন্ট এডোরা হাসপাতাল, আখালিয়া, সিলেট",
            chamberRoomEn = "Block A, Level 2, Room 204",
            chamberRoomBn = "ব্লক এ, লেভেল ২, রুম ২০৪",
            cityEn = "Sylhet",
            cityBn = "সিলেট",
            bmdcRegNo = "BMDC-A-58390",
            bmdcVerifiedYear = 2013,
            experienceYears = 11,
            rating = 4.91,
            totalPatientsServed = 9600,
            newVisitFeeBdt = 800,
            followUpFeeBdt = 400,
            currentRunningSerial = 6,
            nextAvailableSerial = 9,
            avgMinutesPerPatient = 7,
            chamberStatus = "IN_CHAMBER",
            availableTimeSlots = listOf("04:00 PM - 06:00 PM", "06:30 PM - 08:30 PM"),
            supportsLowDataAudio = true,
            symptomKeywords = listOf("skin", "allergy", "rash", "hair", "fungal", "চর্ম", "অ্যালার্জি", "চুলকানি", "চুল"),
            defaultDiagnosisEn = "Tinea Corporis (Superficial Fungal Infection) & Allergic Contact Dermatitis",
            defaultDiagnosisBn = "ছত্রাকজনিত চর্মরোগ (দাদ) ও অ্যালার্জিক ডার্মাটায়টিস",
            defaultRxEn = "1. Tab. Bilastine 20mg [Generic: Bilastine] — 0+0+1 (1 hour before dinner) — 14 Days\n2. Luliconazole 1% Cream — Apply thin layer on affected area once at night — 14 Days\n3. Ketoconazole 2% Medicated Soap — Use during bath daily",
            defaultRxBn = "১. ট্যাবলেট বিলাস্টিন ২০ মি.গ্রা. [জেনেরিক: Bilastine] — ০+০+১ (রাতের খাবারের ১ ঘণ্টা আগে) — ১৪ দিন\n২. লুলিকোনাজল ১% ক্রিম — আক্রান্ত স্থানে রাতে ১ বার পাতলা করে লাগাবেন — ১৪ দিন\n৩. কিটোকোনাজল ২% মেডিকেটেড সাবান — প্রতিদিন গোসলে ব্যবহার করবেন",
            defaultAdviceEn = "Never use steroid-mixed creams (like Betamethasone/Clobetasol combinations) on fungal rashes. Wear loose cotton clothes.",
            defaultAdviceBn = "ফাঙ্গাল ইনফেকশন বা দাদে কখনোই স্টেরয়েড মেশানো ক্রিম ব্যবহার করবেন না। সুতি ও ঢিলেঢালা পোশাক পরিধান করুন।",
            defaultTests = "Skin Scraping for Fungal Microscopy (KOH Mount), CBC with Absolute Eosinophil Count"
        )
    )
}

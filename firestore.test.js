/**
 * ShebaDoc (সেবাডক) - Firestore Security Rules Test Suite
 * Copyright (c) 2026 Md Shabbir Mia <mdshabbirmia516@gmail.com>. All Rights Reserved.
 * Proprietary & Confidential: Authorized exclusively for Md Shabbir Mia. See /LICENSE.
 */
const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_patient_bd";
const BOB_UID = "bob_patient_bd";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validAppointmentPayload(uid, serverTimestamp) {
  return {
    userId: uid,
    doctorId: "doc_cardio_1",
    doctorNameEn: "Prof. Dr. A. K. M. Fazlul Haque",
    doctorNameBn: "অধ্যাপক ডাঃ এ. কে. এম. ফজলুল হক",
    specialtyEn: "Cardiology & Medicine",
    specialtyBn: "হৃদরোগ ও মেডিসিন বিশেষজ্ঞ",
    hospitalNameEn: "Square Hospital, Panthapath, Dhaka",
    hospitalNameBn: "স্কয়ার হাসপাতাল, পান্থপথ, ঢাকা",
    patientName: "Rahim Uddin",
    patientRelation: "Father",
    appointmentDate: "2026-11-15",
    timeSlot: "06:30 PM - 07:00 PM",
    consultationType: "CHAMBER",
    serialNumber: 14,
    currentRunningSerial: 9,
    estimatedWaitMinutes: 35,
    chamberStatus: "IN_CHAMBER",
    feeBdt: 1200,
    isFollowUp: false,
    paymentMethod: "BKASH",
    paymentTrxId: "BKX984210BD",
    symptomsSummary: "Chest tightness and mild shortness of breath for 3 days",
    vitalsBp: "135/85 mmHg",
    vitalsTempF: "98.6 F",
    status: "IN_QUEUE",
    createdAt: serverTimestamp,
    updatedAt: serverTimestamp,
  };
}

function validPrescriptionPayload(uid, serverTimestamp) {
  return {
    userId: uid,
    appointmentId: "appt_101",
    doctorNameEn: "Prof. Dr. A. K. M. Fazlul Haque",
    doctorNameBn: "অধ্যাপক ডাঃ এ. কে. এম. ফজলুল হক",
    specialtyEn: "Cardiology & Medicine",
    specialtyBn: "হৃদরোগ ও মেডিসিন বিশেষজ্ঞ",
    bmdcRegNo: "BMDC-A-28491",
    patientName: "Rahim Uddin",
    patientAge: 58,
    diagnosisEn: "Essential Hypertension & Mild Dyslipidemia",
    diagnosisBn: "উচ্চ রক্তচাপ এবং রক্তে কোলেস্টেরল বৃদ্ধি",
    medicinesSummaryEn: "1. Tab. Amlodipine 5mg (0+0+1 After Meal - 30 Days)\n2. Tab. Rosuvastatin 10mg (0+0+1 After Dinner - 30 Days)",
    medicinesSummaryBn: "১. ট্যাবলেট অ্যামলোডিপিন ৫ মি.গ্রা. (০+০+১ খাবার পরে - ৩০ দিন)\n২. ট্যাবলেট রসুভাস্ট্যাটিন ১০ মি.গ্রা. (০+০+১ রাতের খাবার পরে - ৩০ দিন)",
    adviceEn: "Avoid extra salt, walk 30 mins daily, monitor BP twice weekly.",
    adviceBn: "কাঁচা লবণ পরিহার করুন, প্রতিদিন ৩০ মিনিট হাঁটুন, সপ্তাহে ২ বার প্রেশার মাপুন।",
    recommendedTests: "Lipid Profile (Fasting), ECG 12-Lead, Serum Creatinine",
    followUpDays: 30,
    discountCouponCode: "SHEBA-LAB20",
    createdAt: serverTimestamp,
    updatedAt: serverTimestamp,
  };
}

function validFamilyMemberPayload(uid, serverTimestamp) {
  return {
    userId: uid,
    fullName: "Begum Rokeya",
    relationEn: "Mother",
    relationBn: "মা",
    age: 54,
    gender: "Female",
    bloodGroup: "B+",
    chronicConditions: "Type-2 Diabetes, Hypertension",
    phone: "+8801711002233",
    createdAt: serverTimestamp,
    updatedAt: serverTimestamp,
  };
}

function validRefundClaimPayload(uid, serverTimestamp, deductionFee = 0) {
  return {
    userId: uid,
    appointmentId: "appt_101",
    doctorNameEn: "Prof. Dr. A. K. M. Fazlul Haque",
    doctorNameBn: "অধ্যাপক ডাঃ এ. কে. এম. ফজলুল হক",
    claimType: "DOCTOR_NO_SHOW",
    paymentMethod: "BKASH",
    originalTrxId: "BKX984210BD",
    refundReferenceId: "RFND-BKX-984210",
    refundAmountBdt: 1200,
    deductionFeeBdt: deductionFee,
    reasonSummary: "Doctor delayed in emergency OT > 45m; instant 100% escrow refund triggered.",
    resolutionStatus: "INSTANT_REFUNDED",
    createdAt: serverTimestamp,
    updatedAt: serverTimestamp,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot list or read appointments", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("appointments").get());
});

test("Authenticated user: can create and list own appointments", async () => {
  const aliceCtx = testEnv.authenticatedContext(ALICE_UID);
  const aliceDb = aliceCtx.firestore();
  const now = new Date();
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("appointments")
      .doc("appt_1")
      .set(validAppointmentPayload(ALICE_UID, now))
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("appointments").get()
  );
});

test("Authenticated user: cannot read or modify another user's appointment (cross-user isolation)", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context
      .firestore()
      .collection("users")
      .doc(BOB_UID)
      .collection("appointments")
      .doc("bob_appt_1")
      .set(validAppointmentPayload(BOB_UID, now));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("appointments").doc("bob_appt_1").get()
  );
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("appointments").get()
  );
});

test("Shadow Update & Terminal State Lock: cannot add ghost fields or mutate COMPLETED appointment", async () => {
  const now = new Date();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb.collection("users").doc(ALICE_UID).collection("appointments").doc("appt_lock");

  await assertSucceeds(docRef.set(validAppointmentPayload(ALICE_UID, now)));
  await assertFails(docRef.update({ isAdminBypass: true, updatedAt: new Date() }));
  await assertSucceeds(docRef.update({ status: "COMPLETED", updatedAt: new Date() }));
  await assertFails(docRef.update({ currentRunningSerial: 14, updatedAt: new Date() }));
});

test("Authenticated user: can create digital prescriptions, family members, and 0%-deduction refund claims", async () => {
  const now = new Date();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("prescriptions")
      .doc("rx_1")
      .set(validPrescriptionPayload(ALICE_UID, now))
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("family_members")
      .doc("fam_1")
      .set(validFamilyMemberPayload(ALICE_UID, now))
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("refund_claims")
      .doc("claim_1")
      .set(validRefundClaimPayload(ALICE_UID, now, 0))
  );

  // Reject any refund claim that attempts to deduct a gateway fee > 0 BDT
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("refund_claims")
      .doc("claim_illegal_fee")
      .set(validRefundClaimPayload(ALICE_UID, now, 25))
  );
});

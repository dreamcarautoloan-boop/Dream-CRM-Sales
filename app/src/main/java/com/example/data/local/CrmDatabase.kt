package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CallLogEntity
import com.example.data.model.CustomerEntity
import com.example.data.model.DealEntity
import com.example.data.model.DealStage
import com.example.data.model.InstallmentPartner
import com.example.data.model.InstallmentStatus
import com.example.data.model.InterestLevel
import com.example.data.model.LeadSource
import com.example.data.model.MonthlyFinancialEntity
import com.example.data.model.QualificationStatus
import com.example.data.model.SalesRepTargetEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CustomerEntity::class,
        DealEntity::class,
        CallLogEntity::class,
        SalesRepTargetEntity::class,
        MonthlyFinancialEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class CrmDatabase : RoomDatabase() {
    abstract fun crmDao(): CrmDao
    abstract fun customerDao(): CustomerDao

    companion object {
        @Volatile
        private var INSTANCE: CrmDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): CrmDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CrmDatabase::class.java,
                    "crm_sales_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(CrmDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class CrmDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.crmDao(), database.customerDao())
                }
            }
        }

        suspend fun populateInitialData(dao: CrmDao, customerDao: CustomerDao) {
            val initialDeals = listOf(
                DealEntity(
                    clientName = "أحمد عبد الفتاح الدسوقي",
                    phone = "01000107735",
                    salesRep = "Nada",
                    stage = DealStage.DONE.name,
                    amount = 1_450_000.0,
                    probability = 100,
                    expectedCloseDate = "2026-08-15",
                    carModel = "Kia Sportage 2024 Highline",
                    carType = "NEW",
                    downPayment = 450_000.0,
                    loanAmount = 1_000_000.0,
                    installmentPartner = InstallmentPartner.DRIVE.displayName,
                    installmentStatus = InstallmentStatus.APPROVED.name,
                    leadSource = LeadSource.META_ADS.name,
                    qualificationStatus = QualificationStatus.QUALIFIED.name,
                    interestLevel = InterestLevel.HOT.name,
                    followUpDate = "Done - License Delivered",
                    commissionRate = 0.025,
                    commissionAmount = 36_250.0,
                    isCommissionReceived = true,
                    receivedNotes = "Paid in full via Bank Wire",
                    date = "15/08/2026",
                    notes = "تمت المعاينة وترخيص السيارة وتسليم العقد النهائي للعميل"
                ),
                DealEntity(
                    clientName = "مروان الشناوي",
                    phone = "01019887766",
                    salesRep = "Marwan",
                    stage = DealStage.DONE.name,
                    amount = 2_850_000.0,
                    probability = 100,
                    expectedCloseDate = "2026-08-10",
                    carModel = "Mercedes C180 Avantgarde 2024",
                    carType = "NEW",
                    downPayment = 1_000_000.0,
                    loanAmount = 1_850_000.0,
                    installmentPartner = InstallmentPartner.CONTACT.displayName,
                    installmentStatus = InstallmentStatus.APPROVED.name,
                    leadSource = LeadSource.REFERRAL.name,
                    qualificationStatus = QualificationStatus.QUALIFIED.name,
                    interestLevel = InterestLevel.HOT.name,
                    followUpDate = "Completed",
                    commissionRate = 0.025,
                    commissionAmount = 71_250.0,
                    isCommissionReceived = true,
                    receivedNotes = "Commission cleared",
                    date = "10/08/2026",
                    notes = "تم اعتماد موافقة كونتاكت واستلام رخصة التسيير"
                ),
                DealEntity(
                    clientName = "محمود حسن السعيد",
                    phone = "01202528325",
                    salesRep = "Nada",
                    stage = DealStage.NEGOTIATION.name,
                    amount = 1_250_000.0,
                    probability = 90,
                    expectedCloseDate = "2026-08-22",
                    carModel = "Hyundai Tucson 2024",
                    carType = "NEW",
                    downPayment = 350_000.0,
                    loanAmount = 900_000.0,
                    installmentPartner = InstallmentPartner.AMAN.displayName,
                    installmentStatus = InstallmentStatus.APPROVED.name,
                    leadSource = LeadSource.META_ADS.name,
                    qualificationStatus = QualificationStatus.QUALIFIED.name,
                    interestLevel = InterestLevel.HOT.name,
                    followUpDate = "Today 4:00 PM (توقيع العقد)",
                    commissionRate = 0.025,
                    commissionAmount = 31_250.0,
                    isCommissionReceived = false,
                    receivedNotes = "Awaiting final signatures",
                    date = "18/08/2026",
                    notes = "موافقة أمان جاهزة بمقدم 28%، ميعاد توقيع العقود بالمعرض اليوم"
                ),
                DealEntity(
                    clientName = "سارة عبد العزيز",
                    phone = "01000272856",
                    salesRep = "Esraa",
                    stage = DealStage.VIEWING.name,
                    amount = 980_000.0,
                    probability = 70,
                    expectedCloseDate = "2026-08-25",
                    carModel = "MG GT 2024 Red",
                    carType = "NEW",
                    downPayment = 280_000.0,
                    loanAmount = 700_000.0,
                    installmentPartner = InstallmentPartner.ONE_FINANCE.displayName,
                    installmentStatus = InstallmentStatus.APPROVED.name,
                    leadSource = LeadSource.META_ADS.name,
                    qualificationStatus = QualificationStatus.QUALIFIED.name,
                    interestLevel = InterestLevel.HOT.name,
                    followUpDate = "Tomorrow 1:00 PM (معاينة)",
                    commissionRate = 0.025,
                    commissionAmount = 24_500.0,
                    isCommissionReceived = false,
                    receivedNotes = "",
                    date = "17/08/2026",
                    notes = "تحديد ميعاد معاينة واستلام عرض السعر المعتمد"
                ),
                DealEntity(
                    clientName = "خالد عبد الرحمن إبراهيم",
                    phone = "01091611300",
                    salesRep = "Alaa",
                    stage = DealStage.PROPOSAL.name,
                    amount = 1_150_000.0,
                    probability = 50,
                    expectedCloseDate = "2026-08-30",
                    carModel = "Toyota Corolla 2024 Active Plus",
                    carType = "NEW",
                    downPayment = 350_000.0,
                    loanAmount = 800_000.0,
                    installmentPartner = InstallmentPartner.BEDAYA.displayName,
                    installmentStatus = InstallmentStatus.PENDING_CONDITIONS.name,
                    leadSource = LeadSource.COLD_CALL.name,
                    qualificationStatus = QualificationStatus.QUALIFIED.name,
                    interestLevel = InterestLevel.WARM.name,
                    followUpDate = "Today 6:00 PM (استكمال مفردات المرتب)",
                    commissionRate = 0.025,
                    commissionAmount = 28_750.0,
                    isCommissionReceived = false,
                    receivedNotes = "",
                    date = "16/08/2026",
                    notes = "العميل أرسل بطاقة الرقم القومي ومتبقي كشف حساب بنكي لشركة بداية"
                ),
                DealEntity(
                    clientName = "إسلام طارق الزيات",
                    phone = "01009876543",
                    salesRep = "Nahla",
                    stage = DealStage.QUALIFICATION.name,
                    amount = 850_000.0,
                    probability = 30,
                    expectedCloseDate = "2026-09-05",
                    carModel = "Chery Tiggo 7 Pro 2024",
                    carType = "NEW",
                    downPayment = 200_000.0,
                    loanAmount = 650_000.0,
                    installmentPartner = InstallmentPartner.DRIVE.displayName,
                    installmentStatus = InstallmentStatus.PENDING_PAPERS.name,
                    leadSource = LeadSource.META_ADS.name,
                    qualificationStatus = QualificationStatus.QUALIFIED.name,
                    interestLevel = InterestLevel.WARM.name,
                    followUpDate = "2026-08-20 (إرسال أوراق التقسيط)",
                    commissionRate = 0.025,
                    commissionAmount = 21_250.0,
                    isCommissionReceived = false,
                    receivedNotes = "",
                    date = "18/08/2026",
                    notes = "تم التأهيل مبدئياً، العميل مهتم وبانتظار تجهيز مفردات المرتب لواتساب"
                ),
                DealEntity(
                    clientName = "يوسف سامي مرقص",
                    phone = "01224431826",
                    salesRep = "Marwan",
                    stage = DealStage.PROSPECTING.name,
                    amount = 750_000.0,
                    probability = 10,
                    expectedCloseDate = "2026-09-10",
                    carModel = "Nissan Sunny 2024 Super Saloon",
                    carType = "NEW",
                    downPayment = 150_000.0,
                    loanAmount = 600_000.0,
                    installmentPartner = InstallmentPartner.CONTACT.displayName,
                    installmentStatus = InstallmentStatus.PENDING_PAPERS.name,
                    leadSource = LeadSource.META_ADS.name,
                    qualificationStatus = QualificationStatus.PENDING.name,
                    interestLevel = InterestLevel.HOT.name,
                    followUpDate = "Today 11:00 AM (أول اتصال ترحيبي)",
                    commissionRate = 0.025,
                    commissionAmount = 18_750.0,
                    isCommissionReceived = false,
                    receivedNotes = "",
                    date = "19/08/2026",
                    notes = "ليد جديد من حملة فيسبوك للسيارات الاقتصادية، مطلوب اتصال أولي"
                ),
                DealEntity(
                    clientName = "هشام كامل عبد الله",
                    phone = "01097016001",
                    salesRep = "Mahmoud",
                    stage = DealStage.LOST.name,
                    amount = 1_100_000.0,
                    probability = 0,
                    expectedCloseDate = "2026-08-12",
                    carModel = "Suzuki Grand Vitara 2024",
                    carType = "NEW",
                    downPayment = 300_000.0,
                    loanAmount = 800_000.0,
                    installmentPartner = InstallmentPartner.DRIVE.displayName,
                    installmentStatus = InstallmentStatus.REJECTED.name,
                    leadSource = LeadSource.META_ADS.name,
                    qualificationStatus = QualificationStatus.UNQUALIFIED.name,
                    interestLevel = InterestLevel.COLD.name,
                    followUpDate = "Lost - Candidate for Recycling",
                    lostReason = "رفض ائتماني بسبب آي سكور - قابل للتدوير لبرامج بدون استعلام",
                    isLostRecycled = false,
                    commissionRate = 0.025,
                    commissionAmount = 27_500.0,
                    isCommissionReceived = false,
                    receivedNotes = "",
                    date = "12/08/2026",
                    notes = "تم رفض الاستعلام البنكي لشركة درايف، يمكن تدويره لعرض بدون استعلام عمل"
                ),
                DealEntity(
                    clientName = "كريم وائل الصيرفي",
                    phone = "01067116230",
                    salesRep = "Esraa",
                    stage = DealStage.LOST.name,
                    amount = 1_600_000.0,
                    probability = 0,
                    expectedCloseDate = "2026-08-14",
                    carModel = "Skoda Octavia A8 2024",
                    carType = "NEW",
                    downPayment = 500_000.0,
                    loanAmount = 1_100_000.0,
                    installmentPartner = InstallmentPartner.BANK.displayName,
                    installmentStatus = InstallmentStatus.CLIENT_CANCELLED.name,
                    leadSource = LeadSource.RETURNING.name,
                    qualificationStatus = QualificationStatus.QUALIFIED.name,
                    interestLevel = InterestLevel.COLD.name,
                    followUpDate = "Lost",
                    lostReason = "سعر الفائدة مرتفع - اشترى كاش من معرض آخر",
                    isLostRecycled = false,
                    commissionRate = 0.025,
                    commissionAmount = 40_000.0,
                    isCommissionReceived = false,
                    receivedNotes = "",
                    date = "14/08/2026",
                    notes = "العميل فضل الشراء كاش بدون مصاريف إدارية"
                )
            )
            dao.insertAllDeals(initialDeals)

            val initialTargets = listOf(
                SalesRepTargetEntity(
                    name = "Nada",
                    month = "Aug",
                    worstCaseTarget = 3_000_000.0,
                    baseTarget = 5_000_000.0,
                    bestCaseTarget = 7_000_000.0,
                    worstCaseRate = 0.002,
                    baseRate = 0.005,
                    bestCaseRate = 0.007,
                    salary = 5_000.0,
                    mktgAllocation = 7_500.0,
                    rentAllocation = 0.0,
                    callsCount = 112
                ),
                SalesRepTargetEntity(
                    name = "Esraa",
                    month = "Aug",
                    worstCaseTarget = 3_000_000.0,
                    baseTarget = 5_000_000.0,
                    bestCaseTarget = 7_000_000.0,
                    worstCaseRate = 0.002,
                    baseRate = 0.005,
                    bestCaseRate = 0.007,
                    salary = 5_000.0,
                    mktgAllocation = 8_200.0,
                    rentAllocation = 0.0,
                    callsCount = 98
                ),
                SalesRepTargetEntity(
                    name = "Nahla",
                    month = "Aug",
                    worstCaseTarget = 2_500_000.0,
                    baseTarget = 4_500_000.0,
                    bestCaseTarget = 6_500_000.0,
                    worstCaseRate = 0.002,
                    baseRate = 0.004,
                    bestCaseRate = 0.005,
                    salary = 4_500.0,
                    mktgAllocation = 6_000.0,
                    rentAllocation = 0.0,
                    callsCount = 84
                ),
                SalesRepTargetEntity(
                    name = "Alaa",
                    month = "Aug",
                    worstCaseTarget = 3_000_000.0,
                    baseTarget = 5_000_000.0,
                    bestCaseTarget = 7_000_000.0,
                    worstCaseRate = 0.002,
                    baseRate = 0.005,
                    bestCaseRate = 0.007,
                    salary = 6_000.0,
                    mktgAllocation = 5_500.0,
                    rentAllocation = 0.0,
                    callsCount = 120
                ),
                SalesRepTargetEntity(
                    name = "Marwan",
                    month = "Aug",
                    worstCaseTarget = 6_000_000.0,
                    baseTarget = 10_000_000.0,
                    bestCaseTarget = 14_000_000.0,
                    worstCaseRate = 0.001,
                    baseRate = 0.0025,
                    bestCaseRate = 0.0035,
                    salary = 6_000.0,
                    mktgAllocation = 15_000.0,
                    rentAllocation = 0.0,
                    callsCount = 165
                ),
                SalesRepTargetEntity(
                    name = "Mahmoud",
                    month = "Aug",
                    worstCaseTarget = 3_000_000.0,
                    baseTarget = 5_000_000.0,
                    bestCaseTarget = 7_500_000.0,
                    worstCaseRate = 0.002,
                    baseRate = 0.005,
                    bestCaseRate = 0.007,
                    salary = 5_500.0,
                    mktgAllocation = 8_000.0,
                    rentAllocation = 0.0,
                    callsCount = 105
                )
            )
            dao.insertAllTargets(initialTargets)

            val initialFinancials = listOf(
                MonthlyFinancialEntity(
                    month = "Aug",
                    commissionRevenueRate = 0.025,
                    totalSalaries = 30_500.0,
                    totalMarketing = 46_200.0,
                    totalRent = 0.0,
                    otherExpenses = 3_500.0
                ),
                MonthlyFinancialEntity(
                    month = "Jul",
                    commissionRevenueRate = 0.025,
                    totalSalaries = 28_000.0,
                    totalMarketing = 40_000.0,
                    totalRent = 0.0,
                    otherExpenses = 2_000.0
                )
            )
            dao.insertAllFinancials(initialFinancials)

            val initialLogs = listOf(
                CallLogEntity(
                    clientName = "أحمد عبد الفتاح الدسوقي",
                    salesRep = "Nada",
                    interactionType = "PHONE_CALL",
                    outcome = "DEAL_CLOSED",
                    notes = "تم تسليم رخصة التسيير والعقد وتأكيد استلام العمولة",
                    month = "Aug",
                    timestamp = System.currentTimeMillis() - 86400000L * 3
                ),
                CallLogEntity(
                    clientName = "محمود حسن السعيد",
                    salesRep = "Nada",
                    interactionType = "WHATSAPP",
                    outcome = "NEGOTIATING",
                    notes = "إرسال جدول الأقساط والموافقة المعتمدة من شركة أمان",
                    month = "Aug",
                    timestamp = System.currentTimeMillis() - 86400000L * 1
                ),
                CallLogEntity(
                    clientName = "سارة عبد العزيز",
                    salesRep = "Esraa",
                    interactionType = "SITE_VIEWING",
                    outcome = "VIEWING_SCHEDULED",
                    notes = "تنسيق ميعاد المعاينة لمعرض السيارات غداً",
                    month = "Aug",
                    timestamp = System.currentTimeMillis() - 3600000L * 6
                ),
                CallLogEntity(
                    clientName = "خالد عبد الرحمن إبراهيم",
                    salesRep = "Alaa",
                    interactionType = "PHONE_CALL",
                    outcome = "FOLLOW_UP",
                    notes = "متابعة كشف الحساب البنكي لشركة بداية",
                    month = "Aug",
                    timestamp = System.currentTimeMillis() - 3600000L * 2
                )
            )
            dao.insertAllCallLogs(initialLogs)

            val initialCustomers = listOf(
                CustomerEntity(
                    name = "أحمد عبد الفتاح الدسوقي",
                    phone = "01000107735",
                    secondaryPhone = "01123456789",
                    email = "ahmed.desouky@example.com",
                    city = "القاهرة - التجمع الخامس",
                    leadSource = LeadSource.META_ADS.name,
                    interestStatus = InterestLevel.HOT.name,
                    qualificationStatus = QualificationStatus.QUALIFIED.name,
                    interestedCarModel = "Kia Sportage 2024 Highline",
                    carCondition = "NEW",
                    budget = 1_450_000.0,
                    downPaymentAvailable = 450_000.0,
                    preferredInstallmentPartner = InstallmentPartner.DRIVE.displayName,
                    assignedSalesRep = "Nada",
                    jobTitle = "مدير مالي بشركة استشارات",
                    monthlyIncome = 55_000.0,
                    notes = "تمت الموافقة واستلام رخصة التسيير",
                    lastContactDate = "15/08/2026",
                    nextFollowUpDate = "Completed"
                ),
                CustomerEntity(
                    name = "محمود حسن السعيد",
                    phone = "01202528325",
                    secondaryPhone = "",
                    email = "m.hassan@example.com",
                    city = "الجيزة - الدقي",
                    leadSource = LeadSource.META_ADS.name,
                    interestStatus = InterestLevel.HOT.name,
                    qualificationStatus = QualificationStatus.QUALIFIED.name,
                    interestedCarModel = "Hyundai Tucson 2024",
                    carCondition = "NEW",
                    budget = 1_250_000.0,
                    downPaymentAvailable = 350_000.0,
                    preferredInstallmentPartner = InstallmentPartner.AMAN.displayName,
                    assignedSalesRep = "Nada",
                    jobTitle = "طبيب جراحة",
                    monthlyIncome = 60_000.0,
                    notes = "موافقة أمان جاهزة بمقدم 28%، ميعاد توقيع العقود بالمعرض اليوم",
                    lastContactDate = "18/08/2026",
                    nextFollowUpDate = "Today 4:00 PM (توقيع العقد)"
                ),
                CustomerEntity(
                    name = "سارة عبد العزيز",
                    phone = "01000272856",
                    secondaryPhone = "",
                    email = "sara.abdelaziz@example.com",
                    city = "القاهرة - مصر الجديدة",
                    leadSource = LeadSource.META_ADS.name,
                    interestStatus = InterestLevel.HOT.name,
                    qualificationStatus = QualificationStatus.QUALIFIED.name,
                    interestedCarModel = "MG GT 2024 Red",
                    carCondition = "NEW",
                    budget = 980_000.0,
                    downPaymentAvailable = 280_000.0,
                    preferredInstallmentPartner = InstallmentPartner.ONE_FINANCE.displayName,
                    assignedSalesRep = "Esraa",
                    jobTitle = "مهندسة برمجيات",
                    monthlyIncome = 45_000.0,
                    notes = "تحديد ميعاد معاينة واستلام عرض السعر المعتمد",
                    lastContactDate = "17/08/2026",
                    nextFollowUpDate = "Tomorrow 1:00 PM (معاينة)"
                ),
                CustomerEntity(
                    name = "خالد عبد الرحمن إبراهيم",
                    phone = "01091611300",
                    secondaryPhone = "",
                    email = "khaled.ibrahim@example.com",
                    city = "الإسكندرية - سموحة",
                    leadSource = LeadSource.COLD_CALL.name,
                    interestStatus = InterestLevel.WARM.name,
                    qualificationStatus = QualificationStatus.QUALIFIED.name,
                    interestedCarModel = "Toyota Corolla 2024 Active Plus",
                    carCondition = "NEW",
                    budget = 1_150_000.0,
                    downPaymentAvailable = 350_000.0,
                    preferredInstallmentPartner = InstallmentPartner.BEDAYA.displayName,
                    assignedSalesRep = "Alaa",
                    jobTitle = "صاحب عمل حر / تجارة",
                    monthlyIncome = 70_000.0,
                    notes = "العميل أرسل بطاقة الرقم القومي ومتبقي كشف حساب بنكي لشركة بداية",
                    lastContactDate = "16/08/2026",
                    nextFollowUpDate = "Today 6:00 PM (استكمال مفردات المرتب)"
                ),
                CustomerEntity(
                    name = "إسلام طارق الزيات",
                    phone = "01009876543",
                    secondaryPhone = "",
                    email = "islam.elzayat@example.com",
                    city = "المنصورة",
                    leadSource = LeadSource.META_ADS.name,
                    interestStatus = InterestLevel.WARM.name,
                    qualificationStatus = QualificationStatus.QUALIFIED.name,
                    interestedCarModel = "Chery Tiggo 7 Pro 2024",
                    carCondition = "NEW",
                    budget = 850_000.0,
                    downPaymentAvailable = 200_000.0,
                    preferredInstallmentPartner = InstallmentPartner.DRIVE.displayName,
                    assignedSalesRep = "Nahla",
                    jobTitle = "محاسب قانوني",
                    monthlyIncome = 35_000.0,
                    notes = "تم التأهيل مبدئياً، العميل مهتم وبانتظار تجهيز مفردات المرتب لواتساب",
                    lastContactDate = "18/08/2026",
                    nextFollowUpDate = "2026-08-20 (إرسال أوراق التقسيط)"
                )
            )
            customerDao.insertAllCustomers(initialCustomers)
        }
    }
}

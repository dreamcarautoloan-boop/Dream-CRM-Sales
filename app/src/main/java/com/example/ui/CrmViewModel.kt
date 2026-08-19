package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CrmDatabase
import com.example.data.model.CallLogEntity
import com.example.data.model.CrmUser
import com.example.data.model.DealEntity
import com.example.data.model.DealStage
import com.example.data.model.InstallmentPartner
import com.example.data.model.InstallmentStatus
import com.example.data.model.InterestLevel
import com.example.data.model.LeadSource
import com.example.data.model.MonthlyFinancialEntity
import com.example.data.model.QualificationStatus
import com.example.data.model.SalesRepTargetEntity
import com.example.data.model.UserPermissions
import com.example.data.model.UserRole
import com.example.data.repository.AuthenticationRepository
import com.example.data.repository.CrmRepository
import com.example.data.service.FirestoreDatabaseSeedService
import com.example.data.service.FirestoreLeadService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RepPerformance(
    val target: SalesRepTargetEntity,
    val totalSales: Double,
    val closedDealsCount: Int,
    val totalDealsCount: Int,
    val commissionEarned: Double,
    val commissionReceived: Double,
    val commissionPending: Double,
    val baseProgressPercent: Float,
    val bestProgressPercent: Float,
    val callsCount: Int,
    val netContribution: Double
)

data class FinancialSummary(
    val month: String,
    val totalSales: Double,
    val revenueRate: Double,
    val grossRevenue: Double,
    val salaries: Double,
    val commissions: Double,
    val marketing: Double,
    val rent: Double,
    val otherExpenses: Double,
    val totalExpenses: Double,
    val netProfit: Double,
    val profitMargin: Double
)

data class CrmUiState(
    val isAuthenticated: Boolean = true,
    val currentUser: CrmUser? = CrmUser(
        displayName = "Sales Manager (المدير)",
        role = UserRole.SALES_MANAGER,
        email = AuthenticationRepository.EXCLUSIVE_MANAGER_EMAIL
    ),
    val permissions: UserPermissions = UserPermissions.fromRole(UserRole.SALES_MANAGER),
    val authLoading: Boolean = false,
    val authErrorMessage: String? = null,
    val deals: List<DealEntity> = emptyList(),
    val filteredDeals: List<DealEntity> = emptyList(),
    val lostDeals: List<DealEntity> = emptyList(),
    val todayFollowUps: List<DealEntity> = emptyList(),
    val callLogs: List<CallLogEntity> = emptyList(),
    val targets: List<SalesRepTargetEntity> = emptyList(),
    val repPerformances: List<RepPerformance> = emptyList(),
    val financials: FinancialSummary? = null,
    val totalPipelineAmount: Double = 0.0,
    val weightedPipelineAmount: Double = 0.0,
    val closedDealsAmount: Double = 0.0,
    val totalCommission: Double = 0.0,
    val totalCommissionReceived: Double = 0.0,
    val totalCommissionPending: Double = 0.0,
    val searchQuery: String = "",
    val selectedStageFilter: String? = null,
    val selectedRepFilter: String? = null,
    val selectedSourceFilter: String? = null,
    val selectedPartnerFilter: String? = null,
    val selectedReceivedFilter: Boolean? = null,
    val selectedMonth: String = "Aug",
    val currentRole: UserRole = UserRole.SALES_MANAGER,
    val activeRepName: String = "Nada",
    val duplicateCheckResult: DealEntity? = null,
    val isCheckingDuplicate: Boolean = false
)

private data class CrmDataBundle(
    val deals: List<DealEntity>,
    val callLogs: List<CallLogEntity>,
    val targets: List<SalesRepTargetEntity>,
    val financials: List<MonthlyFinancialEntity>
)

private data class CrmFilterBundle(
    val query: String,
    val stage: String?,
    val rep: String?,
    val source: String?,
    val partner: String?,
    val received: Boolean?,
    val month: String,
    val role: UserRole,
    val activeRep: String
)

private data class CrmAuthBundle(
    val isAuthenticated: Boolean,
    val currentUser: CrmUser?,
    val authLoading: Boolean,
    val authErrorMessage: String?
)

class CrmViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CrmRepository
    private val authRepository = AuthenticationRepository()

    // Auth states
    private val _isAuthenticated = MutableStateFlow(true)
    private val _currentUser = MutableStateFlow<CrmUser?>(
        CrmUser(
            displayName = "Sales Manager",
            role = UserRole.SALES_MANAGER,
            email = AuthenticationRepository.EXCLUSIVE_MANAGER_EMAIL
        )
    )
    private val _authLoading = MutableStateFlow(false)
    private val _authErrorMessage = MutableStateFlow<String?>(null)

    private val _searchQuery = MutableStateFlow("")
    private val _stageFilter = MutableStateFlow<String?>(null)
    private val _repFilter = MutableStateFlow<String?>(null)
    private val _sourceFilter = MutableStateFlow<String?>(null)
    private val _partnerFilter = MutableStateFlow<String?>(null)
    private val _receivedFilter = MutableStateFlow<Boolean?>(null)
    private val _selectedMonth = MutableStateFlow("Aug")
    private val _currentRole = MutableStateFlow(UserRole.SALES_MANAGER)
    private val _activeRepName = MutableStateFlow("Nada")

    private val _duplicateCheckResult = MutableStateFlow<DealEntity?>(null)
    private val _isCheckingDuplicate = MutableStateFlow(false)
    private val seedService = FirestoreDatabaseSeedService(authRepository.syncService)
    val leadService = FirestoreLeadService()

    init {
        val db = CrmDatabase.getDatabase(application, viewModelScope)
        repository = CrmRepository(db.crmDao(), db.customerDao())

        // Check if there is an existing Firebase session
        authRepository.getCurrentUser()?.let { user ->
            _currentUser.value = user
            _currentRole.value = user.role
            _isAuthenticated.value = true
        }

        // Automatic Firestore seeding on startup for users & incoming leads
        viewModelScope.launch {
            try {
                seedService.seedIfEmpty()
                leadService.seedInitialLeadsIfEmpty()
            } catch (_: Exception) { }
        }
    }

    private val authFlow = combine(
        _isAuthenticated,
        _currentUser,
        _authLoading,
        _authErrorMessage
    ) { isAuth, user, loading, error ->
        CrmAuthBundle(isAuth, user, loading, error)
    }

    private val dataFlow = combine(
        repository.allDeals,
        repository.allCallLogs,
        repository.allTargets,
        repository.allFinancials
    ) { deals, callLogs, targets, financials ->
        CrmDataBundle(deals, callLogs, targets, financials)
    }

    private val filterFlow = combine(
        _searchQuery,
        _stageFilter,
        _repFilter,
        _sourceFilter,
        _partnerFilter,
        _receivedFilter,
        _selectedMonth,
        _currentRole,
        _activeRepName
    ) { args: Array<Any?> ->
        CrmFilterBundle(
            query = args[0] as String,
            stage = args[1] as String?,
            rep = args[2] as String?,
            source = args[3] as String?,
            partner = args[4] as String?,
            received = args[5] as Boolean?,
            month = args[6] as String,
            role = args[7] as UserRole,
            activeRep = args[8] as String
        )
    }

    val uiState: StateFlow<CrmUiState> = combine(
        authFlow,
        dataFlow,
        filterFlow,
        _duplicateCheckResult,
        _isCheckingDuplicate
    ) { authBundle, data, filters, dupResult, isCheckingDup ->
        val deals = data.deals
        val callLogs = data.callLogs
        val targets = data.targets
        val financialsList = data.financials

        val query = filters.query
        val stageFilt = filters.stage
        val repFilt = filters.rep
        val sourceFilt = filters.source
        val partnerFilt = filters.partner
        val recFilt = filters.received
        val month = filters.month
        val role = filters.role
        val activeRep = filters.activeRep

        // Role-based visibility enforcement
        val visibleDeals = if (role == UserRole.SALES_REP) {
            deals.filter { it.salesRep.equals(activeRep, ignoreCase = true) }
        } else {
            deals
        }

        // Filter deals
        val filtered = visibleDeals.filter { deal ->
            val matchesQuery = query.isBlank() ||
                    deal.clientName.contains(query, ignoreCase = true) ||
                    deal.phone.contains(query, ignoreCase = true) ||
                    deal.salesRep.contains(query, ignoreCase = true) ||
                    deal.carModel.contains(query, ignoreCase = true) ||
                    deal.installmentPartner.contains(query, ignoreCase = true) ||
                    deal.notes.contains(query, ignoreCase = true)

            val matchesStage = stageFilt == null || deal.stage.equals(stageFilt, ignoreCase = true)
            val matchesRep = repFilt == null || deal.salesRep.equals(repFilt, ignoreCase = true)
            val matchesSource = sourceFilt == null || deal.leadSource.equals(sourceFilt, ignoreCase = true)
            val matchesPartner = partnerFilt == null || deal.installmentPartner.contains(partnerFilt, ignoreCase = true)
            val matchesRec = recFilt == null || deal.isCommissionReceived == recFilt

            matchesQuery && matchesStage && matchesRep && matchesSource && matchesPartner && matchesRec
        }

        val lostDealsList = visibleDeals.filter { it.stage.equals("LOST", ignoreCase = true) }
        val todayFollowUpsList = visibleDeals.filter {
            it.followUpDate.isNotBlank() && (it.followUpDate.contains("Today", ignoreCase = true) || it.followUpDate.contains("اليوم", ignoreCase = true) || it.stage != "DONE" && it.stage != "LOST")
        }

        val totalPipeline = visibleDeals.filter { it.stage != "LOST" }.sumOf { it.amount }
        val weightedPipeline = visibleDeals.filter { it.stage != "LOST" }.sumOf { it.amount * (it.probability / 100.0) }
        val closedDeals = visibleDeals.filter { it.stage == "DONE" }
        val closedAmount = closedDeals.sumOf { it.amount }
        val totalComm = closedDeals.sumOf { it.commissionAmount }
        val commReceived = closedDeals.filter { it.isCommissionReceived }.sumOf { it.commissionAmount }
        val commPending = totalComm - commReceived

        // Calculate Rep performances
        val repPerformances = targets.map { target ->
            val repDeals = deals.filter { it.salesRep.equals(target.name, ignoreCase = true) }
            val repClosed = repDeals.filter { it.stage == "DONE" }
            val repSales = repClosed.sumOf { it.amount }
            val repCommTotal = repClosed.sumOf { it.commissionAmount }
            val repCommReceived = repClosed.filter { it.isCommissionReceived }.sumOf { it.commissionAmount }
            val repCommPending = repCommTotal - repCommReceived
            val repCalls = callLogs.count { it.salesRep.equals(target.name, ignoreCase = true) }.coerceAtLeast(target.callsCount)

            val tierRate = when {
                repSales >= target.bestCaseTarget -> target.bestCaseRate
                repSales >= target.baseTarget -> target.baseRate
                else -> target.worstCaseRate
            }
            val calculatedComm = if (repSales > 0.0) repSales * tierRate else 0.0

            val baseProgress = if (target.baseTarget > 0.0) (repSales / target.baseTarget).toFloat() else 0f
            val bestProgress = if (target.bestCaseTarget > 0.0) (repSales / target.bestCaseTarget).toFloat() else 0f

            val grossRevenueContributed = repSales * 0.025
            val netContribution = grossRevenueContributed - target.salary - calculatedComm - target.mktgAllocation - target.rentAllocation

            RepPerformance(
                target = target,
                totalSales = repSales,
                closedDealsCount = repClosed.size,
                totalDealsCount = repDeals.size,
                commissionEarned = if (calculatedComm > 0.0) calculatedComm else repCommTotal,
                commissionReceived = repCommReceived,
                commissionPending = repCommPending,
                baseProgressPercent = baseProgress,
                bestProgressPercent = bestProgress,
                callsCount = repCalls,
                netContribution = netContribution
            )
        }

        // Financials calculation
        val currentFinancialEntity = financialsList.find { it.month.equals(month, ignoreCase = true) }
            ?: MonthlyFinancialEntity(month = month)

        val monthSales = repPerformances.sumOf { it.totalSales }
        val grossRev = monthSales * currentFinancialEntity.commissionRevenueRate
        val totalSal = targets.sumOf { it.salary }.coerceAtLeast(currentFinancialEntity.totalSalaries)
        val totalCommPaid = repPerformances.sumOf { it.commissionEarned }
        val totalMktg = targets.sumOf { it.mktgAllocation }.coerceAtLeast(currentFinancialEntity.totalMarketing)
        val totalRent = targets.sumOf { it.rentAllocation }.coerceAtLeast(currentFinancialEntity.totalRent)
        val otherExp = currentFinancialEntity.otherExpenses
        val totalExpenses = totalSal + totalCommPaid + totalMktg + totalRent + otherExp
        val netProfit = grossRev - totalExpenses
        val margin = if (grossRev > 0.0) (netProfit / grossRev) * 100.0 else 0.0

        val financialSummary = FinancialSummary(
            month = month,
            totalSales = monthSales,
            revenueRate = currentFinancialEntity.commissionRevenueRate,
            grossRevenue = grossRev,
            salaries = totalSal,
            commissions = totalCommPaid,
            marketing = totalMktg,
            rent = totalRent,
            otherExpenses = otherExp,
            totalExpenses = totalExpenses,
            netProfit = netProfit,
            profitMargin = margin
        )

        CrmUiState(
            isAuthenticated = authBundle.isAuthenticated,
            currentUser = authBundle.currentUser,
            permissions = UserPermissions.fromRole(role),
            authLoading = authBundle.authLoading,
            authErrorMessage = authBundle.authErrorMessage,
            deals = visibleDeals,
            filteredDeals = filtered,
            lostDeals = lostDealsList,
            todayFollowUps = todayFollowUpsList,
            callLogs = callLogs,
            targets = targets,
            repPerformances = repPerformances,
            financials = financialSummary,
            totalPipelineAmount = totalPipeline,
            weightedPipelineAmount = weightedPipeline,
            closedDealsAmount = closedAmount,
            totalCommission = totalComm,
            totalCommissionReceived = commReceived,
            totalCommissionPending = commPending,
            searchQuery = query,
            selectedStageFilter = stageFilt,
            selectedRepFilter = repFilt,
            selectedSourceFilter = sourceFilt,
            selectedPartnerFilter = partnerFilt,
            selectedReceivedFilter = recFilt,
            selectedMonth = month,
            currentRole = role,
            activeRepName = activeRep,
            duplicateCheckResult = dupResult,
            isCheckingDuplicate = isCheckingDup
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CrmUiState()
    )

    // Auth actions using AuthenticationRepository
    fun signInWithEmail(email: String, pass: String, role: UserRole, repName: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authErrorMessage.value = "Please enter email and password"
            return
        }
        viewModelScope.launch {
            _authLoading.value = true
            _authErrorMessage.value = null

            val result = authRepository.signInWithEmail(email, pass, role, repName)
            result.onSuccess { user ->
                _currentUser.value = user
                _currentRole.value = user.role
                _activeRepName.value = user.assignedRepName
                _isAuthenticated.value = true
            }.onFailure { error ->
                _authErrorMessage.value = error.message ?: "Authentication failed"
            }
            _authLoading.value = false
        }
    }

    fun signUpWithEmail(email: String, pass: String, name: String, role: UserRole, repName: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authErrorMessage.value = "Please enter email and password"
            return
        }
        viewModelScope.launch {
            _authLoading.value = true
            _authErrorMessage.value = null

            val result = authRepository.signUpWithEmail(email, pass, name, role, repName)
            result.onSuccess { user ->
                _currentUser.value = user
                _currentRole.value = user.role
                _activeRepName.value = user.assignedRepName
                _isAuthenticated.value = true
            }.onFailure { error ->
                _authErrorMessage.value = error.message ?: "Registration failed"
            }
            _authLoading.value = false
        }
    }

    fun signInWithGoogle(context: Context, role: UserRole, repName: String) {
        viewModelScope.launch {
            _authLoading.value = true
            _authErrorMessage.value = null

            val result = authRepository.signInWithGoogle(context, role, repName)
            result.onSuccess { user ->
                _currentUser.value = user
                _currentRole.value = user.role
                _activeRepName.value = user.assignedRepName
                _isAuthenticated.value = true
            }.onFailure { error ->
                _authErrorMessage.value = error.message ?: "Google Sign-In failed"
            }
            _authLoading.value = false
        }
    }

    fun quickDemoLogin(role: UserRole, repName: String = "Nada") {
        val email = if (role == UserRole.SALES_MANAGER) {
            AuthenticationRepository.EXCLUSIVE_MANAGER_EMAIL
        } else {
            "${role.name.lowercase()}@dreamauto.com"
        }

        val effectiveRole = AuthenticationRepository.resolveAuthorizedRole(email, role)

        _currentRole.value = effectiveRole
        _activeRepName.value = repName
        _currentUser.value = CrmUser(
            uid = "demo_${effectiveRole.name.lowercase()}",
            displayName = if (effectiveRole == UserRole.SALES_MANAGER) "Sales Manager (المدير)" else "${effectiveRole.arabicName} ($repName)",
            email = email,
            role = effectiveRole,
            assignedRepName = repName
        )
        _isAuthenticated.value = true
        _authErrorMessage.value = null
    }

    fun logout(context: Context? = null) {
        signOut(context)
    }

    fun signOut(context: Context? = null) {
        viewModelScope.launch {
            authRepository.logout(context)
            _currentUser.value = null
            _isAuthenticated.value = false
            _authErrorMessage.value = null
        }
    }

    fun clearAuthError() {
        _authErrorMessage.value = null
    }

    fun setUserRole(role: UserRole) {
        _currentRole.value = role
    }

    fun setActiveRepName(name: String) {
        _activeRepName.value = name
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStageFilter(stage: String?) {
        _stageFilter.value = if (_stageFilter.value == stage) null else stage
    }

    fun setRepFilter(rep: String?) {
        _repFilter.value = if (_repFilter.value == rep) null else rep
    }

    fun setSourceFilter(source: String?) {
        _sourceFilter.value = if (_sourceFilter.value == source) null else source
    }

    fun setPartnerFilter(partner: String?) {
        _partnerFilter.value = if (_partnerFilter.value == partner) null else partner
    }

    fun setReceivedFilter(received: Boolean?) {
        _receivedFilter.value = if (_receivedFilter.value == received) null else received
    }

    fun setSelectedMonth(month: String) {
        _selectedMonth.value = month
    }

    fun checkDuplicatePhone(phone: String) {
        if (phone.trim().length < 8) {
            _duplicateCheckResult.value = null
            return
        }
        viewModelScope.launch {
            _isCheckingDuplicate.value = true
            val existing = repository.getDealByPhone(phone.trim())
            _duplicateCheckResult.value = existing
            _isCheckingDuplicate.value = false
        }
    }

    fun clearDuplicateCheck() {
        _duplicateCheckResult.value = null
    }

    fun saveDeal(
        id: Long = 0,
        clientName: String,
        phone: String,
        salesRep: String,
        stage: String,
        amount: Double,
        probability: Int,
        expectedCloseDate: String,
        carModel: String,
        carType: String,
        downPayment: Double,
        loanAmount: Double,
        installmentPartner: String,
        installmentStatus: String,
        leadSource: String,
        qualificationStatus: String,
        interestLevel: String,
        followUpDate: String,
        lostReason: String,
        commissionRate: Double,
        commissionAmount: Double,
        isCommissionReceived: Boolean,
        receivedNotes: String,
        date: String,
        notes: String
    ) {
        viewModelScope.launch {
            val calcComm = if (commissionAmount > 0.0) commissionAmount else amount * commissionRate
            val deal = DealEntity(
                id = id,
                clientName = clientName.trim(),
                phone = phone.trim(),
                salesRep = salesRep.trim(),
                stage = stage,
                amount = amount,
                probability = probability,
                expectedCloseDate = expectedCloseDate.trim(),
                carModel = carModel.trim(),
                carType = carType,
                downPayment = downPayment,
                loanAmount = loanAmount,
                installmentPartner = installmentPartner,
                installmentStatus = installmentStatus,
                leadSource = leadSource,
                qualificationStatus = qualificationStatus,
                interestLevel = interestLevel,
                followUpDate = followUpDate.trim(),
                lostReason = lostReason.trim(),
                commissionRate = commissionRate,
                commissionAmount = calcComm,
                isCommissionReceived = isCommissionReceived,
                receivedNotes = receivedNotes.trim(),
                date = date.trim(),
                notes = notes.trim(),
                updatedAt = System.currentTimeMillis()
            )
            if (id == 0L) {
                repository.insertDeal(deal)
            } else {
                repository.updateDeal(deal)
            }
        }
    }

    fun deleteDeal(deal: DealEntity) {
        viewModelScope.launch {
            repository.deleteDeal(deal)
        }
    }

    fun updateDealStage(dealId: Long, newStage: String, defaultProbability: Int? = null) {
        viewModelScope.launch {
            val prob = defaultProbability ?: when (newStage.uppercase()) {
                "DONE" -> 100
                "NEGOTIATION" -> 90
                "VIEWING" -> 70
                "PROPOSAL" -> 50
                "QUALIFICATION" -> 30
                "LOST" -> 0
                else -> 10
            }
            repository.updateDealStageAndProbability(dealId, newStage, prob)
        }
    }

    fun reassignDeal(dealId: Long, newRep: String, isRecycled: Boolean = false) {
        viewModelScope.launch {
            repository.reassignDeal(dealId, newRep, isRecycled)
        }
    }

    fun markDealAsLost(dealId: Long, lostReason: String) {
        viewModelScope.launch {
            repository.markDealAsLost(dealId, lostReason)
        }
    }

    fun recycleLostDeal(deal: DealEntity, newRep: String) {
        viewModelScope.launch {
            val updated = deal.copy(
                stage = DealStage.PROSPECTING.name,
                probability = 15,
                salesRep = newRep,
                isLostRecycled = true,
                lostReason = "Recycled from ${deal.salesRep}: ${deal.lostReason}",
                followUpDate = "Today (Recycled Lead Callback)",
                updatedAt = System.currentTimeMillis()
            )
            repository.updateDeal(updated)
        }
    }

    fun toggleCommissionReceived(deal: DealEntity) {
        viewModelScope.launch {
            val newStatus = !deal.isCommissionReceived
            val newNotes = if (newStatus && deal.receivedNotes.isBlank()) "Commission Received" else deal.receivedNotes
            repository.updateCommissionStatus(deal.id, newStatus, newNotes)
        }
    }

    fun addCallLog(
        clientId: Long? = null,
        clientName: String,
        salesRep: String,
        interactionType: String,
        outcome: String,
        notes: String,
        month: String = _selectedMonth.value
    ) {
        viewModelScope.launch {
            val log = CallLogEntity(
                clientId = clientId,
                clientName = clientName.trim(),
                salesRep = salesRep.trim(),
                interactionType = interactionType,
                outcome = outcome,
                notes = notes.trim(),
                month = month,
                timestamp = System.currentTimeMillis()
            )
            repository.insertCallLog(log)
        }
    }

    fun deleteCallLog(log: CallLogEntity) {
        viewModelScope.launch {
            repository.deleteCallLog(log)
        }
    }

    fun saveTarget(target: SalesRepTargetEntity) {
        viewModelScope.launch {
            repository.insertOrUpdateTarget(target)
        }
    }

    fun saveFinancial(financial: MonthlyFinancialEntity) {
        viewModelScope.launch {
            repository.insertOrUpdateFinancial(financial)
        }
    }

    /**
     * Seeds the predefined CRM team members (Ali, Marwan, Mahmoud, Nahla, Israa, Nada, Alaa) into Firestore.
     */
    fun seedPredefinedUsersToFirestore(overwrite: Boolean = false, onComplete: ((Result<Int>) -> Unit)? = null) {
        viewModelScope.launch {
            val result = seedService.seedPredefinedUsers(overwriteExisting = overwrite)
            onComplete?.invoke(result)
        }
    }

    fun getPredefinedTeamMembers(): List<CrmUser> = seedService.getPredefinedTeamMembers()
}

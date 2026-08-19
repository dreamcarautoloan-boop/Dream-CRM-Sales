package com.example.ui.screens

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserRole
import com.example.data.repository.AuthenticationRepository
import com.example.ui.CrmUiState
import com.example.ui.CrmViewModel
import com.example.ui.theme.CrmAmber
import com.example.ui.theme.CrmAmberContainer
import com.example.ui.theme.CrmBlue
import com.example.ui.theme.CrmBlueContainer
import com.example.ui.theme.CrmEmerald
import com.example.ui.theme.CrmEmeraldContainer
import com.example.ui.theme.CrmPurple
import com.example.ui.theme.CrmPurpleContainer
import com.example.ui.theme.CrmRose
import com.example.ui.theme.CrmRoseContainer

enum class PasswordStrength(
    val label: String,
    val arabicLabel: String,
    val score: Float,
    val color: Color
) {
    EMPTY("Empty", "فارغة", 0.0f, Color.Gray),
    WEAK("Weak", "ضعيفة", 0.25f, CrmRose),
    FAIR("Fair", "مقبولة", 0.50f, CrmAmber),
    GOOD("Good", "جيدة", 0.75f, CrmBlue),
    STRONG("Strong", "قوية جداً", 1.0f, CrmEmerald)
}

fun calculatePasswordStrength(pass: String): PasswordStrength {
    if (pass.isEmpty()) return PasswordStrength.EMPTY
    var score = 0
    if (pass.length >= 6) score++
    if (pass.length >= 8) score++
    if (pass.any { it.isDigit() }) score++
    if (pass.any { it.isUpperCase() }) score++
    if (pass.any { !it.isLetterOrDigit() }) score++

    return when {
        score <= 1 -> PasswordStrength.WEAK
        score == 2 -> PasswordStrength.FAIR
        score in 3..4 -> PasswordStrength.GOOD
        else -> PasswordStrength.STRONG
    }
}

fun isValidEmail(email: String): Boolean {
    val trimmed = email.trim()
    return trimmed.isNotEmpty() && (
        Patterns.EMAIL_ADDRESS.matcher(trimmed).matches() ||
        trimmed.matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"))
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    state: CrmUiState,
    viewModel: CrmViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var isSignUp by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(state.currentRole) }
    var selectedRepName by remember { mutableStateOf(state.activeRepName) }
    var repDropdownExpanded by remember { mutableStateOf(false) }

    var email by remember {
        mutableStateOf(
            if (state.currentRole == UserRole.SALES_MANAGER) AuthenticationRepository.EXCLUSIVE_MANAGER_EMAIL
            else "sales@dreamauto.com"
        )
    }
    var password by remember { mutableStateOf("password123") }
    var fullName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailTouched by remember { mutableStateOf(false) }
    var passwordTouched by remember { mutableStateOf(false) }

    val isEmailValid by remember(email) {
        derivedStateOf { isValidEmail(email) }
    }

    val passwordStrength by remember(password) {
        derivedStateOf { calculatePasswordStrength(password) }
    }

    val isManagerRoleMismatch by remember(selectedRole, email) {
        derivedStateOf {
            selectedRole == UserRole.SALES_MANAGER &&
                !email.trim().equals(AuthenticationRepository.EXCLUSIVE_MANAGER_EMAIL, ignoreCase = true)
        }
    }

    val isFormValid by remember(isEmailValid, password, isSignUp, fullName, isManagerRoleMismatch) {
        derivedStateOf {
            isEmailValid &&
            password.length >= 6 &&
            (!isSignUp || fullName.isNotBlank()) &&
            !isManagerRoleMismatch
        }
    }

    // Team structure according to PRD & User instructions:
    // Sales Manager: علي (Ali)
    // Team Leader: مروان (Marwan)
    // Sales Reps: محمود (Mahmoud), نهله (Nahla), اسراء (Esraa), ندى (Nada), الاء (Alaa)
    val availableReps = listOf("Mahmoud", "Nahla", "Esraa", "Nada", "Alaa")

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // App Identity & Hero Logo with Luxury Dream Car Badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF0F172A))
                    .border(2.dp, Color(0xFFF59E0B), RoundedCornerShape(24.dp))
                    .padding(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_dreamcar_logo),
                    contentDescription = "Dream Car Auto Loan CRM Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Dream Car Auto Loan CRM",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "نظام إدارة مبيعات وتقسيط السيارات",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Quick One-Tap Team Profiles (سويتش سريع لأعضاء الفريق)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "QUICK TEAM ACCESS (الدخول السريع لأعضاء الفريق):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Manager Ali & Team Leader Marwan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.quickDemoLogin(UserRole.SALES_MANAGER, "Ali")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CrmBlueContainer, contentColor = CrmBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("علي (المدير)", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                Text("Sales Manager", fontSize = 9.sp)
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.quickDemoLogin(UserRole.TEAM_LEADER, "Marwan")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CrmPurpleContainer, contentColor = CrmPurple),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("مروان (تيم ليدر)", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                Text("Team Leader", fontSize = 9.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 5 Sales Reps: Mahmoud, Nahla, Esraa, Nada, Alaa
                    Text(
                        text = "فريق السيلز (Sales Reps):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val repsWithArabic = listOf(
                            "Mahmoud" to "محمود",
                            "Nahla" to "نهله",
                            "Esraa" to "اسراء",
                            "Nada" to "ندى",
                            "Alaa" to "الاء"
                        )
                        repsWithArabic.forEach { (repEng, repArab) ->
                            FilterChip(
                                selected = state.activeRepName == repEng && state.currentRole == UserRole.SALES_REP,
                                onClick = {
                                    viewModel.quickDemoLogin(UserRole.SALES_REP, repEng)
                                },
                                label = { Text("$repArab ($repEng)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CrmEmeraldContainer,
                                    selectedLabelColor = CrmEmerald
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Role Selector Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CUSTOM ROLE LOGIN (تسجيل الدخول المخصص):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4 Role Tiles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RoleTile(
                            role = UserRole.SALES_MANAGER,
                            isSelected = selectedRole == UserRole.SALES_MANAGER,
                            onClick = {
                                selectedRole = UserRole.SALES_MANAGER
                                if (email.isBlank() || email.contains("@dreamauto.com")) {
                                    email = AuthenticationRepository.EXCLUSIVE_MANAGER_EMAIL
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        RoleTile(
                            role = UserRole.TEAM_LEADER,
                            isSelected = selectedRole == UserRole.TEAM_LEADER,
                            onClick = {
                                selectedRole = UserRole.TEAM_LEADER
                                if (email == AuthenticationRepository.EXCLUSIVE_MANAGER_EMAIL) {
                                    email = "marwan@dreamauto.com"
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RoleTile(
                            role = UserRole.SALES_REP,
                            isSelected = selectedRole == UserRole.SALES_REP,
                            onClick = {
                                selectedRole = UserRole.SALES_REP
                                if (email == AuthenticationRepository.EXCLUSIVE_MANAGER_EMAIL) {
                                    email = "${selectedRepName.lowercase()}@dreamauto.com"
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        RoleTile(
                            role = UserRole.MODERATOR,
                            isSelected = selectedRole == UserRole.MODERATOR,
                            onClick = {
                                selectedRole = UserRole.MODERATOR
                                if (email == AuthenticationRepository.EXCLUSIVE_MANAGER_EMAIL) {
                                    email = "moderator@dreamauto.com"
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Role Authorization Notice
                    if (selectedRole == UserRole.SALES_MANAGER) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = CrmBlueContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = CrmBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "حساب المدير (علي): ${AuthenticationRepository.EXCLUSIVE_MANAGER_EMAIL}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CrmBlue,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // If Sales Rep is chosen, select which rep
                    if (selectedRole == UserRole.SALES_REP) {
                        Spacer(modifier = Modifier.height(12.dp))
                        ExposedDropdownMenuBox(
                            expanded = repDropdownExpanded,
                            onExpandedChange = { repDropdownExpanded = !repDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = "Sales Representative: $selectedRepName",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Select Rep Profile (اختر السيلز)") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = repDropdownExpanded) },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = repDropdownExpanded,
                                onDismissRequest = { repDropdownExpanded = false }
                            ) {
                                availableReps.forEach { rep ->
                                    DropdownMenuItem(
                                        text = { Text(rep, fontWeight = if (rep == selectedRepName) FontWeight.Bold else FontWeight.Normal) },
                                        onClick = {
                                            selectedRepName = rep
                                            repDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Auth Form Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isSignUp) "Create CRM Account (حساب جديد)" else "Sign In with Credentials (تسجيل الدخول)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isSignUp) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name (الاسم بالكامل)") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_fullname_input")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Email Input
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailTouched = true
                        },
                        label = { Text("Email Address (البريد الإلكتروني)") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        trailingIcon = {
                            if (emailTouched && email.isNotEmpty()) {
                                if (isEmailValid) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Valid Email",
                                        tint = CrmEmerald,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.ErrorOutline,
                                        contentDescription = "Invalid Email",
                                        tint = CrmRose,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        isError = (emailTouched && !isEmailValid) || isManagerRoleMismatch,
                        supportingText = {
                            if (emailTouched && !isEmailValid) {
                                Text("Please enter a valid email address (e.g. name@dreamauto.com)", color = CrmRose)
                            } else if (isManagerRoleMismatch) {
                                Text("Sales Manager role is restricted to ${AuthenticationRepository.EXCLUSIVE_MANAGER_EMAIL}", color = CrmRose)
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Password Input
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordTouched = true
                        },
                        label = { Text("Password (كلمة المرور)") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = passwordTouched && password.length < 6,
                        supportingText = {
                            if (passwordTouched && password.length < 6) {
                                Text("Password must be at least 6 characters", color = CrmRose)
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (isFormValid) {
                                    if (isSignUp) {
                                        viewModel.signUpWithEmail(email, password, fullName, selectedRole, selectedRepName)
                                    } else {
                                        viewModel.signInWithEmail(email, password, selectedRole, selectedRepName)
                                    }
                                }
                            }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input")
                    )

                    // Password Strength Indicator
                    if (password.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Password Strength (قوة كلمة المرور):",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${passwordStrength.arabicLabel} • ${passwordStrength.label}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = passwordStrength.color
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            val animatedProgress by animateFloatAsState(
                                targetValue = passwordStrength.score,
                                label = "strength_progress"
                            )
                            val animatedColor by animateColorAsState(
                                targetValue = passwordStrength.color,
                                label = "strength_color"
                            )

                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                color = animatedColor,
                                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                PasswordCriteriaPill(label = "6+ chars", isMet = password.length >= 6, modifier = Modifier.weight(1f))
                                PasswordCriteriaPill(label = "Number", isMet = password.any { it.isDigit() }, modifier = Modifier.weight(1f))
                                PasswordCriteriaPill(label = "Uppercase", isMet = password.any { it.isUpperCase() }, modifier = Modifier.weight(1f))
                                PasswordCriteriaPill(label = "Symbol", isMet = password.any { !it.isLetterOrDigit() }, modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    // Error Banner
                    if (state.authErrorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = CrmRoseContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = CrmRose, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = state.authErrorMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CrmRose,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary Submit Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            if (isSignUp) {
                                viewModel.signUpWithEmail(email, password, fullName, selectedRole, selectedRepName)
                            } else {
                                viewModel.signInWithEmail(email, password, selectedRole, selectedRepName)
                            }
                        },
                        enabled = isFormValid && !state.authLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_submit_button")
                    ) {
                        if (state.authLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (isSignUp) "Create Account (إنشاء الحساب)" else "Sign In (دخول)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Toggle Sign Up / Sign In
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isSignUp) "Already have an account?" else "Need a new account?",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSignUp) "Sign In" else "Sign Up",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable {
                                    isSignUp = !isSignUp
                                    viewModel.clearAuthError()
                                }
                                .padding(4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PasswordCriteriaPill(
    label: String,
    isMet: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isMet) CrmEmeraldContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(vertical = 3.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isMet) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (isMet) CrmEmerald else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = if (isMet) CrmEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
fun RoleTile(
    role: UserRole,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, bg, fg) = when (role) {
        UserRole.SALES_MANAGER -> Triple(Icons.Default.SupervisorAccount, CrmBlueContainer, CrmBlue)
        UserRole.TEAM_LEADER -> Triple(Icons.Default.BusinessCenter, CrmPurpleContainer, CrmPurple)
        UserRole.SALES_REP -> Triple(Icons.Default.Person, CrmEmeraldContainer, CrmEmerald)
        UserRole.MODERATOR -> Triple(Icons.Default.AccountCircle, CrmAmberContainer, CrmAmber)
    }

    Surface(
        color = if (isSelected) bg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 0.5.dp,
            color = if (isSelected) fg else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) fg else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = role.arabicName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (isSelected) fg else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = role.displayName,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

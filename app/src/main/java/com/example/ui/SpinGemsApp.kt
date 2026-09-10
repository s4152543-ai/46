package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.RewardEntity
import com.example.data.SpinHistoryEntity
import com.example.data.UserEntity
import com.example.ui.theme.*
import com.example.viewmodel.SpinGemsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Lobby : Screen("lobby", "Game", Icons.Default.Casino)
    object LuckyWheel : Screen("lucky_wheel", "Lucky Wheel", Icons.Default.BrightnessHigh)
    object SlotMachine : Screen("slot_machine", "Slots", Icons.Default.Gamepad)
    object Bank : Screen("bank", "Bank", Icons.Default.AccountBalance)
    object Events : Screen("events", "Events", Icons.Default.CardGiftcard)
    object ReferEarn : Screen("refer_earn", "Refer & Earn", Icons.Default.Share)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpinGemsApp(viewModel: SpinGemsViewModel = viewModel()) {
    val navController = rememberNavController()
    val user by viewModel.user.collectAsState()
    val context = LocalContext.current

    val screens = listOf(
        Screen.Bank,
        Screen.Lobby,
        Screen.LuckyWheel,
        Screen.ReferEarn,
        Screen.Events
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Profile & VIP
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = "Profile", tint = EmeraldDark)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = GoldPrimary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, GoldPrimary)
                            ) {
                                Text(
                                    text = "VIP ${user?.vipLevel ?: 1}",
                                    color = GoldPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Balance and Gems Display
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = EmeraldSurface,
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.MonetizationOn, contentDescription = "Coins", tint = GoldPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = String.format("%.2f", user?.balance ?: 0.0),
                                        color = GoldPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            viewModel.deposit(500.0)
                                            Toast.makeText(context, "Added 500.00 Coins!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp).testTag("add_balance_button")
                                    ) {
                                        Icon(Icons.Default.AddCircle, contentDescription = "Add Funds", tint = GoldPrimary)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                color = EmeraldSurface,
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, DiamondCyan.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Diamond, contentDescription = "Gems", tint = DiamondCyan, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${user?.gems ?: 0}",
                                        color = DiamondCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EmeraldDark)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = EmeraldDark,
                tonalElevation = 8.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title, fontSize = 10.sp) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GoldPrimary,
                            selectedTextColor = GoldPrimary,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = EmeraldSurface
                        ),
                        modifier = Modifier.testTag("nav_${screen.route}")
                    )
                }
            }
        },
        containerColor = EmeraldDark
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Lobby.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Lobby.route) {
                LobbyScreen(viewModel = viewModel, onNavigateToWheel = {
                    navController.navigate(Screen.LuckyWheel.route)
                }, onNavigateToSlots = {
                    navController.navigate(Screen.SlotMachine.route)
                })
            }
            composable(Screen.LuckyWheel.route) {
                LuckyWheelScreen(viewModel = viewModel)
            }
            composable(Screen.SlotMachine.route) {
                SlotMachineScreen(viewModel = viewModel)
            }
            composable(Screen.Bank.route) {
                BankScreen(viewModel = viewModel)
            }
            composable(Screen.Events.route) {
                EventsScreen(viewModel = viewModel)
            }
            composable(Screen.ReferEarn.route) {
                ReferEarnScreen(user = user)
            }
        }
    }
}

@Composable
fun LobbyScreen(viewModel: SpinGemsViewModel, onNavigateToWheel: () -> Unit, onNavigateToSlots: () -> Unit) {
    val spinHistory by viewModel.spinHistory.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Jackpot Banner Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldMedium),
                border = BorderStroke(2.dp, GoldPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = "Jackpot", tint = GoldPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("JACKPOT LOTTO LIVE", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Surface(
                            color = RubyRed,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("LIVE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Current Jackpot Pool", color = TextMuted, fontSize = 12.sp)
                    Text("₹1,179,124", color = GoldPrimary, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Draw In: 02:02:57", color = TextLight, fontSize = 12.sp)
                        Text("My Tickets: 5/100", color = DiamondCyan, fontSize = 12.sp)
                    }
                }
            }
        }

        // Quick Action Banners
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToWheel,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    modifier = Modifier.weight(1f).height(50.dp).testTag("goto_lucky_wheel")
                ) {
                    Icon(Icons.Default.BrightnessHigh, contentDescription = null, tint = EmeraldDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lucky Wheel", color = EmeraldDark, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onNavigateToSlots,
                    colors = ButtonDefaults.buttonColors(containerColor = DiamondCyan),
                    modifier = Modifier.weight(1f).height(50.dp).testTag("goto_slots")
                ) {
                    Icon(Icons.Default.Gamepad, contentDescription = null, tint = EmeraldDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gem Slots", color = EmeraldDark, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Hot Games Section
        item {
            Text("HOT GEM GAMES", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        val games = listOf(
            Triple("500 Lucky Gems", "Spin & Match", RubyRed),
            Triple("Chicken Road 2", "Multiplier Quest", GoldDark),
            Triple("Vono Aviator", "Rocket Flight", SapphireBlue),
            Triple("Ace Super 777", "Classic Slot", AmethystPurple),
            Triple("Gems II Fortune", "Tiki Jackpot", DiamondCyan),
            Triple("Ganesha Gold", "Divine Reels", GoldPrimary)
        )

        items(games.chunked(2)) { rowGames ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowGames.forEach { game ->
                    Card(
                        onClick = onNavigateToSlots,
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = EmeraldMedium),
                        border = BorderStroke(1.dp, game.third.copy(alpha = 0.6f)),
                        modifier = Modifier.weight(1f).height(110.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    color = game.third,
                                    shape = CircleShape,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Surface(
                                    color = RubyRed,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("HOT", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                            Column {
                                Text(game.first, color = TextLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(game.second, color = TextMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }
                if (rowGames.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // Recent Spin History
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("RECENT SPIN HISTORY", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        if (spinHistory.isEmpty()) {
            item {
                Text("No spins yet. Try the Lucky Wheel or Gem Slots!", color = TextMuted, fontSize = 13.sp)
            }
        } else {
            items(spinHistory.take(5)) { spin ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = EmeraldMedium),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(spin.gameName, color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Bet: ${spin.betAmount}", color = TextMuted, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(spin.resultText, color = if (spin.winAmount > 0) Color.Green else RubyRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("+${String.format("%.2f", spin.winAmount)}", color = GoldPrimary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LuckyWheelScreen(viewModel: SpinGemsViewModel) {
    val context = LocalContext.current
    var isSpinning by remember { mutableStateOf(false) }
    var rotationAngle by remember { mutableStateOf(0f) }
    val spinResultText = remember { mutableStateOf("Spin the wheel for epic gem rewards!") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("LUCKY GEM WHEEL", color = GoldPrimary, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(spinResultText.value, color = TextLight, fontSize = 14.sp, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(32.dp))

        // Wheel representation
        Box(
            modifier = Modifier
                .size(280.dp)
                .rotate(rotationAngle)
                .border(6.dp, GoldPrimary, CircleShape)
                .background(EmeraldSurface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Diamond, contentDescription = null, tint = DiamondCyan, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("500X", color = GoldPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (!isSpinning) {
                    isSpinning = true
                    spinResultText.value = "Spinning gems..."
                    val randomAdd = (720..1440).random().toFloat()
                    rotationAngle += randomAdd

                    viewModel.spinGame("Lucky Wheel", 25.0, true) { success, win, msg ->
                        isSpinning = false
                        spinResultText.value = msg
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            },
            enabled = !isSpinning,
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
            modifier = Modifier.fillMaxWidth().height(56.dp).testTag("spin_wheel_button")
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = EmeraldDark)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isSpinning) "Spinning..." else "SPIN WHEEL (25 Gems)", color = EmeraldDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun SlotMachineScreen(viewModel: SpinGemsViewModel) {
    val context = LocalContext.current
    var isSpinning by remember { mutableStateOf(false) }
    var reel1 by remember { mutableStateOf("💎") }
    var reel2 by remember { mutableStateOf("👑") }
    var reel3 by remember { mutableStateOf("⭐") }
    var resultMsg by remember { mutableStateOf("Match 3 gems to win jackpot!") }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("GEM SLOTS MACHINE", color = GoldPrimary, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(resultMsg, color = TextLight, fontSize = 14.sp, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(32.dp))

        // Slot Reels Box
        Surface(
            color = EmeraldMedium,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, GoldPrimary),
            modifier = Modifier.fillMaxWidth().height(140.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SlotReel(symbol = reel1)
                SlotReel(symbol = reel2)
                SlotReel(symbol = reel3)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (!isSpinning) {
                    isSpinning = true
                    resultMsg = "Spinning reels..."
                    
                    coroutineScope.launch {
                        val symbols = listOf("💎", "👑", "⭐", "🍒", "🍀", "7️⃣")
                        for (i in 0..10) {
                            reel1 = symbols.random()
                            reel2 = symbols.random()
                            reel3 = symbols.random()
                            delay(100)
                        }

                        viewModel.spinGame("Gem Slots", 50.0, false) { success, win, msg ->
                            isSpinning = false
                            if (success) {
                                reel1 = "💎"
                                reel2 = "💎"
                                reel3 = "💎"
                            }
                            resultMsg = msg
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            enabled = !isSpinning,
            colors = ButtonDefaults.buttonColors(containerColor = RubyRed),
            modifier = Modifier.fillMaxWidth().height(56.dp).testTag("spin_slots_button")
        ) {
            Icon(Icons.Default.Casino, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isSpinning) "Spinning Reels..." else "SPIN SLOTS (50.00 Coins)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun SlotReel(symbol: String) {
    Surface(
        color = EmeraldDark,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
        modifier = Modifier.size(80.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(symbol, fontSize = 36.sp)
        }
    }
}

@Composable
fun BankScreen(viewModel: SpinGemsViewModel) {
    val user by viewModel.user.collectAsState()
    val withdrawalHistory by viewModel.withdrawalHistory.collectAsState()
    val context = LocalContext.current

    var bankName by remember { mutableStateOf("") }
    var accountHolder by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var ifscCode by remember { mutableStateOf("") }
    var withdrawAmount by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("BANK & REAL MONEY VAULT", color = GoldPrimary, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = EmeraldMedium),
                border = BorderStroke(1.dp, GoldPrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Total Balance", color = TextMuted, fontSize = 12.sp)
                    Text("₹${String.format("%.2f", user?.balance ?: 0.0)}", color = GoldPrimary, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Winnings: ₹${String.format("%.2f", user?.totalWins ?: 0.0)}", color = TextLight, fontSize = 13.sp)
                        Text("VIP Tier ${user?.vipLevel ?: 1}", color = DiamondCyan, fontSize = 13.sp)
                    }
                }
            }
        }

        item {
            Text("Quick Deposit", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf(100.0, 500.0, 1000.0).forEach { amount ->
                    Button(
                        onClick = {
                            viewModel.deposit(amount)
                            Toast.makeText(context, "Deposited ₹$amount", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSurface),
                        border = BorderStroke(1.dp, GoldPrimary),
                        modifier = Modifier.weight(1f).testTag("deposit_$amount")
                    ) {
                        Text("+₹$amount", color = GoldPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Bank Account Withdrawal Details", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Bank Name (e.g. HDFC, SBI, Chase)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = TextMuted,
                        focusedLabelColor = GoldPrimary,
                        unfocusedLabelColor = TextMuted,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("bank_name_input")
                )

                OutlinedTextField(
                    value = accountHolder,
                    onValueChange = { accountHolder = it },
                    label = { Text("Account Holder Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = TextMuted,
                        focusedLabelColor = GoldPrimary,
                        unfocusedLabelColor = TextMuted,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("account_holder_input")
                )

                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    label = { Text("Bank Account Number") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = TextMuted,
                        focusedLabelColor = GoldPrimary,
                        unfocusedLabelColor = TextMuted,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("account_number_input")
                )

                OutlinedTextField(
                    value = ifscCode,
                    onValueChange = { ifscCode = it },
                    label = { Text("IFSC / Routing Code") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = TextMuted,
                        focusedLabelColor = GoldPrimary,
                        unfocusedLabelColor = TextMuted,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("ifsc_input")
                )

                OutlinedTextField(
                    value = withdrawAmount,
                    onValueChange = { withdrawAmount = it },
                    label = { Text("Withdrawal Amount (₹)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = TextMuted,
                        focusedLabelColor = GoldPrimary,
                        unfocusedLabelColor = TextMuted,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("withdraw_input")
                )

                Button(
                    onClick = {
                        val amt = withdrawAmount.toDoubleOrNull() ?: 0.0
                        viewModel.withdrawToBank(bankName, accountNumber, ifscCode, accountHolder, amt) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            if (success) {
                                withdrawAmount = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("withdraw_button")
                ) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = EmeraldDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Withdraw Real Money to Bank", color = EmeraldDark, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("BANK WITHDRAWAL HISTORY", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("(${withdrawalHistory.size} Records)", color = TextMuted, fontSize = 12.sp)
            }
        }

        if (withdrawalHistory.isEmpty()) {
            item {
                Text("No withdrawal history yet. Enter bank details and withdraw real money winnings!", color = TextMuted, fontSize = 13.sp)
            }
        } else {
            items(withdrawalHistory) { record ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = EmeraldMedium),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(record.bankName, color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Surface(
                                color = Color.Green.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color.Green)
                            ) {
                                Text(record.status, color = Color.Green, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Text("Amount: ₹${String.format("%.2f", record.amount)}", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("A/C: ${record.accountNumber} | IFSC: ${record.ifscCode}", color = TextMuted, fontSize = 12.sp)
                        Text("Holder: ${record.accountHolderName}", color = TextMuted, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun EventsScreen(viewModel: SpinGemsViewModel) {
    val rewards by viewModel.rewards.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("EVENTS & DAILY REWARDS", color = GoldPrimary, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)

        if (rewards.isEmpty()) {
            Text("No active events right now.", color = TextMuted)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(rewards) { reward ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmeraldMedium),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (reward.isClaimed) TextMuted else GoldPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(reward.title, color = TextLight, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Diamond, contentDescription = null, tint = DiamondCyan, modifier = Modifier.size(16.dp))
                                    Text(" +${reward.rewardGems} Gems  ", color = DiamondCyan, fontSize = 13.sp)
                                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                                    Text(" +₹${reward.rewardCoins}", color = GoldPrimary, fontSize = 13.sp)
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.claimReward(reward)
                                    Toast.makeText(context, "Claimed ${reward.title}!", Toast.LENGTH_SHORT).show()
                                },
                                enabled = !reward.isClaimed,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (reward.isClaimed) TextMuted else GoldPrimary
                                ),
                                modifier = Modifier.testTag("claim_${reward.id}")
                            ) {
                                Text(if (reward.isClaimed) "Claimed" else "Claim", color = EmeraldDark, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReferEarnScreen(user: UserEntity?) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Share, contentDescription = "Refer", tint = GoldPrimary, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("REFER & EARN", color = GoldPrimary, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Invite friends to Spin Gems and earn 200 Gems + 10% commission on every spin!", color = TextLight, fontSize = 14.sp, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            color = EmeraldMedium,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, GoldPrimary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Your Referral Code", color = TextMuted, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(user?.referralCode ?: "GEMSPIN777", color = GoldPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                Toast.makeText(context, "Referral link copied to clipboard!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("share_referral_button")
        ) {
            Icon(Icons.Default.Share, contentDescription = null, tint = EmeraldDark)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Share Invite Link", color = EmeraldDark, fontWeight = FontWeight.Bold)
        }
    }
}

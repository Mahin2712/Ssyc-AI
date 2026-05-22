package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.StudySyncViewModel
import com.example.ui.screens.*
import com.example.ui.theme.StudySyncTheme

class MainActivity : ComponentActivity() {
    private val viewModel: StudySyncViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudySyncTheme(darkTheme = viewModel.isDarkMode) {
                Scaffold(
                    topBar = {
                        if (viewModel.currentScreen != "room_detail") {
                            TopAppBar(
                                title = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                        ) {
                                            Text(
                                                "SS",
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }
                                        Text(
                                            "StudySync",
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = (-0.5).sp,
                                            fontSize = 20.sp,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(
                                                    if (viewModel.activeSession.collectAsState().value != null) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                                                    CircleShape
                                                )
                                        )
                                    }
                                },
                                actions = {
                                    IconButton(onClick = { viewModel.toggleDarkMode() }) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Toggle Theme"
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .padding(end = 12.dp)
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .clickable { viewModel.navigateTo("profile") }
                                    ) {
                                        Text(
                                            "JD",
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.background
                                )
                            )
                        }
                    },
                    bottomBar = {
                        if (viewModel.currentScreen != "room_detail") {
                            NavigationBar(
                                tonalElevation = 0.dp,
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                NavigationBarItem(
                                    selected = viewModel.currentScreen == "home",
                                    onClick = { viewModel.navigateTo("home") },
                                    icon = {
                                        Icon(
                                            imageVector = if (viewModel.currentScreen == "home") Icons.Filled.Home else Icons.Outlined.Home,
                                            contentDescription = "Home"
                                        )
                                    },
                                    label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                                )
                                NavigationBarItem(
                                    selected = viewModel.currentScreen == "rooms" || viewModel.currentScreen == "room_detail",
                                    onClick = { viewModel.navigateTo("rooms") },
                                    icon = {
                                        Icon(
                                            imageVector = if (viewModel.currentScreen == "rooms" || viewModel.currentScreen == "room_detail") Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                                            contentDescription = "Rooms"
                                        )
                                    },
                                    label = { Text("Rooms", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                                )
                                NavigationBarItem(
                                    selected = viewModel.currentScreen == "leaderboard",
                                    onClick = { viewModel.navigateTo("leaderboard") },
                                    icon = {
                                        Icon(
                                            imageVector = if (viewModel.currentScreen == "leaderboard") Icons.Filled.Star else Icons.Outlined.Star,
                                            contentDescription = "Stats"
                                        )
                                    },
                                    label = { Text("Stats", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                                )
                                NavigationBarItem(
                                    selected = viewModel.currentScreen == "profile",
                                    onClick = { viewModel.navigateTo("profile") },
                                    icon = {
                                        Icon(
                                            imageVector = if (viewModel.currentScreen == "profile") Icons.Filled.Person else Icons.Outlined.Person,
                                            contentDescription = "Profile"
                                        )
                                    },
                                    label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        when (viewModel.currentScreen) {
                            "home" -> HomeScreen(viewModel)
                            "rooms" -> RoomsScreen(viewModel)
                            "room_detail" -> RoomDetailScreen(viewModel)
                            "leaderboard" -> LeaderboardScreen(viewModel)
                            "profile" -> ProfileScreen(viewModel)
                            else -> HomeScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}

// Retro-compatible greeting helper for screenshot/legacy tests
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

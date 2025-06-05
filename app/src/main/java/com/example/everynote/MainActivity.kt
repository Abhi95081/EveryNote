package com.example.everynote

import LoginScreen
import NoteViewModel
import NoteViewModelFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.everynote.Animation.LottieLoadingAnimation
import com.example.everynote.Screens.HomeScreen
import com.example.everynote.ui.theme.EveryNoteTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EveryNoteTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "welcome") {
                    composable("welcome") {
                        WelcomeScreen(onGetStarted = {
                            navController.navigate("login"){
                                popUpTo("welcome") { inclusive = true }
                            }
                        })
                    }
                    composable("login") {
                        LoginScreen(navHostController = navController)
                    }
                    composable("home") {
                        val factory = NoteViewModelFactory(applicationContext)
                        val noteViewModel: NoteViewModel = viewModel(factory = factory)
                        HomeScreen(viewModel = noteViewModel)
                    }


                }
            }
        }
    }
}

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    val clicked = remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (clicked.value) 0.9f else 1f,
        animationSpec = tween(durationMillis = 150),
        finishedListener = {
            if (clicked.value) onGetStarted()
        }
    )

    val rainbowBrush = Brush.linearGradient(
        colors = listOf(Color(0xFFff9a9e), Color(0xFFfad0c4), Color(0xFFfbc2eb), Color(0xFFa6c1ee))
    )

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF0f2027), Color(0xFF203a43), Color(0xFF2c5364))
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(brush = backgroundBrush),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Every Notes",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Capture your day, one note at a time.",
                    color = Color.LightGray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(64.dp))
                LottieLoadingAnimation()
                Spacer(modifier = Modifier.height(64.dp))
                Button(
                    onClick = { clicked.value = true },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    modifier = Modifier
                        .padding(16.dp)
                        .height(55.dp)
                        .width(220.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .background(rainbowBrush, shape = CircleShape)
                ) {
                    Text(
                        text = "Get Started",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

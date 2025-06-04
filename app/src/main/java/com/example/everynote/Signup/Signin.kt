import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.airbnb.lottie.compose.*
import com.example.everynote.R
import com.example.everynote.prefdatastorage.User
import com.example.everynote.prefdatastorage.UserPreferences
import kotlinx.coroutines.launch
import androidx.core.net.toUri

@Composable
fun LoginScreen(navHostController: NavHostController) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }
    val scope = rememberCoroutineScope()

    val savedUser by userPrefs.userFlow.collectAsState(initial = User("", "", "", ""))

    var name by remember { mutableStateOf(savedUser.name) }
    var email by remember { mutableStateOf(savedUser.email) }
    var password by remember { mutableStateOf(savedUser.password) }
    var passwordVisible by remember { mutableStateOf(false) }
    var pickedImageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(contract = GetContent()) {
        it?.let { pickedImageUri = it }
    }

    LaunchedEffect(savedUser.photoUrl) {
        pickedImageUri = if (savedUser.photoUrl.isNotEmpty()) savedUser.photoUrl.toUri() else null
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.logic))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

    val scroll = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.92f),
            elevation = CardDefaults.cardElevation(10.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(scroll),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LottieAnimation(composition, progress, modifier = Modifier.size(160.dp))

                Spacer(modifier = Modifier.height(16.dp))

                // Avatar Picker
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    val avatarModifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)

                    if (pickedImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(pickedImageUri),
                            contentDescription = null,
                            modifier = avatarModifier
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.avtar),
                            contentDescription = null,
                            modifier = avatarModifier
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text("Welcome Back", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("Please login to continue", fontSize = 16.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.ArrowForward else Icons.Default.ArrowBack,
                                contentDescription = null
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        scope.launch {
                            userPrefs.saveUser(
                                name,
                                pickedImageUri?.toString() ?: "",
                                email,
                                password
                            )
                            navHostController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    },
                    shape = CircleShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text("Login", fontSize = 18.sp)
                }
            }
        }
    }
}

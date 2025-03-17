package com.example.alarmclock

import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*
import com.example.alarmclock.ui.theme.AlarmClockTheme

/**
 * Main activity class - entry point of the application
 * Sets up the Compose UI with the AlarmClockTheme
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge display for a more immersive UI
        enableEdgeToEdge()
        setContent {
            AlarmClockTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AlarmClockApp()
                }
            }
        }
    }
}

/**
 * Main composable function that builds the alarm clock UI
 * @param modifier Modifier for customizing the layout
 */
@Composable
fun AlarmClockApp(modifier: Modifier = Modifier) {
    // Get current context for Toast and Intent operations
    val context = LocalContext.current

    // State variables that survive configuration changes
    var selectedTime by rememberSaveable { mutableStateOf("No time selected") }
    var selectedHour by rememberSaveable { mutableStateOf(-1) } // -1 indicates no selection
    var selectedMinute by rememberSaveable { mutableStateOf(-1) } // -1 indicates no selection
    var alarmMessage by rememberSaveable { mutableStateOf("Wake up for class!") }

    // Load background image from resources
    val image = painterResource(R.drawable.pexels_photo_1366919)

    // Root container with background image
    Box(modifier = modifier.fillMaxSize()) {
        // Background image that fills the screen
        Image(
            painter = image,
            contentDescription = null, // Decorative image doesn't need description
            contentScale = ContentScale.Crop,
            modifier = modifier.fillMaxSize()
        )

        // Main content column centered on screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Card containing all interactive elements
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                // Content inside the card
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // App title
                    Text(
                        text = "Set Alarm Time",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Display selected time or default message
                    Text(
                        text = selectedTime,
                        fontSize = 25.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    // Input field for alarm message
                    OutlinedTextField(
                        value = alarmMessage,
                        onValueChange = { alarmMessage = it },
                        label = { Text("Enter your alarm message", fontSize = 15.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        singleLine = true,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Button to select time - Step 1 in the flow
                    Button(
                        onClick = {
                            // Show time picker dialog
                            showTimePicker(context) { hour, minute, canceled ->
                                if (!canceled) {
                                    // Update state with selected time if not canceled
                                    selectedHour = hour
                                    selectedMinute = minute
                                    selectedTime = String.format("Selected Time: %02d:%02d", hour, minute)
                                } else {
                                    // Reset state if time picker was canceled
                                    selectedTime = "No time selected"
                                    selectedHour = -1
                                    selectedMinute = -1
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Select Time",
                            fontSize = 20.sp
                        )
                    }

                    // Button to set the alarm - Step 2 in the flow
                    Button(
                        onClick = {
                            // Only set alarm if time was selected
                            if (selectedHour >= 0 && selectedMinute >= 0) {
                                setAlarm(context, selectedHour, selectedMinute, alarmMessage)
                            } else {
                                // Inform user if no time was selected
                                Toast.makeText(context, "Please select a time first", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        // Disable button if no time selected
                        enabled = selectedHour >= 0 && selectedMinute >= 0
                    ) {
                        Text(
                            text = "Set Alarm",
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Shows the time picker dialog and handles the result
 * @param context Android context required for showing dialog
 * @param onTimeSelected Callback function that receives the selected time or cancellation
 */
fun showTimePicker(context: Context, onTimeSelected: (Int, Int, Boolean) -> Unit) {
    // Get current time for dialog's default values
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    try {
        // Create and configure time picker dialog
        val dialog = TimePickerDialog(
            context,
            // Callback when time is selected
            { _, selectedHour, selectedMinute ->
                onTimeSelected(selectedHour, selectedMinute, false)
            },
            hour,
            minute,
            true // Use 24h format
        )

        // Handle dialog cancellation
        dialog.setOnCancelListener {
            onTimeSelected(-1, -1, true)
        }

        // Show the dialog
        dialog.show()
    } catch (e: Exception) {
        // Handle any errors when showing the dialog
        Toast.makeText(context, "Failed to open time picker", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Creates an alarm using the system alarm clock
 * @param context Android context required for launching intent
 * @param hour Hour for the alarm (24h format)
 * @param minute Minute for the alarm
 * @param message Custom message for the alarm
 */
fun setAlarm(context: Context, hour: Int, minute: Int, message: String) {
    // Create intent to launch system alarm clock
    val alarmIntent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
        // Set alarm details as extras
        putExtra(AlarmClock.EXTRA_HOUR, hour)
        putExtra(AlarmClock.EXTRA_MINUTES, minute)
        putExtra(AlarmClock.EXTRA_MESSAGE, message)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        // Launch the system alarm clock
        context.applicationContext.startActivity(alarmIntent)
        // Confirm to user that alarm was set
        Toast.makeText(context, "Alarm set for $hour:$minute", Toast.LENGTH_SHORT).show()
    } catch (e: ActivityNotFoundException) {
        // Handle case where no alarm app is available
        Toast.makeText(context, "No alarm app available", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Preview function for Android Studio's Layout Preview
 * Shows how the UI will look during development
 */
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AlarmClockTheme {
        AlarmClockApp()
    }
}
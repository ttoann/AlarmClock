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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

@Composable
fun AlarmClockApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var selectedTime by rememberSaveable { mutableStateOf("No time selected") }
    var selectedHour by rememberSaveable { mutableStateOf(-1) }
    var selectedMinute by rememberSaveable { mutableStateOf(-1) }
    var alarmMessage by rememberSaveable { mutableStateOf("Wake up for class!") }
    val image = painterResource(R.drawable.pexels_photo_1366919)

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Set Alarm Time",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = selectedTime,
                        fontSize = 25.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

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

                    // Step 1: Select time button
                    Button(
                        onClick = {
                            showTimePicker(context) { hour, minute, canceled ->
                                if (!canceled) {
                                    selectedHour = hour
                                    selectedMinute = minute
                                    selectedTime = String.format("Selected Time: %02d:%02d", hour, minute)
                                } else {
                                    // Handle cancellation
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

                    // Step 2: Set alarm button
                    Button(
                        onClick = {
                            if (selectedHour >= 0 && selectedMinute >= 0) {
                                setAlarm(context, selectedHour, selectedMinute, alarmMessage)
                            } else {
                                Toast.makeText(context, "Please select a time first", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
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

fun showTimePicker(context: Context, onTimeSelected: (Int, Int, Boolean) -> Unit) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    try {
        val dialog = TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                onTimeSelected(selectedHour, selectedMinute, false)
            },
            hour,
            minute,
            true
        )

        // Handle cancellation
        dialog.setOnCancelListener {
            onTimeSelected(-1, -1, true)
        }

        dialog.show()
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to open time picker", Toast.LENGTH_SHORT).show()
    }
}

fun setAlarm(context: Context, hour: Int, minute: Int, message: String) {
    val alarmIntent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
        putExtra(AlarmClock.EXTRA_HOUR, hour)
        putExtra(AlarmClock.EXTRA_MINUTES, minute)
        putExtra(AlarmClock.EXTRA_MESSAGE, message)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        context.applicationContext.startActivity(alarmIntent)
        Toast.makeText(context, "Alarm set for $hour:$minute", Toast.LENGTH_SHORT).show()
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No alarm app available", Toast.LENGTH_SHORT).show()
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AlarmClockTheme {
        AlarmClockApp()
    }
}
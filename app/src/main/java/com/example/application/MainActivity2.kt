package com.example.application

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.application.ui.theme.ApplicationTheme
import com.example.application.ui.theme.DarkColorScheme
import com.example.application.ui.theme.LightColorScheme
import com.example.application.ui.theme.Typography

class MainActivity2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainActivityContent("Android")
                }
            }
        }
    }
}

@Composable
fun MainActivityContent(name: String, modifier: Modifier = Modifier) {
    Column {
        Header(image = R.drawable.ic_launcher_background, content = "Kitkat")
        Spacer(modifier = Modifier.height(30.dp))
        Text("Hello my brother")
        TemperatureText(celsius = 20)
        ConvertButton {}
    }
}

@Composable
fun GreetingPreview() {
    val celsius = remember {
        mutableStateOf(0)
    }

    val newCelsius = remember {
        mutableStateOf("")
    }

    TemperatureConverterTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Header(image = R.drawable.ic_launcher_background, content = "Kitkat")
                Spacer(modifier = Modifier.height(30.dp))
                TextField(
                    label = {
                        Text("Enter the celsius value...")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    value = newCelsius.value, onValueChange = {value ->
                        newCelsius.value = when(value) {
                            null -> ""
                            else -> value
                        }
                    })
                TemperatureText(celsius = celsius.value)
                ConvertButton {
                    newCelsius.value.toIntOrNull()?.let {
                        celsius.value = it
                    }
                }
            }
        }

    }
}

@Composable
fun TemperatureConverterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
    ) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    }else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )


}

@Composable
fun Header(image: Int, content: String) {
    Image(
        modifier = Modifier
            .height(120.dp)
            .padding(10.dp)
            .clip(RoundedCornerShape(20.dp))
            .fillMaxWidth(),
        painter = painterResource(id = R.drawable.ic_launcher_background),
        contentScale = ContentScale.Crop,
        contentDescription = "Icon call answer")
}

@Composable
fun TemperatureText(celsius: Int) {
    val farenheit = (celsius.toDouble() * 9/5) + 32
    Text("$celsius celsius is $farenheit farenheit.")
}

@Composable
fun ConvertButton(onClick: () -> Unit) {
    Button(modifier = Modifier.padding(8.dp), onClick = onClick) {
        Text("Convert")
    }
}
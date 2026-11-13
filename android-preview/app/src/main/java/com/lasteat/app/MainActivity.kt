package com.lasteat.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lasteat.app.ui.customer.CustomerPreviewApp
import com.lasteat.app.ui.customer.CustomerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { RolePreviewApp() }
    }
}

private enum class PreviewRole { Customer, Owner }

@Composable
private fun RolePreviewApp() {
    var role by rememberSaveable { mutableStateOf<PreviewRole?>(null) }
    val customerState = rememberSaveableStateHolder()
    val ownerState = remember { mutableStateOf(sampleState()) }
    val showRolePicker = { role = null }

    BackHandler(enabled = role != null, onBack = showRolePicker)

    when (role) {
        PreviewRole.Customer -> customerState.SaveableStateProvider(PreviewRole.Customer.name) {
            CustomerPreviewApp()
        }
        PreviewRole.Owner -> OwnerPreviewApp(ownerState, showRolePicker)
        null -> CustomerTheme {
            Surface(Modifier.fillMaxSize()) {
                Box(Modifier.safeDrawingPadding().fillMaxSize().padding(24.dp), Alignment.Center) {
                    Column(Modifier.widthIn(max = 360.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("LastEat", style = MaterialTheme.typography.headlineLarge)
                        Text("어떤 역할로 시작할까요?", style = MaterialTheme.typography.bodyLarge)
                        Button(onClick = { role = PreviewRole.Customer }, modifier = Modifier.fillMaxWidth()) {
                            Text("손님으로 시작")
                        }
                        OutlinedButton(onClick = { role = PreviewRole.Owner }, modifier = Modifier.fillMaxWidth()) {
                            Text("사장님으로 시작")
                        }
                    }
                }
            }
        }
    }
}

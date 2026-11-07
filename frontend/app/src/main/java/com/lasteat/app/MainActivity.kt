package com.lasteat.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.lasteat.app.ui.AppRoot
import com.lasteat.app.ui.components.DefaultImageContent
import com.lasteat.app.ui.components.LocalImageContent
import com.lasteat.app.ui.theme.LastEatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LastEatTheme {
                // 이미지 로더를 정하면 여기서 한 번만 교체하세요. 예) Coil:
                // LocalImageContent provides { url, desc, modifier ->
                //     AsyncImage(model = url, contentDescription = desc, modifier = modifier, contentScale = ContentScale.Crop)
                // }
                CompositionLocalProvider(LocalImageContent provides DefaultImageContent) {
                    Surface(Modifier.fillMaxSize()) {
                        AppRoot()
                    }
                }
            }
        }
    }
}

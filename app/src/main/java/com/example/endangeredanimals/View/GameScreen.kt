package com.example.endangeredanimals.View

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.endangeredanimals.R

@Composable
// Bước 1: Thêm NavController làm tham số
fun GameScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        GameButton(
            imageRes = R.drawable.button_habitat,
            onClick = { /* TODO: Điều hướng đến Game 1 */ }
        )

        Spacer(modifier = Modifier.height(24.dp))

        GameButton(
            imageRes = R.drawable.button_conservation,
            // Bước 2: Thay đổi hành động onClick để điều hướng đến "inden_game"
            onClick = { navController.navigate("inden_game") }
        )
    }
}

@Composable
fun GameButton(
    imageRes: Int,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(150.dp),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Game Button",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
        )
    }
}

// Hàm Preview để xem trước giao diện
@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    // Tạo một NavController giả cho Preview
    val navController = rememberNavController()
    GameScreen(navController = navController)
}


package com.example.endangeredanimals.View

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.endangeredanimals.ui.AppWarningColor
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class FavoriteAnimal(
    val id: Int,
    val name: String,
    val imageRes: Int
)

@Composable
fun LoveScreen() {
    val favoriteAnimals = remember {
        listOf(
            FavoriteAnimal(
                id = 1,
                name = "Một loài động vật",
                imageRes = com.example.endangeredanimals.R.drawable.avata
            )
        )
    }

    if (favoriteAnimals.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Danh sách yêu thích của bạn đang trống.",
                fontSize = 18.sp,
                color = Color.Gray
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(favoriteAnimals) { animal ->
                FavoriteAnimalItem(animal = animal)
            }
        }
    }
}

@Composable
fun FavoriteAnimalItem(animal: FavoriteAnimal) {
    var isFavorite by remember { mutableStateOf(true) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = animal.imageRes),
                contentDescription = animal.name,
                contentScale = ContentScale.Crop, // Đảm bảo ảnh lấp đầy không gian
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = animal.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Yêu thích/Bỏ yêu thích",
                tint = if (isFavorite) AppWarningColor else Color.Black,
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        isFavorite = !isFavorite
                    }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoveScreenPreview() {
    LoveScreen()
}

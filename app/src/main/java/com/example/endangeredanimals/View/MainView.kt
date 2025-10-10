@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.endangeredanimals.View

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.endangeredanimals.R
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ViewMain() {
    var text by remember { mutableStateOf("") }
    var selectItem by remember { mutableStateOf(0) }
    val muc = listOf("Home", "Game", "Love", "Profile")
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = text,
                            onValueChange = { text = it },
                            placeholder = { Text("Nhập tên động vật", style = TextStyle(color = Color.White, fontSize = 15.sp)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Tìm kiếm",
                                    tint = Color.White
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(7.dp),
                            maxLines = 1,
                            shape = RoundedCornerShape(25.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White,
                                unfocusedBorderColor = Color.White,
                                focusedBorderColor = Color.White,
                                cursorColor = Color.White
                            )
                        )
                        Button(
                            onClick = { println("dmmmm") },
                            modifier = Modifier.padding(start = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14752b))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.filter),
                                    contentDescription = "Lọc",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.size(4.dp))
                                Text("Lọc")
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF37ab3c)),
                modifier = Modifier
                    .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                    .fillMaxWidth()
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)),
                containerColor = Color(0xFFDDDDDD),
                contentColor = Color.Black,
                tonalElevation = 25.dp
            ) {
                muc.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ){
                                Icon(
                                    painter = painterResource(id = when (index){
                                        0 -> R.drawable.home
                                        1 -> R.drawable.game
                                        2 -> R.drawable.favorite
                                        3 -> R.drawable.profile
                                        else -> R.drawable.home
                                    }),
                                    contentDescription = item,
                                    tint = if (selectItem == index) Color.Black else Color.Gray
                                )
                            }
                        },
                        onClick = {selectItem = index},
                        selected = selectItem == index,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent,
                            selectedIconColor = Color.Black,
                            unselectedTextColor = Color.Black
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.White),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            item(span = { GridItemSpan(maxLineSpan) }) {
                Column { // Bọc trong Column để có thể thêm các modifier nếu cần
                    var selectedCategoryIndex by remember { mutableStateOf(-1) }
                    val list = listOf(
                        "Cầy", "Gà", "Nhông", "Vịt", "Thạch Sùng",
                        "Khỉ", "Voọc", "Vượn", "Chuột", "Dơi"
                    )
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(list) { index, item ->
                            val isSelected = selectedCategoryIndex == index
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {
                                        selectedCategoryIndex = index
                                        println("Đã chọn: $item")
                                    },
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) Color(0xFF37ab3c) else Color.Transparent,
                                border = BorderStroke(1.dp, Color(0xFF37ab3c))
                            ) {
                                Text(
                                    text = item,
                                    color = if (isSelected) Color.White else Color.Black,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Card(
                    modifier = Modifier.padding(bottom = 8.dp), // Thêm padding dưới
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFebebeb))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val imageList = listOf(R.drawable.lion, R.drawable.pubg, R.drawable.ghost)
                        val pagerState = rememberPagerState { imageList.size }
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentPadding = PaddingValues(horizontal = 32.dp)
                        ) { page ->
                            Card(
                                modifier = Modifier
                                    .graphicsLayer {
                                        val pageOffset = pagerState.getOffsetDistanceInPages(page)
                                        scaleX = 0.85f + 0.15f * (1f - pageOffset.absoluteValue)
                                        scaleY = 0.85f + 0.15f * (1f - pageOffset.absoluteValue)
                                        alpha = 0.5f + 0.5f * (1f - pageOffset.absoluteValue)
                                    }
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = imageList[page]),
                                    contentDescription = "Slide $page",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        Row(
                            Modifier
                                .height(30.dp)
                                .padding(top = 10.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(imageList.size) { iteration ->
                                val color = if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .size(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            // -- MỤC 3: DANH SÁCH ĐỘNG VẬT --
            // Giả sử bạn có một danh sách động vật để hiển thị
            val animalItems = (1..20).toList()
            items(animalItems) { animalId ->
                Card(
                    modifier = Modifier.height(150.dp),
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // Thay thế bằng nội dung thực của bạn, ví dụ: Ảnh và Tên
                        Text("Động vật $animalId", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false},
            sheetState = bottomSheetState,
            containerColor = Color(0xFFd3e3d7)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Chi tiet",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                Text(
                    text = "Tu vua nhap: $text ",
                    fontSize = 15.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 5.dp)
                )
                Image(
                    painter = painterResource(R.drawable.avata),
                    contentDescription = "anh thu",
                    modifier = Modifier
                        .size(80.dp)
                        .padding(10.dp)
                )
                Button(
                    onClick = {
                        scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                            if (!bottomSheetState.isVisible) showBottomSheet = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFab310f)),
                ) {
                    Text("Dong", color = Color.White)
                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun PreviewMainView() {
    ViewMain()
}
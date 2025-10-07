@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.endangeredanimals.View

import android.annotation.SuppressLint
import android.content.ClipData.Item
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.endangeredanimals.R
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ViewMain() {
    var text by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    var selectItem by remember { mutableStateOf(0) }
    val muc = listOf("Home", "Game", "Love", "Profile")
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = { Text("Nhập tên động vật") },
                        modifier = Modifier
                            .padding(3.dp)
                            .fillMaxWidth(),
                        maxLines = 1
                    )
                    Button(
                        onClick = { println("dmmmm") },
                        modifier = Modifier
                            .padding(10.dp)
                            ,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF228cdb))
                    ) {

                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFc28442)),
                modifier = Modifier
//                    .border(
//                        color = Color.Gray,
//                        width = 2.dp,
//                        shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)
//                )
                    .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
//                    .border(
//                        width = 2.dp,
//                        color = Color.Gray,
//                        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
//                    )
                ,
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
        Box(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White),
            contentAlignment = Alignment.TopCenter,
        ){
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(7.dp)
                    .background(Color.White)
            ) {
                Card(
                    modifier = Modifier
                        .padding(7.dp)
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFebebeb))
                ) {
                    Column(modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.CenterHorizontally)) {
                        Image(
                            painter = painterResource(R.drawable.avata2),
                            contentDescription = "thotrong",
                            modifier = Modifier.padding(10.dp)
                        )
                        Text(
                            text = "Thỏ cơ bắp ",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.Black)
                        Text(
                            text = "Cháu ông Năm",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black)
                    }
                    Row(modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { println("dmmmm") },
                            modifier = Modifier
                                .padding(5.dp)
                                .weight(1f)
                        ) {
                            Text("AAAAAAAA")
                        }
                        Spacer(modifier = Modifier.size(25.dp))
                        Button(
                            onClick = { println("gasd") },
                            modifier = Modifier
                                .padding(5.dp)
                                .weight(1f)
                        ) {
                            Text("BBBBBBBB")
                        }
                    }
                }
                Text(
                    text = "Nhập tên:",
                    modifier = Modifier.padding(start = 8.dp),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(5.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Nhập bất cứ thứ gì vào") },
                    modifier = Modifier
                        .padding(3.dp)
                        .fillMaxWidth(),
                    maxLines = 2,
                    textStyle = TextStyle(color = Color.Black)
                )
                Button(
                    onClick = { scope.launch { showBottomSheet = true } },
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF228cdb))
                ) {
                    Text("OK")
                }
                Box(
                    modifier = Modifier.size(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.avata),
                        modifier = Modifier
                            .size(300.dp)
                            .align(Alignment.Center),
                        contentDescription = "anh tho"
                    )
                }
                val list = listOf(
                    Item("aaaa"),
                    Item("bbbb"),
                    Item("cccc"),
                    Item("aaaa"),
                    Item("bbbb"),
                    Item("cccc"),
                    Item("aaaa"),
                    Item("bbbb"),
                    Item("cccc")
                )
                LazyRow(
                    modifier = Modifier.padding(5.dp),
                    contentPadding = PaddingValues(10.dp)
                ) {
                    items(list) { item ->
                        Text(
                            text = "${item.text}",
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
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
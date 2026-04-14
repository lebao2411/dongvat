@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.endangeredanimals.View

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.endangeredanimals.ViewModel.FavoriteViewModel

@Composable
fun FavoriteScreen(
    navController: NavController,
    favoriteViewModel: FavoriteViewModel = viewModel() 
) {
    val favoriteAnimals by favoriteViewModel.favoriteAnimals.collectAsState()
    val isLoading by favoriteViewModel.isLoading.collectAsState()
    val isRefreshing by favoriteViewModel.isRefreshing.collectAsState()
    
    val pullToRefreshState = rememberPullToRefreshState()

    // Sử dụng Surface để có nền đặc, tránh hiện chồng màn hình
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = { favoriteViewModel.refresh() },
            modifier = Modifier.fillMaxSize(),
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = isRefreshing,
                    containerColor = Color.White,
                    color = Color(0xFF37ab3c),
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        ) {
            if (isLoading && favoriteAnimals.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF37ab3c))
                }
            } else if (favoriteAnimals.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Bạn chưa có động vật yêu thích nào.",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Động vật yêu thích của bạn",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 16.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )

                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        verticalItemSpacing = 8.dp,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(favoriteAnimals, key = { it.animalID ?: it.hashCode() }) { animal ->
                            AnimalGridItem(
                                animal = animal,
                                onItemClick = {
                                    val id = animal.animalID
                                    if (!id.isNullOrBlank()) {
                                        // Mã hóa ID để tránh lỗi Route
                                        val encodedId = Uri.encode(id)
                                        navController.navigate("animal_screen/$encodedId")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.example.endangeredanimals.View

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.endangeredanimals.Model.Animal
import com.example.endangeredanimals.R
import com.example.endangeredanimals.ViewModel.HomeViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.math.absoluteValue
import com.google.gson.Gson

@Composable
fun HomeScreen(
    navController: NavHostController,
    homeViewModel: HomeViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val animalItems by homeViewModel.animalItems.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()

    if (isLoading && animalItems.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp)
                .background(Color.White),
            contentPadding = WindowInsets.navigationBars.asPaddingValues(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                CategorySuggestionRow(
                    categories = homeViewModel.suggestedCategories,
                    onCategoryClick = { category ->
                        val encodedCategory = URLEncoder.encode(category, StandardCharsets.UTF_8.toString())
                        navController.navigate("result_screen?category=$encodedCategory")
                    }
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                ImageSlider()
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Khám phá thế giới động vật",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 8.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            items(animalItems, key = { it.animalID ?: it.hashCode() }) { animal ->
                AnimalGridItem(
                    animal = animal,
                    onItemClick = {
                        val animalJson = Gson().toJson(animal)
                        val encodedJson = URLEncoder.encode(animalJson, StandardCharsets.UTF_8.toString())

                        navController.navigate("animal_screen/${animal.animalID}")
                    }
                )
            }
        }
    }
}

@Composable
private fun CategorySuggestionRow(
    categories: List<String>,
    onCategoryClick: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        contentPadding = PaddingValues(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onCategoryClick(category) },
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, Color(0xFF37ab3c))
            ) {
                Text(
                    text = category,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ImageSlider() {
    Card(
        modifier = Modifier.padding(bottom = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFebebeb))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val imageList = listOf(R.drawable.slide1, R.drawable.slide2, R.drawable.slide3)
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

@Composable
fun AnimalGridItem(
    animal: Animal,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(
                model = animal.imageUrl,
                placeholder = painterResource(id = R.drawable.save_animal),
                error = painterResource(id = R.drawable.save_animal),
                contentDescription = animal.nameVn,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth()
            )
            animal.nameVn?.let {
                Text(
                    text = it,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}



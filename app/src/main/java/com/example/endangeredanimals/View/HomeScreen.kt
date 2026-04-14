@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.example.endangeredanimals.View

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.endangeredanimals.Model.Animal
import com.example.endangeredanimals.R
import com.example.endangeredanimals.ViewModel.HomeViewModel
import com.example.endangeredanimals.ui.AppBackgroundCard
import kotlinx.coroutines.delay
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun HomeScreen(
    navController: NavHostController,
    homeViewModel: HomeViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val randomAnimalItems by homeViewModel.randomAnimalItems.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    val isRefreshing by homeViewModel.isRefreshing.collectAsState()
    
    val pullToRefreshState = rememberPullToRefreshState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = { homeViewModel.refresh() },
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
            if (isLoading && randomAnimalItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF37ab3c))
                }
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    contentPadding = WindowInsets.navigationBars.asPaddingValues(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalItemSpacing = 8.dp
                ) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Spacer(modifier = Modifier.height(5.dp).fillMaxWidth())
                    }

                    item(span = StaggeredGridItemSpan.FullLine) {
                        ImageSlider()
                    }

                    item(span = StaggeredGridItemSpan.FullLine) {
                        CategorySuggestionRow(
                            categories = homeViewModel.suggestedCategories,
                            onCategoryClick = { category ->
                                val encodedCategory = URLEncoder.encode(category, StandardCharsets.UTF_8.toString())
                                navController.navigate("result_screen?category=$encodedCategory")
                            }
                        )
                    }

                    item(span = StaggeredGridItemSpan.FullLine) {
                        Text(
                            text = "Khám phá thế giới động vật",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp, top = 5.dp, bottom = 8.dp)
                        )
                    }

                    items(randomAnimalItems, key = { it.animalID?.toString() ?: it.hashCode().toString() }) { animal ->
                        AnimalGridItem(
                            animal = animal,
                            onItemClick = {
                                val id = animal.animalID?.toString()
                                if (!id.isNullOrBlank()) {
                                    val encodedId = Uri.encode(id)
                                    navController.navigate("animal_screen/$encodedId")
                                }
                            }
                        )
                    }

                    item(span = StaggeredGridItemSpan.FullLine) {
                        DisclaimerText()
                    }
                }
            }
        }
    }
}

@Composable
private fun DisclaimerText() {
    val uriHandler = LocalUriHandler.current
    val url = "http://vnredlist.vast.vn/"

    Text(
        text = buildAnnotatedString {
            append("Thông tin được lấy từ ")
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) {
                append("vnredlist.vn")
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { uriHandler.openUri(url) },
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodySmall
    )
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

            LaunchedEffect(Unit) {
                while (true) {
                    delay(10000L)
                    val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
                    pagerState.animateScrollToPage(nextPage)
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) { page ->
                Image(
                    painter = painterResource(id = imageList[page]),
                    contentDescription = "Slide $page",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
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
        elevation = CardDefaults.cardElevation(5.dp),
        colors = CardDefaults.cardColors(containerColor = AppBackgroundCard)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(
                model = animal.imageUrl,
                placeholder = painterResource(id = R.drawable.loading),
                error = painterResource(id = R.drawable.noimage),
                contentDescription = animal.nameVn,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
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

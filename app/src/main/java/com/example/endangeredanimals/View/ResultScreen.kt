package com.example.endangeredanimals.View

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.endangeredanimals.Model.Animal
import com.example.endangeredanimals.R
import com.example.endangeredanimals.ViewModel.ResultViewModel
import com.example.endangeredanimals.ui.AppBackgroundCard
import com.example.endangeredanimals.ui.AppPrimaryColor
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    navController: NavController,
    resultViewModel: ResultViewModel = viewModel()
) {
    val searchResults by resultViewModel.searchResults.collectAsState()
    val isLoading by resultViewModel.isLoading.collectAsState()
    val message by resultViewModel.searchMessage.collectAsState()
    val searchQuery by resultViewModel.searchQuery.collectAsState()

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Tự động tìm kiếm khi searchQuery thay đổi
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            delay(300)
            resultViewModel.searchAnimals(searchQuery)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Thanh tìm kiếm
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppPrimaryColor)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = Color.White
                )
            }
            TextField(
                value = searchQuery,
                onValueChange = { resultViewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                placeholder = { Text("Nhập tên động vật...") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(32.dp)
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF37ab3c))
            }
        } else if (searchResults.isNotEmpty()) {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalItemSpacing = 8.dp
            ) {
                items(searchResults) { animal ->
                    ResultCard(animal = animal) { selectedAnimal ->
                        val animalId = selectedAnimal.animalID
                        if (!animalId.isNullOrBlank()) {
                            navController.navigate("animal_screen/$animalId")
                        } else {
                            Log.e("ResultScreen", "Animal ID is null or blank, cannot navigate.")
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ResultCard(
    animal: Animal,
    onClick: (Animal) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(animal) },
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.cardColors(containerColor = AppBackgroundCard)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val imageModel = if (animal.imageUrl.isNullOrBlank() || animal.imageUrl == "Không rõ") {
                R.drawable.noimage
            } else {
                animal.imageUrl
            }
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageModel)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.loading),
                error = painterResource(R.drawable.noimage),
                contentDescription = animal.nameVn,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = animal.nameVn ?: "Chưa có tên",
                modifier = Modifier.padding(vertical = 8.dp),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

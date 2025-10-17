package com.example.endangeredanimals.Model

data class Activity(
    val activityID: String = "",
    val animalID: String = "", // Có thể liên quan đến một con vật cụ thể
    val activityType: String = "",
    val title: String = ""
)
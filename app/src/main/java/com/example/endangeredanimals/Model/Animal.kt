package com.example.endangeredanimals.Model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class Animal(
    @get:Exclude var animalID: String = "",

    val nameVn: String = "",
    val nameLatin: String = "",
    val status: String = "",
    @get:PropertyName("class")
    @set:PropertyName("class")
    var className: String = "",
    val species: String = "",
    val location: String = "",
    val popStatus: String = "",
    val popTrend: String = "",
    val habitatFeat: String = "",
    val habitatType: String = "",
    val reproduction: String = "",
    val diet: String = "",
    val threats: String = "",
    val imageUrl: String = ""
)
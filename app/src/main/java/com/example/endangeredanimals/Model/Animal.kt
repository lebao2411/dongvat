package com.example.endangeredanimals.Model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Animal(
    @SerialName("animalId")
    var animalID: String? = null,
    @SerialName("nameVn")
    var nameVn: String? = null,
    @SerialName("nameLatin")
    var nameLatin: String? = null,
    @SerialName("otherNames")
    var otherNames: String? = null,
    @SerialName("status")
    var status: String? = null,
    @SerialName("animalGroup")
    var animalGroup: String? = null,
    @SerialName("species")
    var species: String? = null,
    @SerialName("location")
    var location: String? = null,
    @SerialName("popStatus")
    var popStatus: String? = null,
    @SerialName("popTrend")
    var popTrend: String? = null,
    @SerialName("habitatFeat")
    var habitatFeat: String? = null,
    @SerialName("habitatType")
    var habitatType: String? = null,
    @SerialName("reproduction")
    var reproduction: String? = null,
    @SerialName("diet")
    var diet: String? = null,
    @SerialName("threats")
    var threats: String? = null,
    @SerialName("imageUrl")
    var imageUrl: String? = null
)

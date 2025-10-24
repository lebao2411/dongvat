package com.example.endangeredanimals.Model

import com.google.gson.annotations.SerializedName

data class Animal(
    @SerializedName("animalId")
    var animalID: String? = null,

    @SerializedName("nameVn")
    val nameVn: String? = null,

    @SerializedName("nameLatin")
    val nameLatin: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("class")
    val animalClass: String? = null,

    @SerializedName("species")
    val species: String? = null,

    @SerializedName("location")
    val location: String? = null,

    @SerializedName("popStatus")
    val popStatus: String? = null,

    @SerializedName("popTrend")
    val popTrend: String? = null,

    @SerializedName("habitatFeat")
    val habitatFeat: String? = null,

    @SerializedName("habitatType")
    val habitatType: String? = null,

    @SerializedName("reproduction")
    val reproduction: String? = null,

    @SerializedName("diet")
    val diet: String? = null,

    @SerializedName("threats")
    val threats: String? = null,

    @SerializedName("imageUrl")
    val imageUrl: String? = null,

) {
    constructor() : this(
        null, null, null, null, null,
        null, null, null, null, null,
        null, null, null, null, null
    )
}

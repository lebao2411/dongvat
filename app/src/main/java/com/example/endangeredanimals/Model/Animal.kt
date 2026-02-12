package com.example.endangeredanimals.Model

import androidx.annotation.Keep

@Keep
data class Animal(
    var animalID: String? = null,
    var nameVn: String? = null,
    var nameLatin: String? = null,
    var status: String? = null,
    var animalGroup: String? = null,
    var species: String? = null,
    var location: String? = null,
    var popStatus: String? = null,
    var popTrend: String? = null,
    var habitatFeat: String? = null,
    var habitatType: String? = null,
    var reproduction: String? = null,
    var diet: String? = null,
    var threats: String? = null,
    var imageUrl: String? = null,

) {
    constructor() : this(
        null, null, null, null, null,
        null, null, null, null, null,
        null, null, null, null, null
    )
}

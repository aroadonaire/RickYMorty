package dam.pmdm.rickymorty

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Episode(
    val id: Int,
    val name: String,
    @com.google.gson.annotations.SerializedName("episode")
    val code: String,
    @com.google.gson.annotations.SerializedName("air_date")
    val airDate: String,    
    var watched: Boolean = false,
    val characters: List<String> = emptyList()
) : Parcelable
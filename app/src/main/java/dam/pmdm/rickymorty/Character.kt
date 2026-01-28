package dam.pmdm.rickymorty

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Character(
    val id: Int,
    val name: String,
    val image: String,
    val species: String,
    val status: String
) : Parcelable
package dam.pmdm.rickymorty

import retrofit2.http.GET
import retrofit2.http.Url

interface ApiService {
    @GET("episode")
    suspend fun getEpisodes(): EpisodeResponse

    @GET
    suspend fun getCharacter(@Url url: String): Character
}
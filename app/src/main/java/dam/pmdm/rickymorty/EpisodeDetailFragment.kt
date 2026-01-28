package dam.pmdm.rickymorty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.lifecycle.lifecycleScope


class EpisodeDetailFragment : Fragment() {

    private lateinit var tvName: TextView
    private lateinit var tvCode: TextView
    private lateinit var tvAirDate: TextView
    private lateinit var switchWatched: Switch
    private lateinit var rvCharacters: RecyclerView
    private lateinit var characterAdapter: CharacterAdapter
    private val characterList = mutableListOf<Character>()

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private var episode: Episode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            episode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable("episode", Episode::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.getParcelable("episode")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_episode_detail, container, false)

        tvName = view.findViewById(R.id.tvName)
        tvCode = view.findViewById(R.id.tvCode)
        tvAirDate = view.findViewById(R.id.tvAirDate)
        switchWatched = view.findViewById(R.id.switchWatched)
        rvCharacters = view.findViewById(R.id.rvCharacters)

        characterAdapter = CharacterAdapter(characterList)
        rvCharacters.layoutManager = GridLayoutManager(requireContext(), 2)
        rvCharacters.adapter = characterAdapter
        
        episode?.let {
            tvName.text = it.name
            tvCode.text = it.code
            tvAirDate.text = it.airDate
            checkWatchedStatus(it.id.toString())
            fetchCharacters(it.characters)
        }

        switchWatched.setOnCheckedChangeListener { _, isChecked ->
            saveWatchedStatus(isChecked)
        }

        return view
    }

    private fun checkWatchedStatus(episodeId: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .collection("watched_episodes").document(episodeId)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        switchWatched.isChecked = doc.getBoolean("watched") ?: false
                    }
                }
        }
    }

    private fun saveWatchedStatus(isChecked: Boolean) {
        val userId = auth.currentUser?.uid
        val episodeId = episode?.id.toString()
        if (userId != null && episodeId != "null") {
            val data = hashMapOf("watched" to isChecked)
            db.collection("users").document(userId)
                .collection("watched_episodes").document(episodeId).set(data).addOnSuccessListener {
                    episode?.watched = isChecked
                }
        }
    }

    private fun fetchCharacters(urls: List<String>) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://rickandmortyapi.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ApiService::class.java)

        lifecycleScope.launch {
            urls.forEach { url ->
                try {
                    val character = api.getCharacter(url)
                    characterList.add(character)
                    characterAdapter.notifyItemInserted(characterList.size - 1)
                } catch (e: Exception) {
                    android.util.Log.e("API_ERROR", "Error cargando personaje: ${e.message}")
                }
            }
        }
    }
}
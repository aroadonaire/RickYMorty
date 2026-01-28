package dam.pmdm.rickymorty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Spinner
import android.widget.AdapterView
import retrofit2.Retrofit
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.google.firebase.firestore.ktx.firestore
import android.widget.Toast


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 * Use the [EpisodesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EpisodesFragment : Fragment() {

    private lateinit var rvEpisodes: RecyclerView
    private lateinit var adapter: EpisodeAdapter
    private lateinit var spinnerFilter: Spinner
    private lateinit var btnMarkSelected: android.widget.Button
    private val episodeList = mutableListOf<Episode>()
    private val filteredList = mutableListOf<Episode>()
    private val db = com.google.firebase.ktx.Firebase.firestore
    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_episodes, container, false)

        rvEpisodes = view.findViewById(R.id.rvEpisodes)
        spinnerFilter = view.findViewById(R.id.spinnerFilter)
        btnMarkSelected = view.findViewById(R.id.btnMarkSelected)

        rvEpisodes.layoutManager = LinearLayoutManager(requireContext())

        adapter = EpisodeAdapter(filteredList) { count ->
            if (count > 0) {
                btnMarkSelected.visibility = View.VISIBLE
                btnMarkSelected.text = "Marcar ($count) como vistos"
            } else {
                btnMarkSelected.visibility = View.GONE
            }
        }
        rvEpisodes.adapter = adapter

        btnMarkSelected.setOnClickListener {
            markSelectedAsWatched()
        }

        if (episodeList.isEmpty()) {
            fetchEpisodes()
        } else {
            showAllEpisodes()
        }

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> showAllEpisodes()
                    1 -> showWatchedEpisodes()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        return view
    }

    private fun fetchEpisodes() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://rickandmortyapi.com/api/")
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ApiService::class.java)

        lifecycleScope.launch {
            try {
                val response = api.getEpisodes()
                val remoteEpisodes = response.results
                val userId = auth.currentUser?.uid

                if (userId != null) {
                    db.collection("users").document(userId)
                        .collection("watched_episodes").get()
                        .addOnSuccessListener { snapshot ->
                            val watchedIds = snapshot.documents
                                .filter { it.getBoolean("watched") == true }
                                .map { it.id }

                            remoteEpisodes.forEach { episode ->
                                episode.watched = watchedIds.contains(episode.id.toString())
                            }

                            episodeList.clear()
                            episodeList.addAll(remoteEpisodes)

                            when (spinnerFilter.selectedItemPosition) {
                                0 -> showAllEpisodes()
                                1 -> showWatchedEpisodes()
                            }
                        }
                }
            } catch (e: Exception) {
                android.util.Log.e("API_ERROR", "Error: ${e.message}")
            }
        }
    }

    private fun markSelectedAsWatched() {
        val selected = adapter.getSelectedEpisodes()
        val userId = auth.currentUser?.uid

        if (userId != null && selected.isNotEmpty()) {
            val batch = db.batch()

            selected.forEach { episode ->
                val docRef = db.collection("users").document(userId)
                    .collection("watched_episodes").document(episode.id.toString())
                batch.set(docRef, mapOf("watched" to true))
            }

            batch.commit().addOnSuccessListener {
                Toast.makeText(context, "${selected.size} episodios marcados como vistos", Toast.LENGTH_SHORT).show()
                adapter.clearSelection()
                fetchEpisodes()
            }.addOnFailureListener {
                Toast.makeText(context, "Error al actualizar episodios", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun showAllEpisodes() {
        filteredList.clear()
        filteredList.addAll(episodeList)
        adapter.notifyDataSetChanged()
    }

    private fun showWatchedEpisodes() {
        filteredList.clear()
        filteredList.addAll(episodeList.filter { it.watched })
        adapter.notifyDataSetChanged()

        if (filteredList.isEmpty()) {
            Toast.makeText(context, "No tienes episodios marcados como vistos", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        fetchEpisodes()
    }
}
package dam.pmdm.rickymorty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class StatsFragment : Fragment() {
    private lateinit var tvStats: TextView
    private lateinit var tvCount: TextView
    private lateinit var tvPercentage: TextView
    private lateinit var pbCircle: ProgressBar

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_stats, container, false)

        tvStats = view.findViewById(R.id.tvStats)
        tvCount = view.findViewById(R.id.tvCount)
        tvPercentage = view.findViewById(R.id.tvPercentage)
        pbCircle = view.findViewById(R.id.pbCircle)

        fetchDataAndCalculate()

        return view
    }

    private fun fetchDataAndCalculate() {
        lifecycleScope.launch {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://rickandmortyapi.com/api/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val api = retrofit.create(ApiService::class.java)

                val response = api.getEpisodes()
                val total = response.results.size

                val userId = auth.currentUser?.uid
                if (userId != null) {
                    db.collection("users").document(userId)
                        .collection("watched_episodes")
                        .whereEqualTo("watched", true)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            val watched = snapshot.size()

                            updateUI(watched, total)
                        }
                }
            } catch (e: Exception) {
                tvStats.text = "Error al cargar estadísticas"
            }
        }
    }

    private fun updateUI(watched: Int, total: Int) {
        if (total > 0) {
            val percentage = (watched.toDouble() / total * 100).toInt()

            tvCount.text = "Has visto $watched de $total episodios"
            tvPercentage.text = "$percentage%"

            pbCircle.progress = percentage
        } else {
            tvCount.text = "No hay datos de episodios"
        }
    }
}
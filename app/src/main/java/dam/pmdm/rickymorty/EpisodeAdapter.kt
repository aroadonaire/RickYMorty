package dam.pmdm.rickymorty

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import android.os.Bundle
import androidx.navigation.findNavController

class EpisodeAdapter(
    private val episodes: List<Episode>,
    private val onSelectionChanged: (Int) -> Unit
) : RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>() {

    private val selectedEpisodes = mutableSetOf<Episode>()
    var isSelectionMode = false

    fun getSelectedEpisodes() = selectedEpisodes.toList()

    fun clearSelection() {
        isSelectionMode = false
        selectedEpisodes.clear()
        notifyDataSetChanged()
    }

    class EpisodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvCode: TextView = itemView.findViewById(R.id.tvCode)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val ivWatched: ImageView = itemView.findViewById(R.id.ivWatched)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_episode, parent, false)
        return EpisodeViewHolder(view)
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        val episode = episodes[position]

        holder.tvName.text = episode.name
        holder.tvCode.text = episode.code
        holder.tvDate.text = episode.airDate

        if (isSelectionMode && selectedEpisodes.contains(episode)) {
            holder.itemView.setBackgroundColor(android.graphics.Color.LTGRAY)
        } else {
            holder.itemView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }

        if (episode.watched) {
            holder.ivWatched.visibility = View.VISIBLE
            holder.itemView.alpha = 0.5f
        } else {
            holder.ivWatched.visibility = View.GONE
            holder.itemView.alpha = 1.0f
        }

        holder.itemView.setOnClickListener {
            if (isSelectionMode) {
                toggleSelection(episode)
            } else {
                val bundle = Bundle()
                bundle.putParcelable("episode", episode)
                it.findNavController().navigate(R.id.action_episodesFragment_to_episodeDetailFragment, bundle)
            }
        }


        holder.itemView.setOnLongClickListener {
            if (!isSelectionMode) {
                isSelectionMode = true
                toggleSelection(episode)
                true
            } else false
        }
    }
    private fun toggleSelection(episode: Episode) {
        if (selectedEpisodes.contains(episode)) {
            selectedEpisodes.remove(episode)
        } else {
            selectedEpisodes.add(episode)
        }

        if (selectedEpisodes.isEmpty()) {
            isSelectionMode = false
        }

        notifyDataSetChanged()
        onSelectionChanged(selectedEpisodes.size)
    }

    override fun getItemCount(): Int = episodes.size
}
package ru.ikom.editor

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.ikom.editor.databinding.ColorItemLayoutBinding

class ColorsAdapter(
    private val selected: (Int) -> Unit
) : ListAdapter<ColorUi, ColorsAdapter.ViewHolder>(DiffColor()) {
    inner class ViewHolder(private val view: ColorItemLayoutBinding) :
        RecyclerView.ViewHolder(view.root) {

            init {
                view.root.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) selected(adapterPosition)
                }
            }

        fun bind(item: ColorUi) {
            view.color.setBackgroundColor(item.color)
            bindSelected(item)
        }

        fun bindSelected(item: ColorUi) {
            if (item.selected)
                view.color.animate().scaleX(0.75f).scaleY(0.75f).setDuration(150).start()
            else view.color.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ColorItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) super.onBindViewHolder(holder, position, payloads)
        else holder.bindSelected(getItem(position))
    }
}

data class ColorUi(val id: Int, val color: Int, val selected: Boolean = false)

class DiffColor : DiffUtil.ItemCallback<ColorUi>() {
    override fun areItemsTheSame(oldItem: ColorUi, newItem: ColorUi): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: ColorUi, newItem: ColorUi): Boolean =
        oldItem == newItem

    override fun getChangePayload(oldItem: ColorUi, newItem: ColorUi): Any? =
        oldItem.selected != newItem.selected

}
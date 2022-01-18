package com.triceriasolutions.pollbuzz.ui.vote

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.triceriasolutions.pollbuzz.R
import com.triceriasolutions.pollbuzz.data.models.PollOption

class OptionsListAdapter(
    var context: Context, var list:List<PollOption>
) : RecyclerView.Adapter<OptionViewHolder>(

) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.text_poll_option_item, parent, false)

        return OptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        holder.optionText.text=list[position].text
    }

    override fun getItemCount(): Int {
      return list.size
    }
}

class OptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
val optionselector=itemView.findViewById<RadioButton>(R.id.option_left)
val optionText=itemView.findViewById<TextView>(R.id.option_text)

}

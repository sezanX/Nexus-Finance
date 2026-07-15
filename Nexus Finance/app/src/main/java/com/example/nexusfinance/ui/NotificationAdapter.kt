package com.example.nexusfinance.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nexusfinance.R

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val iconResId: Int,
    val iconTintResId: Int? = null
)

class NotificationAdapter(
    private val notifications: List<NotificationItem>
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivNotifIcon)
        val tvTitle: TextView = view.findViewById(R.id.tvNotifTitle)
        val tvMessage: TextView = view.findViewById(R.id.tvNotifMessage)
        val tvTime: TextView = view.findViewById(R.id.tvNotifTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notif = notifications[position]
        holder.tvTitle.text = notif.title
        holder.tvMessage.text = notif.message
        holder.tvTime.text = notif.time
        holder.ivIcon.setImageResource(notif.iconResId)
        if (notif.iconTintResId != null) {
            holder.ivIcon.setColorFilter(holder.itemView.context.getColor(notif.iconTintResId))
        } else {
            holder.ivIcon.clearColorFilter()
        }
    }

    override fun getItemCount() = notifications.size
}

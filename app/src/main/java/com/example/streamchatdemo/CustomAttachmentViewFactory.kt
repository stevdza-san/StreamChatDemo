package com.example.streamchatdemo

import android.annotation.SuppressLint
import android.content.Intent
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import com.example.streamchatdemo.databinding.CustomAttachmentBinding
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.message.list.MessageListItemStyle
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentViewFactory
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class CustomAttachmentViewFactory : AttachmentViewFactory() {

    @SuppressLint("SimpleDateFormat")
    override fun createAttachmentView(
        data: MessageListItem.MessageItem,
        listeners: MessageListListenerContainer,
        style: MessageListItemStyle,
        parent: ViewGroup
    ): View {
        val attachment = data.message.attachments.find { it.type == "appointment" }

        return if (attachment != null) {
            val appointment = attachment.extraData["appointment"] as String? ?: ""

            val calendar = Calendar.getInstance()
            val format = SimpleDateFormat("yyyy-MM-dd-HH-mm")
            try {
                val date = format.parse(appointment)
                if (date != null) {
                    calendar.time = date
                    createAppointmentView(
                        parent,
                        calendar
                    )
                } else {
                    super.createAttachmentView(data, listeners, style, parent)
                }
            } catch (e: Exception) {
                super.createAttachmentView(data, listeners, style, parent)
            }
        } else {
            super.createAttachmentView(data, listeners, style, parent)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun createAppointmentView(parent: ViewGroup, calendar: Calendar): View {
        val binding = CustomAttachmentBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        val dateFormat = SimpleDateFormat("MMMM dd, yyyy")
        val timeFormat = SimpleDateFormat("HH:mm")
        binding.appointmentDateTextView.text = dateFormat.format(calendar.time)
        binding.appointmentTimeTextView.text = timeFormat.format(calendar.time)
        binding.appointmentImageView.setBackgroundResource(R.drawable.ic_baseline_event_note_24)
        binding.appointmentCardView.setOnClickListener {
            createEvent(parent = parent, calendar = calendar)
        }
        return binding.root
    }

    private fun createEvent(parent: ViewGroup, calendar: Calendar){
        val startTime = calendar.timeInMillis

        val intent = Intent(Intent.ACTION_INSERT).apply {
            type = "vnd.android.cursor.item/event"
            putExtra(CalendarContract.Events.TITLE, "")
            putExtra(CalendarContract.Events.DESCRIPTION, "")
            putExtra(CalendarContract.Events.EVENT_LOCATION, "")
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
        }
        startActivity(parent.context, intent, null)
    }

}










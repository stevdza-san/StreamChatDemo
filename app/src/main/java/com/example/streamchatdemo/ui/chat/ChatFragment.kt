package com.example.streamchatdemo.ui.chat

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.streamchatdemo.CustomAttachmentViewFactory
import com.example.streamchatdemo.R
import com.example.streamchatdemo.databinding.FragmentChatBinding
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.message.input.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.header.viewmodel.MessageListHeaderViewModel
import io.getstream.chat.android.ui.message.list.header.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.factory.MessageListViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class ChatFragment : Fragment() {

    private val args: ChatFragmentArgs by navArgs()

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val client = ChatClient.instance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        setupMessages()

        binding.messagesHeaderView.setBackButtonClickListener {
            requireActivity().onBackPressed()
        }
        binding.messageList.setAttachmentViewFactory(CustomAttachmentViewFactory())
        binding.appointmentButton.setOnClickListener {
            showDialog()
        }

        return binding.root
    }

    private fun showDialog() {
        val dialog = Dialog(requireActivity())
        dialog.setContentView(R.layout.custom_alert_dialog)

        val date = dialog.findViewById(R.id.datePicker) as DatePicker
        val time = dialog.findViewById(R.id.timePicker) as TimePicker
        val nextButton = dialog.findViewById(R.id.nextButton) as Button
        val backButton = dialog.findViewById(R.id.backButton) as Button

        var clicked = false
        nextButton.setOnClickListener {
            if (!clicked) {
                time.visibility = View.VISIBLE
                date.visibility = View.INVISIBLE
                backButton.visibility = View.VISIBLE
                nextButton.text = "Submit"
            } else {
                val calendar = Calendar.getInstance()
                calendar.set(date.year, date.month, date.dayOfMonth, time.hour, time.minute)

                val format = SimpleDateFormat("yyyy-MM-dd-HH-mm")
                val appointment: String = format.format(calendar.time)

                sendMessage(appointment = appointment)

                dialog.dismiss()
            }
            clicked = !clicked
        }

        backButton.setOnClickListener {
            time.visibility = View.INVISIBLE
            date.visibility = View.VISIBLE
            backButton.visibility = View.INVISIBLE
            nextButton.text = "Next"
            clicked = !clicked
        }

        dialog.show()
    }

    private fun sendMessage(appointment: String) {
        val attachment = Attachment(
            type = "appointment",
            extraData = mutableMapOf(
                "appointment" to appointment
            )
        )
        val message = Message(
            user = client.getCurrentUser()!!,
            text = "",
            attachments = mutableListOf(attachment)
        )
        val channelId = args.channelId.split(":").last()
        client.sendMessage(
            channelType = "messaging",
            channelId = channelId,
            message = message
        ).enqueue()
    }

    private fun setupMessages() {
        val factory = MessageListViewModelFactory(cid = args.channelId)

        val messageListHeaderViewModel: MessageListHeaderViewModel by viewModels { factory }
        val messageListViewModel: MessageListViewModel by viewModels { factory }
        val messageInputViewModel: MessageInputViewModel by viewModels { factory }

        messageListHeaderViewModel.bindView(binding.messagesHeaderView, viewLifecycleOwner)
        messageListViewModel.bindView(binding.messageList, viewLifecycleOwner)
        messageInputViewModel.bindView(binding.messageInputView, viewLifecycleOwner)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
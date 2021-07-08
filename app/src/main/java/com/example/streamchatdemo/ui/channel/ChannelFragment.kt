package com.example.streamchatdemo.ui.channel

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.streamchatdemo.R
import com.example.streamchatdemo.databinding.FragmentChannelBinding
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.ui.avatar.AvatarView
import io.getstream.chat.android.ui.channel.list.header.viewmodel.ChannelListHeaderViewModel
import io.getstream.chat.android.ui.channel.list.header.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel
import io.getstream.chat.android.ui.channel.list.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.factory.ChannelListViewModelFactory

class ChannelFragment : Fragment() {

    private val args: ChannelFragmentArgs by navArgs()

    private var _binding: FragmentChannelBinding? = null
    private val binding get() = _binding!!

    private val client = ChatClient.instance()
    private lateinit var user: User

    @SuppressLint("WrongConstant")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentChannelBinding.inflate(inflater, container, false)

        setupUser()
        setupChannels()
        setupDrawer()

        binding.channelsView.setChannelDeleteClickListener { channel ->
            deleteChannel(channel)
        }

        binding.channelListHeaderView.setOnActionButtonClickListener {
            findNavController().navigate(R.id.action_channelFragment_to_usersFragment)
        }

        binding.channelsView.setChannelItemClickListener { channel ->
            val action = ChannelFragmentDirections.actionChannelFragmentToChatFragment(channel.cid)
            findNavController().navigate(action)
        }

        binding.channelListHeaderView.setOnUserAvatarClickListener {
            binding.drawerLayout.openDrawer(Gravity.START)
        }

        return binding.root
    }

    private fun setupUser() {
        if (client.getCurrentUser() == null) {
            user = if (args.chatUser.firstName.contains("Stefan")) {
                User(
                    id = args.chatUser.username,
                    extraData = mutableMapOf(
                        "name" to args.chatUser.firstName,
                        "county" to "Serbia",
                        "image" to "https://yt3.ggpht.com/ytc/AAUvwniNg3lwIeJ-ybvA1xuWBEzLoYA5KPxnKrojub0zhg=s900-c-k-c0x00ffffff-no-rj"
                    )
                )
            } else {
                User(
                    id = args.chatUser.username,
                    extraData = mutableMapOf(
                        "name" to args.chatUser.firstName
                    )
                )
            }
            val token = client.devToken(user.id)
            client.connectUser(
                user = user,
                token = token
            ).enqueue { result ->
                if (result.isSuccess) {
                    Log.d("ChannelFragment", "Success Connecting the User")
                } else {
                    Log.d("ChannelFragment", result.error().message.toString())
                }
            }
        }
    }

    private fun setupChannels() {
        val filters = Filters.and(
            Filters.eq("type", "messaging"),
            Filters.`in`("members", listOf(client.getCurrentUser()!!.id))
        )
        val viewModelFactory = ChannelListViewModelFactory(
            filters,
            ChannelListViewModel.DEFAULT_SORT
        )
        val listViewModel: ChannelListViewModel by viewModels { viewModelFactory }
        val listHeaderViewModel: ChannelListHeaderViewModel by viewModels()

        listHeaderViewModel.bindView(binding.channelListHeaderView, viewLifecycleOwner)
        listViewModel.bindView(binding.channelsView, viewLifecycleOwner)
    }

    private fun setupDrawer() {
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            if (menuItem.itemId == R.id.logout_menu) {
                logout()
            }
            false
        }
        val currentUser = client.getCurrentUser()!!
        val headerView = binding.navigationView.getHeaderView(0)
        val headerAvatar = headerView.findViewById<AvatarView>(R.id.avatarView)
        headerAvatar.setUserData(currentUser)
        val headerId = headerView.findViewById<TextView>(R.id.id_textView)
        headerId.text = currentUser.id
        val headerName = headerView.findViewById<TextView>(R.id.name_textView)
        headerName.text = currentUser.name
    }

    private fun deleteChannel(channel: Channel) {
        ChatDomain.instance().deleteChannel(channel.cid).enqueue { result ->
            if (result.isSuccess) {
                showToast("Channel: ${channel.name} removed!")
            } else {
                Log.e("ChannelFragment", result.error().message.toString())
            }
        }
    }

    private fun logout() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            client.disconnect()
            findNavController().navigate(R.id.action_channelFragment_to_loginFragment)
            showToast("Logged out successfully")
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Logout?")
        builder.setMessage("Are you sure you want to logout?")
        builder.create().show()
    }

    private fun showToast(message: String) {
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}










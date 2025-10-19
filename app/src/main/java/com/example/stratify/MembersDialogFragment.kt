package com.example.stratify

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stratify.databinding.DialogMembersBinding
import com.google.firebase.auth.FirebaseAuth

class MembersDialogFragment : DialogFragment() {

    private var _binding: DialogMembersBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogMembersBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnClose.setOnClickListener {
            dismiss()
        }

        // --- AWAL PERBAIKAN ---

        // Ambil SEMUA data dari arguments
        val memberNames = arguments?.getStringArray(ARG_MEMBER_NAMES) ?: emptyArray()
        val memberIds = arguments?.getStringArray(ARG_MEMBER_IDS) ?: emptyArray() // <-- BARU

        val memberPhotos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(ARG_MEMBER_PHOTOS, HashMap::class.java) as? HashMap<String, String>
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable(ARG_MEMBER_PHOTOS) as? HashMap<String, String>
        } ?: hashMapOf()

        Log.d("DBG_MEMBERS", "Member names: ${memberNames.toList()}")
        Log.d("DBG_MEMBERS", "Member IDs: ${memberIds.toList()}") // <-- BARU
        Log.d("DBG_MEMBERS", "Member photos: $memberPhotos")

        val currentUserId = auth.currentUser?.uid // <-- Pakai ID untuk cek

        // Buat list Member dengan data LENGKAP (ID, Name, isYou)
        val members = mutableListOf<Member>()
        for (i in memberNames.indices) {
            val id = memberIds.getOrNull(i) ?: ""
            val name = memberNames.getOrNull(i) ?: "Unknown"

            members.add(Member(
                userId = id,                 // <-- INI DIA FIX-NYA
                name = name,
                isYou = (id == currentUserId) // <-- Cek pakai ID
            ))
        }

        // --- AKHIR PERBAIKAN ---

        // Adapter sekarang akan dapat userId dan bisa cari foto
        val memberAdapter = MemberAdapter(members, memberPhotos)
        binding.rvMembers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = memberAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        // Ubah nama argumen dan tambahkan ID
        private const val ARG_MEMBER_NAMES = "member_names"
        private const val ARG_MEMBER_IDS = "member_ids" // <-- BARU
        private const val ARG_MEMBER_PHOTOS = "member_photos"

        // Update fungsi newInstance
        fun newInstance(
            memberNames: Array<String>,
            memberIds: Array<String>, // <-- BARU
            memberPhotos: HashMap<String, String> = hashMapOf()
        ): MembersDialogFragment {
            val fragment = MembersDialogFragment()
            val args = Bundle().apply {
                putStringArray(ARG_MEMBER_NAMES, memberNames)
                putStringArray(ARG_MEMBER_IDS, memberIds) // <-- BARU
                putSerializable(ARG_MEMBER_PHOTOS, memberPhotos)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
package com.example.stratify.view.user

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.stratify.R
import com.example.stratify.view.profile.SharedViewModel
import com.google.firebase.auth.FirebaseAuth

class ProfileOptionsFragment : Fragment() {

    interface OnOptionSelectedListener {
        fun onProfileEditSelected()
        fun onLogoutSelected()
        fun onEnglishSelected()
        fun onIndonesianSelected()
        fun onAccountSelected()
        fun onLanguageSelected()
    }

    private var listener: OnOptionSelectedListener? = null
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnOptionSelectedListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnOptionSelectedListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val profileImage = view.findViewById<ImageView>(R.id.ivProfileImage)
        val profileName = view.findViewById<TextView>(R.id.tvProfileName)

        observeViewModel(profileImage, profileName)
        setupClickListeners(view)

    }

    override fun onResume() {
        super.onResume()

        val view = view ?: return
        val profileImage = view.findViewById<ImageView>(R.id.ivProfileImage)
        val profileName = view.findViewById<TextView>(R.id.tvProfileName)
        val profileEmail = view.findViewById<TextView>(R.id.tvProfileEmail)

        if (profileImage != null && profileName != null && profileEmail != null) {
            loadInitialData(profileImage, profileName, profileEmail)
        }
    }


    private fun loadInitialData(imageView: ImageView, nameView: TextView, emailView: TextView) {
        val user = FirebaseAuth.getInstance().currentUser ?: return

        if (sharedViewModel.displayName.value == null && user.displayName != null) {
            sharedViewModel.displayName.value = user.displayName
        }
        if (sharedViewModel.photoUri.value == null && user.photoUrl != null) {
            sharedViewModel.photoUri.value = user.photoUrl
        }

        val initialName = sharedViewModel.displayName.value ?: user.displayName
        val initialPhotoUri = sharedViewModel.photoUri.value ?: user.photoUrl

        nameView.text = initialName
        emailView.text = user.email

        val placeholderResId = resources.getIdentifier("ic_profile", "drawable", requireContext().packageName)

        Glide.with(this)
            .load(initialPhotoUri)
            .placeholder(placeholderResId.takeIf { it != 0 } ?: R.drawable.ic_profile)
            .circleCrop()
            .into(imageView)
    }

    private fun observeViewModel(imageView: ImageView, nameView: TextView) {
        sharedViewModel.displayName.observe(viewLifecycleOwner) { newName ->
            nameView.text = newName
        }

        sharedViewModel.photoUri.observe(viewLifecycleOwner) { newUri ->
            Glide.with(this)
                .load(newUri)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.ic_profile)
                .circleCrop()
                .into(imageView)
        }
    }

    private fun setupClickListeners(view: View) {
        view.findViewById<TextView>(R.id.optionProfileEdit)?.setOnClickListener {
            listener?.onProfileEditSelected()
        }

        view.findViewById<TextView>(R.id.optionLogout)?.setOnClickListener {
            listener?.onLogoutSelected()
        }

        view.findViewById<TextView>(R.id.optionEnglish)?.setOnClickListener {
            listener?.onEnglishSelected()
        }
        view.findViewById<TextView>(R.id.optionIndonesia)?.setOnClickListener {
            listener?.onIndonesianSelected()
        }

        view.findViewById<TextView>(R.id.optionAccount)?.setOnClickListener {
            listener?.onAccountSelected()
            toggleVisibility(view.findViewById(R.id.account_options_container))
        }
        view.findViewById<TextView>(R.id.optionLanguage)?.setOnClickListener {
            listener?.onLanguageSelected()
            toggleVisibility(view.findViewById(R.id.language_options_container))
        }
    }

    private fun toggleVisibility(container: LinearLayout?) {
        container?.let {
            it.visibility = if (it.visibility == View.GONE) View.VISIBLE else View.GONE
        }
    }
}
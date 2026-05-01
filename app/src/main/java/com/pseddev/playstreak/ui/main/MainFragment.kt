package com.pseddev.mystreak.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.pseddev.mystreak.BuildConfig
import com.pseddev.mystreak.R
import com.pseddev.mystreak.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textAppTitle.text = getString(R.string.app_name)
        binding.textVersion.text = "Version ${BuildConfig.VERSION_NAME}"

        binding.buttonConfiguration.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_configurationFragment)
        }

        binding.buttonImportExport.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_syncFragment)
        }

        binding.buttonPrivacyPolicy.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_privacyPolicyFragment)
        }

        binding.buttonTermsOfService.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_termsOfServiceFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.pandacat.simplechaserun.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pandacat.simplechaserun.databinding.FragmentRunSessionBinding

class RunSessionFragment : Fragment() {
    lateinit var binding: FragmentRunSessionBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = FragmentRunSessionBinding.inflate(inflater, container, false)
        setupUI()
        return binding.root
    }

    private fun setupUI()
    {

    }
}
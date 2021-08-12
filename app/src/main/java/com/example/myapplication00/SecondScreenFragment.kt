package com.example.myapplication00

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.myapplication00.databinding.FragmentFirstScreenBinding
import com.example.myapplication00.databinding.FragmentSecondScreenBinding

class SecondScreenFragment : Fragment() {
    lateinit var binding: FragmentSecondScreenBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSecondScreenBinding.inflate(layoutInflater)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val button = binding.goBackButton
        button.setOnClickListener {
            Navigation.findNavController(view).popBackStack()
        }
    }
}
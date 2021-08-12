package com.example.myapplication00

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.myapplication00.databinding.ActivityMainBinding
import com.example.myapplication00.databinding.FragmentFirstScreenBinding

class FirstScreenFragment : Fragment() {
    lateinit var binding: FragmentFirstScreenBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFirstScreenBinding.inflate(layoutInflater)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val button = binding.button
        button.setOnClickListener{
            val action = R.id.action_firstScreenFragment_to_secondScreenFragment
            Navigation.findNavController(binding.root).navigate(action)
        }
    }
}
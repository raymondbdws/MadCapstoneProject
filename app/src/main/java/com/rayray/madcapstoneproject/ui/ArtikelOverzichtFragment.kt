package com.rayray.madcapstoneproject.ui.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.rayray.madcapstoneproject.R
import com.rayray.madcapstoneproject.model.ProductViewModel

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ArtikelOverzichtFragment : Fragment() {

    private val viewModel: ProductViewModel by activityViewModels()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_overzicht_artikel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getProduct()
    }
}
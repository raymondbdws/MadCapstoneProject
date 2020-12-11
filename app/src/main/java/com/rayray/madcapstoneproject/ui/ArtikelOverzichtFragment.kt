package com.rayray.madcapstoneproject.ui.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rayray.madcapstoneproject.R
import com.rayray.madcapstoneproject.adapter.ProductAdapter
import com.rayray.madcapstoneproject.model.Product
import com.rayray.madcapstoneproject.model.ProductViewModel
import kotlinx.android.synthetic.main.fragment_overzicht_artikel.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ArtikelOverzichtFragment : Fragment() {

    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var productAdapter: ProductAdapter
    private val viewModel: ProductViewModel by activityViewModels()
    private var products: ArrayList<Product> = arrayListOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_overzicht_artikel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRv()
        viewModel.getProduct()
        observeProduct()
    }

    private fun observeProduct() {
        viewModel.product.observe(viewLifecycleOwner, Observer {products ->
                this@ArtikelOverzichtFragment.products.clear()
                this@ArtikelOverzichtFragment.products.addAll(products)
                this@ArtikelOverzichtFragment.products.sortBy {
                    it.register_at
                }
                productAdapter.notifyDataSetChanged()
            }
        )
    }

    private fun initRv(){
        productAdapter = ProductAdapter(products)
        viewManager = LinearLayoutManager(activity)

        createItemTouchHelper().attachToRecyclerView(rvProducts)

        rvProducts.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = productAdapter
        }
    }

    private fun createItemTouchHelper(): ItemTouchHelper {
        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
//                val productToDelete = products[position]
//                viewModel.deleteGame(gameToDelete)
            }
        }
        return ItemTouchHelper(callback)
    }
}
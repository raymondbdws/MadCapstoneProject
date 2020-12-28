package com.rayray.madcapstoneproject.ui.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.util.isNotEmpty
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.rayray.madcapstoneproject.R
import com.rayray.madcapstoneproject.model.Product
import com.rayray.madcapstoneproject.model.ProductViewModel
import com.rayray.madcapstoneproject.ui.MainActivity
import kotlinx.android.synthetic.main.fragment_inboek.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class InboekFragment : Fragment() {

    private val viewModel: ProductViewModel by activityViewModels()
    private val requestCodeCameraPermission = 1001
    private var barCode = "0"
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private var quantity = 0
    private val minimumQuantityToUpdate = 1

    //Onderste 2 blijven 0,
    private val resetNumbers = 0
    private val resetStrings = ""

    private lateinit var selectedProduct: Product
    private lateinit var cameraSource: CameraSource
    private lateinit var detector: BarcodeDetector

    //There are 4 fragments, 0 - 3. InboekFragment is at position 1.
    private val cameraFragment = 1


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inboek, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkCameraPermission()
        viewModel.getProduct()
        //observeProduct()
        productItemClickEvent()
        enableSaveButton(false)
    }

    /**
     * Voegt onClick event toe aan de 3 knoppen
     */
    fun productItemClickEvent() {
        val tvQuantity: TextView = view?.findViewById(R.id.tvQuantity) as TextView

        ibAddQuantity.setOnClickListener {
            tvQuantity.text = (++quantity).toString()
        }
        ibDeleteQuantity.setOnClickListener {
            tvQuantity.text = (--quantity).toString()
        }
        btn_save_product_quantity.setOnClickListener {
            if (quantity >= minimumQuantityToUpdate){
                quantity += selectedProduct.stock_quantity

                viewModel.updateProduct(quantity, selectedProduct)
                viewModel.getProduct()
                Toast.makeText(context, "Opgeslagen", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context, "Kan niet afboeken, zie tab 'afboeken'", Toast.LENGTH_SHORT).show()
            }
            enableSaveButton(false)
            clearSelectedProduct()
        }
    }

    private fun clearSelectedProduct() {
        barCode = resetNumbers.toString()
        quantity = resetNumbers

        val tvProductCode: TextView = view?.findViewById(R.id.tvProductCode) as TextView
        val tvName: TextView = view?.findViewById(R.id.tvProductName) as TextView
        val tvPrice: TextView = view?.findViewById(R.id.tvProductPrice) as TextView
        val tvQuantity: TextView = view?.findViewById(R.id.tvQuantity) as TextView

        tvProductCode.text = resetStrings
        tvName.text = resetStrings
        tvPrice.text = resetStrings
        tvQuantity.text = quantity.toString()
    }

    private fun enableSaveButton(bool: Boolean) {
        btn_save_product_quantity.isEnabled = bool
        btn_save_product_quantity.isClickable = bool
    }

    private fun checkSelectedProductIsNotInitialized() {
        //todo check anders crashed tie
    }

    private fun checkCameraPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                requireActivity(), arrayOf(
                    Manifest.permission.CAMERA
                ),
                requestCodeCameraPermission
            )
        } else {
            Log.e("DB", "PERMISSION GRANTED")
            setupControls()
        }

    }

    private fun setupControls() {
        detector = BarcodeDetector.Builder(requireContext()).build()
        cameraSource = CameraSource.Builder(requireContext(), detector)
            .setAutoFocusEnabled(true)
            .build()

        svCameraPreview.holder.addCallback(surfaceCallBack)
        detector.setProcessor(processor)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodeCameraPermission && grantResults.isNotEmpty()) {
            setupControls()
        } else {
            Toast.makeText(context, "Geen toestemming", Toast.LENGTH_SHORT).show()
        }
    }

    private val surfaceCallBack = object : SurfaceHolder.Callback {
        override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
            try {
                checkCameraPermission()
                cameraSource.start(surfaceHolder)
            } catch (excepten: Exception) {
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }

        override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        }

        override fun surfaceDestroyed(p0: SurfaceHolder) {
            cameraSource.stop()
        }

    }

    private fun observeProduct() {
        val tvBarCode: TextView = view?.findViewById(R.id.tvProductCode) as TextView
        val tvName: TextView = view?.findViewById(R.id.tvProductName) as TextView
        val tvPrice: TextView = view?.findViewById(R.id.tvProductPrice) as TextView

        viewModel.products.observe(viewLifecycleOwner, {
            for (product in it) {
                if (product.ean == barCode) {
                    tvBarCode.text = product.code
                    tvName.text = "${product.brand} ${product.type}"
                    tvPrice.text = product.sell_price.toString()
                    selectedProduct = product

                }
            }
        })

        // plus min knop, click listener, bij elke klik voorraad updaten.
    }

    private val processor = object : Detector.Processor<Barcode> {
        override fun release() {

        }

        override fun receiveDetections(detections: Detector.Detections<Barcode>) {
            val currentFragment = (activity as MainActivity?)?.getSelectedTab()
            // val tabLayout: TabLayout = view!!.findViewById(R.id.tabLayout)
            if (detections != null && detections.detectedItems.isNotEmpty() &&
                currentFragment == cameraFragment
            ) {
                val barCodes: SparseArray<Barcode> = detections.detectedItems
                barCode = barCodes.valueAt(resetNumbers).displayValue

                Log.d("tag", barCode)
                mainScope.launch {
                    withContext(Dispatchers.Main) {
                        observeProduct()
                        enableSaveButton(true)
                    }
                }
            } else {
                Log.d("tag", "No value + " + currentFragment)
            }
        }

    }
}
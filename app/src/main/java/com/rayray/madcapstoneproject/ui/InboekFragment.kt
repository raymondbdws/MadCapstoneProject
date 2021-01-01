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
import com.google.android.material.snackbar.Snackbar
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
 * @author Raymond Chang
 *
 * Deze class zorgt ervoor dat producten gescand en correct ingeboekt kunnen worden
 */
class InboekFragment : Fragment() {

    /**
     * Variabelen
     */
    private lateinit var selectedProduct: Product
    private lateinit var cameraSource: CameraSource
    private lateinit var detector: BarcodeDetector

    //There are 4 fragments, 0 - 3. InboekFragment is at position 1.
    private val cameraFragment = 1
    //Onderste 2 blijven 0,
    private val resetNumbers = 0
    private val resetStrings = ""
    private val viewModel: ProductViewModel by activityViewModels()
    private val requestCodeCameraPermission = 1001
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private val minimumQuantityToUpdate = 1

    private var barCode = "0"
    private var quantity = 0

    /**
     * OncreateView
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inboek, container, false)
    }

    /**
     * onViewCreated
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkCameraPermission()
        viewModel.getProduct()
        productItemClickEvent()
        enableSaveButton(false)
    }

    /**
     * Voegt onClick event toe aan de 3 knoppen: Plus, Min en Opslaan knop.
     */
    private fun productItemClickEvent() {
        val tvQuantity: TextView = view?.findViewById(R.id.tvQuantity) as TextView

        //Telt het aantal op
        ibAddQuantity.setOnClickListener {
            tvQuantity.text = (++quantity).toString()
        }

        //Telt af
        ibDeleteQuantity.setOnClickListener {
            tvQuantity.text = (--quantity).toString()
        }

        //Slaat het aantal op in Product object en stuurt het door naar viewModel
        btn_save_product_quantity.setOnClickListener {
            if (quantity >= minimumQuantityToUpdate){
                quantity += selectedProduct.stock_quantity

                val updatedProduct = Product(
                    selectedProduct.code,
                    selectedProduct.ean,
                    selectedProduct.brand,
                    selectedProduct.type,
                    selectedProduct.department,
                    selectedProduct.sell_price,
                    selectedProduct.purchased_price,
                    quantity,
                    selectedProduct.register_at,
                    selectedProduct.image,
                    selectedProduct.specs,
                    selectedProduct.release_date
                )

                viewModel.updateProduct(updatedProduct)
                viewModel.getProduct()

                Snackbar.make(
                    requireActivity().findViewById(R.id.clInboek),
                    R.string.opgeslagen,
                    Snackbar.LENGTH_LONG
                ).show()
            }else{
                Snackbar.make(
                    requireActivity().findViewById(R.id.clInboek),
                    R.string.niet_opgeslagen,
                    Snackbar.LENGTH_LONG
                ).show()
            }
            enableSaveButton(false)
            clearSelectedProduct()
        }
    }

    /**
     * Zorgt ervoor dat het scande product niet meer wordt weergegeven.
     */
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

    /**
     * Alleen wanneer het product gescand is, dan wordt de knop weer klikbaar.
     */
    private fun enableSaveButton(bool: Boolean) {
        btn_save_product_quantity.isEnabled = bool
        btn_save_product_quantity.isClickable = bool
    }

    /**
     * Controleert de camera toestemming, en vraagt het aan gebruiker indien nodig.
     */
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

    /**
     * initialize alle benodigde componenten voor de camera en barcode scanner
     */
    private fun setupControls() {
        detector = BarcodeDetector.Builder(requireContext()).build()
        cameraSource = CameraSource.Builder(requireContext(), detector)
            .setAutoFocusEnabled(true)
            .build()

        svCameraPreview.holder.addCallback(surfaceCallBack)
        detector.setProcessor(processor)
    }

    /**
     * Controleert wat het resultaat is als er een toestemming gevraagd wordt. Als de
     * gebruiker instemt, dan wordt de camera en barcode componenten pas geinitialiseert.
     */
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

    /**
     * Surface is nodig om het beeld van de camera te laten weergeven. Hier wordt de Callback
     * overerft. Bij surfaceCreated wordt er geprobeert om toestemming te krijgen en opent de camera.
     * Bij surfaceDestroyed, stopt de camera automatisch.
     */
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

    /**
     * Wanneer er een product is gescand, gaat het kijken of hij het product herkent. Zoja, dan
     * wordt de gegevens van het product geladen, anders niet.
     */
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
    }

    /**
     * In deze method wordt er continue gekeken of de scanner een barcode herkent.
     */
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
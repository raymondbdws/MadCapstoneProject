package com.rayray.madcapstoneproject.ui.ui

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.rayray.madcapstoneproject.R
import com.rayray.madcapstoneproject.adapter.ProductAdapter
import com.rayray.madcapstoneproject.adapter.ProductAdapterForChecking
import com.rayray.madcapstoneproject.model.Product
import com.rayray.madcapstoneproject.model.ProductViewModel
import com.rayray.madcapstoneproject.ui.MainActivity
import kotlinx.android.synthetic.main.dialog_camera.*
import kotlinx.android.synthetic.main.fragment_controle.*
import kotlinx.android.synthetic.main.fragment_overzicht_artikel.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ControleFragment : Fragment() {
    private lateinit var alertDialogFragment: AlertDialog
    private lateinit var detector: BarcodeDetector
    private lateinit var cameraSource: CameraSource
    private lateinit var inflater: LayoutInflater
    private lateinit var dialogView: View
    private lateinit var svCameraControlePreview: SurfaceView
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var productAdapter: ProductAdapterForChecking

    //todo map gebruiken om de huidige voorraad te controleren.
    private var products: ArrayList<Product> = arrayListOf()
    private var productToCheck: MutableMap<String, Int> = mutableMapOf()
    private var barCode = "0"

    private val viewModel: ProductViewModel by activityViewModels()
    private val requestCodeCameraPermission = 1001
    private val resetNumbers = 0
    private val mainScope = CoroutineScope(Dispatchers.Main)
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
            //todo proberen code hier naartoe te verplaatsen
        }

        override fun surfaceDestroyed(p0: SurfaceHolder) {
            cameraSource.stop()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_controle, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getProduct()
        observeProduct()
        initRv()
        inflater = this.layoutInflater
        dialogView = inflater.inflate(R.layout.dialog_camera, null)
        svCameraControlePreview = dialogView.findViewById<SurfaceView>(R.id.svCameraControlePreview)
        checkCameraPermission()
        createDialog()

        btnScanArticle.setOnClickListener {
            alertDialogFragment.show()
        }
    }

    private fun initRv() {
        productAdapter = ProductAdapterForChecking(products, productToCheck)
        viewManager = LinearLayoutManager(activity)

        rvProductsControle.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = productAdapter
        }
    }

    private fun observeProduct() {
        viewModel.products.observe(viewLifecycleOwner, { products ->
            this@ControleFragment.products.clear()
            this@ControleFragment.products.addAll(products)
        })
    }

    private fun checkCameraPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
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

        svCameraControlePreview.holder.addCallback(surfaceCallBack)
        detector.setProcessor(processor)
    }

    private val processor = object : Detector.Processor<Barcode> {
        override fun release() {

        }

        override fun receiveDetections(detections: Detector.Detections<Barcode>) {
            val currentFragment = (activity as MainActivity?)?.getSelectedTab()
            // val tabLayout: TabLayout = view!!.findViewById(R.id.tabLayout)
            if (detections != null) {
                val barCodes: SparseArray<Barcode> = detections.detectedItems


                if(barCodes.size() > 0){
                    //geen try catch, anders stopt de functie te vroeg. error tot gevolg.
                    barCode = barCodes.valueAt(resetNumbers).displayValue
                }

                countScannedProduct()

            } else {
                Log.d("tag", "No value + " + currentFragment)
            }
        }
    }

    private fun countScannedProduct(){
        //Loopt door alle producten die in de database staan
        for (product in products) {

            //controleert of de gescande barcode overeenkomt met het
            // ean nummer in de database
            if (barCode == product.ean) {

                //Controleert of de barcode al bestaat in de map
                //anders voegt hij een nieuwe toe.
                if (productToCheck.containsKey(barCode)) {

                    //Het aantal getelde producten moet lager zijn dan het aantal in de database
                    if (productToCheck[barCode]!! < product.stock_quantity) {

                        //Voegt een niet bestaande value in Map. Met een start waarde van 1
                        productToCheck[barCode] = (productToCheck[barCode]!! + 1)
                    } else {
                        alertDialogFragment.dismiss()

                        mainScope.launch {
                            withContext(Dispatchers.Main){
                                Toast.makeText(
                                    context,
                                    R.string.quantity_error,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                } else {
                    //Voegt een nieuw ean nummer met quantity 1
                    productToCheck[barCode] = 1
                }
                mainScope.launch {
                    withContext(Dispatchers.Main) {
                        barCode = resetNumbers.toString()
                        alertDialogFragment.dismiss()
                        productAdapter.notifyDataSetChanged()
                    }
                }
            }
        }

    }

    private fun createDialog() {
        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(context)
        dialogBuilder.setOnDismissListener(object : DialogInterface.OnDismissListener {
            override fun onDismiss(arg0: DialogInterface) {
                alertDialogFragment.dismiss()
            }
        })

        dialogBuilder.setView(dialogView)
        alertDialogFragment = dialogBuilder.create()
    }
}
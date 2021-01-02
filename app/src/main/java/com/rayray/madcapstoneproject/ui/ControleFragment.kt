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
import com.google.android.material.snackbar.Snackbar
import com.rayray.madcapstoneproject.R
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

/**
 * @author Raymond Chang
 *
 * Het doel van deze class is alle benodigdheden die nodig zijn om de voorraad te tellen
 * te faciliteren.
 */
class ControleFragment : Fragment() {

    /**
     * Variabelen
     */
    private lateinit var alertDialogFragment: AlertDialog
    private lateinit var detector: BarcodeDetector
    private lateinit var cameraSource: CameraSource
    private lateinit var inflater: LayoutInflater
    private lateinit var dialogView: View
    private lateinit var svCameraControlePreview: SurfaceView
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var productAdapter: ProductAdapterForChecking

    private var products: ArrayList<Product> = arrayListOf()
    private var productToCheck: MutableMap<String, Int> = mutableMapOf()
    private var barCode = "0"

    private val viewModel: ProductViewModel by activityViewModels()
    private val requestCodeCameraPermission = 1001
    private val resetNumbers = 0
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private val surfaceCallBack = object : SurfaceHolder.Callback {
        override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
            //niet nodig, maar dient wel aanwezig te zijn. Inherentance
        }

        override fun surfaceChanged(surfaceHolder: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
            try {
                checkCameraPermission()
                cameraSource.start(surfaceHolder)
            } catch (excepten: Exception) {
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }

        override fun surfaceDestroyed(p0: SurfaceHolder) {
            cameraSource.stop()
        }
    }

    /**
     * OncreateView
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_controle, container, false)
    }

    /**
     * onViewCreated
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getProduct()
        observeProduct()
        initRv()
        inflater = this.layoutInflater
        dialogView = inflater.inflate(R.layout.dialog_camera, null)
        svCameraControlePreview = dialogView.findViewById(R.id.svCameraControlePreview)
        checkCameraPermission()
        createDialog()

        btnScanArticle.setOnClickListener {
            alertDialogFragment.show()
        }
    }

    /**
     * initialiseert Recycleview
     */
    private fun initRv() {
        productAdapter = ProductAdapterForChecking(products, productToCheck)
        viewManager = LinearLayoutManager(activity)

        rvProductsControle.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = productAdapter
        }
    }

    /**
     * Observing products. Automatisch wordt de RV bijgewerkt wanneer Productslijst wordt aangepast.
     */
    private fun observeProduct() {
        viewModel.products.observe(viewLifecycleOwner, { products ->
            this@ControleFragment.products.clear()
            this@ControleFragment.products.addAll(products)
        })
    }

    /**
     * Controleert camera toestemming. Dit is verplicht, anders kan je de app niet gebruiken.
     * Als er nog geen toestemming is gegeven door gebruiker, dan wordt het gevraagd.
     */
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

    /**
     * initialize alle benodigde componenten voor de camera en barcode scanner
     */
    private fun setupControls() {
        detector = BarcodeDetector.Builder(requireContext()).build()
        cameraSource = CameraSource.Builder(requireContext(), detector)
            .setAutoFocusEnabled(true)
            .build()
        svCameraControlePreview.holder.addCallback(surfaceCallBack)
        detector.setProcessor(processor)
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
            if (detections != null) {
                val barCodes: SparseArray<Barcode> = detections.detectedItems

                if (barCodes.size() > 0) {
                    //geen try catch, anders stopt de functie te vroeg. error tot gevolg.
                    barCode = barCodes.valueAt(resetNumbers).displayValue
                }

                countScannedProduct()
            } else {
                Log.d("tag", "No value + " + currentFragment)
            }
        }
    }

    /**
     * Zorgt ervoor dat de producten op correcte manier geteld worden.
     */
    private fun countScannedProduct() {
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
                            withContext(Dispatchers.Main) {
                                Snackbar.make(
                                    requireActivity().findViewById(R.id.clControle),
                                    R.string.quantity_error,
                                    Snackbar.LENGTH_LONG
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

    /**
     * dialog is nodig om het beeld van de camera te weergeven.
     */
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
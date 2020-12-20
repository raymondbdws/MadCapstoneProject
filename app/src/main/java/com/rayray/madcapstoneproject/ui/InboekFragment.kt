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

    private fun observeProduct(){
        viewModel.product.observe(viewLifecycleOwner, {
            for (product in it){
                if (product.ean == barCode){
                    //todo product item vullen onder de scanner
                    val tvBarCode: TextView = view?.findViewById(R.id.tvProductCode) as TextView
                    val tvName: TextView = view?.findViewById(R.id.tvProductName) as TextView
                    val tvPrice: TextView = view?.findViewById(R.id.tvProductPrice) as TextView
                    //todo weergeven in inboeken fragment.
                    tvBarCode.text = product.code
                    tvName.text = "${product.brand} ${product.type}"
                    tvPrice.text = product.sell_price.toString()

//                            textView.setOnClickListener {
//                                textView.text = getString(R.string.name)
//                            }

                }
            }
        })

        //todo plus min knop, click listener, bij elke klik voorraad updaten.
        //todo start scanning, en maak gescande artikel leeg.

        svCameraPreview.holder.addCallback(surfaceCallBack)
    }

    private val processor = object : Detector.Processor<Barcode> {
        override fun release() {

        }

        override fun receiveDetections(detections: Detector.Detections<Barcode>) {
            val currentFragment = (activity as MainActivity?)?.getSelectedTab()
            // val tabLayout: TabLayout = view!!.findViewById(R.id.tabLayout)
            if (detections != null && detections.detectedItems.isNotEmpty() &&
                currentFragment == cameraFragment) {
                val barCodes: SparseArray<Barcode> = detections.detectedItems
                barCode = barCodes.valueAt(0).displayValue

                Log.d("tag", barCode)
                mainScope.launch {
                    withContext(Dispatchers.Main){
                        observeProduct()
                    }
                }

                // vang barcode op
                // stop tijdelijk scanne

                // haal het desbetreffende artikel op


                //cameraSource.stop()
            } else {

                Log.d("tag", "No value + " + currentFragment)
               // cameraSource.stop()
            }
        }

    }
}
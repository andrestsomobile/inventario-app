package com.koba.inventario.relocation

import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.koba.inventario.R
import com.koba.inventario.camera.BARCODE_CAPTURED_VALUE
import com.koba.inventario.camera.INVOCATION_SOURCE_VALUE
import com.koba.inventario.database.AppDatabase
import com.koba.inventario.positioning.TrafficUiModel

const val INVOCATION_SOURCE_RELOCATION_PRODUCT = "INVOCATION_SOURCE_RELOCATION_PRODUCT"
const val INVOCATION_SOURCE_RELOCATION_ORIGIN = "INVOCATION_SOURCE_RELOCATION_ORIGIN"
const val INVOCATION_SOURCE_RELOCATION_DESTINATION = "INVOCATION_SOURCE_RELOCATION_DESTINATION"

class RelocationFragment : Fragment() {

    private val viewModel: RelocationViewModel by activityViewModels()
    private lateinit var username : TextView
    private lateinit var syncButton : ImageButton
    private lateinit var scanProductButton : Button
    private lateinit var scanOriginButton : Button
    private lateinit var scanDestinationButton : Button
    private lateinit var modifyButton : Button
    private lateinit var barCodeProduct : EditText
    private lateinit var barCodeOrigin : EditText
    private lateinit var barCodeDestination : EditText
    private lateinit var navController: NavController
    private lateinit var progressBar: ProgressBar
    private lateinit var barcodeSource: String
    private lateinit var amountProduct : EditText
    private lateinit var user: String
    private var saved: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_relocation, container, false)
        navController = findNavController()
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String>(
            BARCODE_CAPTURED_VALUE
        )?.observe(
            viewLifecycleOwner) { result ->
            barcodeSource = result
        }
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String>(
            INVOCATION_SOURCE_VALUE
        )?.observe(
            viewLifecycleOwner) { result ->
            when(result) {
                INVOCATION_SOURCE_RELOCATION_PRODUCT -> barCodeProduct.setText(barcodeSource)
                INVOCATION_SOURCE_RELOCATION_ORIGIN -> barCodeOrigin.setText(barcodeSource)
                else -> barCodeDestination.setText(barcodeSource)
            }
        }
        setHasOptionsMenu(true)
        initLiveData()
        initViews(view)
        return view
    }

    private fun initViews(view: View) {
        username = view.findViewById(R.id.username)
        syncButton = view.findViewById(R.id.syncButton)
        scanProductButton = view.findViewById(R.id.button_scan_product)
        scanOriginButton = view.findViewById(R.id.button_scan_origin)
        scanDestinationButton = view.findViewById(R.id.button_scan_destination)
        modifyButton = view.findViewById(R.id.button_save_modify)
        barCodeProduct = view.findViewById(R.id.barcode_product)
        barCodeOrigin = view.findViewById(R.id.barcode_origin)
        barCodeDestination = view.findViewById(R.id.barcode_destination)
        progressBar = view.findViewById(R.id.progressBar)
        amountProduct = view.findViewById(R.id.edit_text_amount)

        syncButton.isEnabled = false
        val args : RelocationFragmentArgs by navArgs()
        username.text = "¡Hola, "+args.username+"!"
        user = args.login
        viewModel.findByUser(args.username)

        barCodeProduct.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                barCodeProduct.setText(barCodeProduct.text.substring(0, barCodeProduct.text.length-1))
                barCodeOrigin.requestFocus()
                activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            }
            false
        })

        barCodeOrigin.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                barCodeOrigin.setText(barCodeOrigin.text.substring(0, barCodeOrigin.text.length-1))
                barCodeDestination.requestFocus()
                activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            }
            false
        })

        barCodeDestination.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                barCodeDestination.setText(barCodeDestination.text.substring(0, barCodeDestination.text.length-1))
                amountProduct.requestFocus()
                activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            }
            false
        })

        modifyButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            var required = true
            if (TextUtils.isEmpty(barCodeProduct.text.toString())){
                Toast.makeText(requireContext(), getString(R.string.required_field_product), Toast.LENGTH_SHORT).show()
                required = false
            }
            if (TextUtils.isEmpty(barCodeOrigin.text.toString())) {
                Toast.makeText(requireContext(), getString(R.string.required_field_origin), Toast.LENGTH_SHORT)
                    .show()
                required = false
            }
            if (TextUtils.isEmpty(barCodeDestination.text.toString())) {
                Toast.makeText(requireContext(), getString(R.string.required_field_destination), Toast.LENGTH_SHORT)
                    .show()
                required = false
            }
            if (TextUtils.isEmpty(amountProduct.text.toString())) {
                Toast.makeText(requireContext(), getString(R.string.required_field_amount), Toast.LENGTH_SHORT)
                    .show()
                required = false
            }
            if(required){
                save(barCodeProduct.text.toString(),barCodeOrigin.text.toString(),barCodeDestination.text.toString(),user,amountProduct.text.toString())
                //save("053891138391",args.username,amountProduct.text.toString())
            }
            progressBar.visibility = View.INVISIBLE
        }

        scanProductButton.setOnClickListener {
            navController.navigate(RelocationFragmentDirections.actionRelocationFragmentToCameraFragment(INVOCATION_SOURCE_RELOCATION_PRODUCT))
        }
        scanOriginButton.setOnClickListener {
            navController.navigate(RelocationFragmentDirections.actionRelocationFragmentToCameraFragment(INVOCATION_SOURCE_RELOCATION_ORIGIN))
        }
        scanDestinationButton.setOnClickListener {
            navController.navigate(RelocationFragmentDirections.actionRelocationFragmentToCameraFragment(INVOCATION_SOURCE_RELOCATION_DESTINATION))
        }

        syncButton.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.sync_complete), Toast.LENGTH_SHORT)
                .show()
            syncButton.isEnabled = false
        }

    }

    private fun initLiveData() {
        viewModel.setDataBase(AppDatabase.getDatabase(requireContext().applicationContext))
        viewModel.createdLiveData.observe(viewLifecycleOwner) { result ->
            if (result) {
                Toast.makeText(requireContext(), getString(R.string.save_item_ok), Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.syncLiveData.observe(viewLifecycleOwner) { result ->
            if (result) {
                syncButton.isEnabled = true
            }
        }

        viewModel.relocationLiveData.observe(viewLifecycleOwner) { result ->
            if(!saved){
                Toast.makeText(requireContext(), result, Toast.LENGTH_SHORT).show()
                saved = true
            }
        }

        viewModel.relocationUpdateLiveData.observe(viewLifecycleOwner) { result ->
            if(result != null && !saved) {
                if (result) {
                    viewModel.update(barCodeProduct.text.toString(),barCodeOrigin.text.toString(),barCodeDestination.text.toString(),user,amountProduct.text.toString().toInt())
                } else {
                    viewModel.create(barCodeProduct.text.toString(),barCodeOrigin.text.toString(),barCodeDestination.text.toString(),user,1,amountProduct.text.toString().toInt())
                }
                barCodeProduct.text.clear()
                barCodeOrigin.text.clear()
                barCodeDestination.text.clear()
                amountProduct.text.clear()
                syncButton.isEnabled = true
            }
        }

        viewModel.relocationServiceCreateLiveData.observe(viewLifecycleOwner) { result ->
            if(result != null && !saved) {
                if (result) {
                    barCodeProduct.text.clear()
                    barCodeOrigin.text.clear()
                    barCodeDestination.text.clear()
                    amountProduct.text.clear()
                } else {
                    // se valida en room
                    viewModel.findByProduct(barCodeProduct.text.toString(),barCodeOrigin.text.toString(),barCodeDestination.text.toString(),user)
                }
            }
        }
    }

    private fun save(barcodeProduct: String,barcodeOrigin: String,barcodeDestination: String,user: String,amount :String) {
        saved = false
        viewModel.createRelocationService(barcodeProduct,barcodeOrigin,barcodeDestination,amount.toInt())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.overflow_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                navController.navigate(RelocationFragmentDirections.actionRelocationFragmentToLoginFragment())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
        //return NavigationUI.onNavDestinationSelected(item!!, requireView().findNavController()) || super.onOptionsItemSelected(item)
    }

}
package com.koba.inventario.positioning

import android.app.AlertDialog
import android.content.DialogInterface
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

const val INVOCATION_SOURCE_POSITION_PRODUCT = "INVOCATION_SOURCE_POSITION_PRODUCT"
const val INVOCATION_SOURCE_POSITION_LOCATION = "INVOCATION_SOURCE_POSITION_LOCATION"

class PositionFragment : Fragment() {

    private val viewModel: PositionViewModel by activityViewModels()
    private val viewModelTraffic: TrafficViewModel by activityViewModels()
    private lateinit var username : TextView
    private lateinit var syncButton : ImageButton
    private lateinit var scanProductButton : Button
    private lateinit var scanLocationButton : Button
    private lateinit var saveButton : Button
    private lateinit var finishButton: Button
    private lateinit var barCodeProduct : EditText
    private lateinit var barCodeLocation : EditText
    private lateinit var navController: NavController
    private lateinit var progressBar: ProgressBar
    private lateinit var barcodeSource: String
    private lateinit var amountProduct : EditText
    private lateinit var spinnerTraffic: Spinner
    private lateinit var adapter: ArrayAdapter<TrafficUiModel>
    val args : PositionFragmentArgs by navArgs()
    private var trafficIdSelected: TrafficUiModel = TrafficUiModel(null)
    private lateinit var user: String
    private var saved: Boolean = true
    private var savedTraffic: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_position, container, false)
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
            if(result.equals(INVOCATION_SOURCE_POSITION_PRODUCT)) barCodeProduct.setText(barcodeSource) else barCodeLocation.setText(barcodeSource)
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
        scanLocationButton = view.findViewById(R.id.button_scan_location)
        saveButton = view.findViewById(R.id.button_save_position)
        barCodeProduct = view.findViewById(R.id.barcode_product)
        barCodeLocation = view.findViewById(R.id.barcode_location)
        progressBar = view.findViewById(R.id.progressBar)
        amountProduct = view.findViewById(R.id.edit_text_amount)
        spinnerTraffic = view.findViewById(R.id.spinner_traffic)
        finishButton = view.findViewById(R.id.button_finish_position)

        syncButton.isEnabled = false

        username.text = "¡Hola, "+args.username+"!"
        user = args.login

        viewModel.findByUser(user)
        viewModelTraffic.findByUser(user)

        spinnerTraffic.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // You can define your actions as you want
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                trafficIdSelected = spinnerTraffic.selectedItem as TrafficUiModel
            }
        }

        barCodeProduct.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                barCodeProduct.setText(barCodeProduct.text.substring(0, barCodeProduct.text.length-1))
                barCodeLocation.requestFocus()
                activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            }
            false
        })

        barCodeLocation.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                barCodeLocation.setText(barCodeLocation.text.substring(0, barCodeLocation.text.length-1))
                amountProduct.requestFocus()
                activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            }
            false
        })

        saveButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            var required = true
            if (TextUtils.isEmpty(barCodeProduct.text.toString())){
                Toast.makeText(requireContext(), getString(R.string.required_field_product), Toast.LENGTH_SHORT).show()
                required = false
            }
            if (TextUtils.isEmpty(barCodeLocation.text.toString())) {
                Toast.makeText(requireContext(), getString(R.string.required_field_location), Toast.LENGTH_SHORT)
                    .show()
                required = false
            }
            if (TextUtils.isEmpty(amountProduct.text.toString())) {
                Toast.makeText(requireContext(), getString(R.string.required_field_amount), Toast.LENGTH_SHORT)
                    .show()
                required = false
            }
            if (trafficIdSelected.trafficId == 0) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.required_field_traffic),
                    Toast.LENGTH_SHORT
                ).show()
                required = false
            }
            if(required){
                save(barCodeProduct.text.toString(),barCodeLocation.text.toString(),user,amountProduct.text.toString(),trafficIdSelected.trafficId!!)
            }
            progressBar.visibility = View.INVISIBLE
        }

        finishButton.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this.context)
            dialogBuilder.setMessage(getString(R.string.text_position_question))
            // if the dialog is cancelable
            .setCancelable(false)
            // positive button text and action
            .setPositiveButton(getString(R.string.text_confirm_yes), DialogInterface.OnClickListener {
                    _dialog, _id ->
                progressBar.visibility = View.VISIBLE
                var required = true

                if (trafficIdSelected.trafficId == 0) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.required_field_traffic),
                        Toast.LENGTH_SHORT
                    ).show()
                    required = false
                }
                if(required){
                    saveTraffic(trafficIdSelected.trafficId!!)
                }
                progressBar.visibility = View.INVISIBLE
            })
                // negative button text and action
                .setNegativeButton(getString(R.string.text_confirm_no), DialogInterface.OnClickListener {
                        _dialog, _id -> _dialog.cancel()
                })
            val alert = dialogBuilder.create()
            // set title for alert dialog box
            alert.setTitle(getString(R.string.text_tittle_confirm))
            // show alert dialog
            alert.show()
        }

        scanProductButton.setOnClickListener {
            navController.navigate(PositionFragmentDirections.actionPositionFragmentToCameraFragment(INVOCATION_SOURCE_POSITION_PRODUCT))
        }
        scanLocationButton.setOnClickListener {
            navController.navigate(PositionFragmentDirections.actionPositionFragmentToCameraFragment(INVOCATION_SOURCE_POSITION_LOCATION))
        }

        syncButton.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.sync_complete), Toast.LENGTH_SHORT)
                .show()
            syncButton.isEnabled = false
            viewModel.findByUser(user)
        }

    }

    private fun initLiveData() {
        viewModel.setDataBase(AppDatabase.getDatabase(requireContext().applicationContext))
        viewModelTraffic.setDataBase(AppDatabase.getDatabase(requireContext().applicationContext))
        viewModel.createdLiveData.observe(viewLifecycleOwner) { result ->
            if (result) {
                Toast.makeText(requireContext(), getString(R.string.save_item_ok_position), Toast.LENGTH_SHORT).show()
            }
        }
        viewModelTraffic.createdLiveData.observe(viewLifecycleOwner) { result ->
            if (result) {
                Toast.makeText(requireContext(), getString(R.string.save_item_ok_position), Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.syncLiveData.observe(viewLifecycleOwner) { result ->
            if (result) {
                syncButton.isEnabled = true
            }
        }
        viewModelTraffic.syncLiveData.observe(viewLifecycleOwner) { result ->
            if (result) {
                syncButton.isEnabled = true
            }
        }
        viewModel.validateTrafficLiveData.observe(viewLifecycleOwner) { result ->
            if (result) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.traffic_field_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        viewModel.trafficLiveData.observe(viewLifecycleOwner) { result ->
            adapter =
                ArrayAdapter<TrafficUiModel>(requireContext(), android.R.layout.simple_spinner_item, result)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            adapter.notifyDataSetChanged()
            spinnerTraffic.adapter = adapter
            if(trafficIdSelected.trafficId != null){
                spinnerTraffic.setSelection(adapter.getPosition(trafficIdSelected))
            }
        }
        viewModelTraffic.trafficLiveData.observe(viewLifecycleOwner) { result ->
            if(!savedTraffic){
                Toast.makeText(requireContext(), result, Toast.LENGTH_SHORT).show()
                savedTraffic = true
            }
        }
        viewModel.positionLiveData.observe(viewLifecycleOwner) { result ->
            if(!saved){
                Toast.makeText(requireContext(), result, Toast.LENGTH_SHORT).show()
                saved = true
            }
        }
        viewModelTraffic.trafficUpdateLiveData.observe(viewLifecycleOwner) { result ->
            if(result != null && !savedTraffic) {
                if (result) {
                    viewModelTraffic.update(trafficIdSelected.trafficId!!,user)
                } else {
                    viewModelTraffic.create(trafficIdSelected.trafficId!!,user,1)
                }
                this.trafficIdSelected = TrafficUiModel(null)
                spinnerTraffic.setSelection(0)
                adapter.notifyDataSetChanged()
                syncButton.isEnabled = true
            }
        }
        viewModelTraffic.trafficServiceCreateLiveData.observe(viewLifecycleOwner) { result ->
            if(result != null && !savedTraffic) {
                if (result) {
                    this.trafficIdSelected = TrafficUiModel(null)
                    spinnerTraffic.setSelection(0)
                    adapter.notifyDataSetChanged()
                    spinnerTraffic.adapter = adapter
                    barCodeProduct.text.clear()
                    barCodeLocation.text.clear()
                    amountProduct.text.clear()
                    viewModel.findAllTraffics()
                } else {
                    // se valida en room
                    viewModelTraffic.findByTrafficCode(trafficIdSelected.trafficId!!,user)
                }
            }
        }
        viewModel.positionUpdateLiveData.observe(viewLifecycleOwner) { result ->
            if(result != null && !saved &&  amountProduct.text.isNotEmpty()) {
                if (result) {
                    viewModel.update(barCodeProduct.text.toString(),barCodeLocation.text.toString(),user,amountProduct.text.toString().toInt(), trafficIdSelected.trafficId!!)
                } else {
                    viewModel.create(barCodeProduct.text.toString(),barCodeLocation.text.toString(),user,1,amountProduct.text.toString().toInt(), trafficIdSelected.trafficId!!)
                }
                barCodeProduct.text.clear()
                barCodeLocation.text.clear()
                amountProduct.text.clear()
                syncButton.isEnabled = true
            }
        }
        viewModel.positionServiceCreateLiveData.observe(viewLifecycleOwner) { result ->
            if(result != null && !saved) {
                if (result) {
                   // this.trafficIdSelected = TrafficUiModel(null)
                  //  spinnerTraffic.setSelection(0)
                 //   adapter.notifyDataSetChanged()
                 //   spinnerTraffic.adapter = adapter
                    barCodeProduct.text.clear()
                    barCodeLocation.text.clear()
                    amountProduct.text.clear()
                } else {
                    // se valida en room
                    viewModel.findByProduct(barCodeProduct.text.toString(),barCodeLocation.text.toString(),user)
                }
            }
        }
        viewModel.findAllTraffics()
    }

    private fun save(barcodeProduct: String,barcodeLocation: String,user: String, amount :String, trafficIdSelected: Int) {
        saved = false
        var amountInt = 0;

        try {
            amountInt = amount.toInt();
        } catch(ex: Exception) {
            Toast.makeText(requireContext(), getString(R.string.required_number_amount), Toast.LENGTH_SHORT).show()
        }

        viewModel.createPositionService(trafficIdSelected,barcodeProduct,barcodeLocation,amountInt,user)
    }

    private fun saveTraffic(trafficIdSelected: Int) {
        savedTraffic = false
        viewModelTraffic.createTraffic(trafficIdSelected)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.overflow_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                navController.navigate(PositionFragmentDirections.actionPositionFragmentToLoginFragment())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
        //return NavigationUI.onNavDestinationSelected(item!!, requireView().findNavController()) || super.onOptionsItemSelected(item)
    }
}
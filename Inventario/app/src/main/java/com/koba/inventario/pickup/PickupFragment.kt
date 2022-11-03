package com.koba.inventario.pickup

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.koba.inventario.R
import com.koba.inventario.camera.BARCODE_CAPTURED_VALUE
import com.koba.inventario.camera.INVOCATION_SOURCE_VALUE
import com.koba.inventario.database.AppDatabase


const val INVOCATION_SOURCE_PICKUP_PRODUCT = "INVOCATION_SOURCE_PICKUP_PRODUCT"
const val INVOCATION_SOURCE_PICKUP_LOCATION = "INVOCATION_SOURCE_PICKUP_LOCATION"
const val REQUISITION_CAPTURED_VALUE = "REQUISITION_CAPTURED_VALUE"

class PickupFragment : Fragment() {

    private val viewModel: PickupViewModel by activityViewModels()
    private val viewModelRequisition: RequisitionViewModel by activityViewModels()
    private lateinit var username : TextView
    private lateinit var syncButton : ImageButton
    private lateinit var scanProductButton : Button
    private lateinit var scanLocationButton : Button
    private lateinit var removeButton : Button
    private lateinit var barCodeProduct : EditText
    private lateinit var barCodeLocation : EditText
    private lateinit var amountProduct : EditText
    private lateinit var novelty : EditText
    private lateinit var navController: NavController
    private lateinit var progressBar: ProgressBar
    private lateinit var barcodeSource: String
    private lateinit var user: String
    private lateinit var requisitionNumberId: String
    private lateinit var requisitionNumber : TextView
    private var dataRequisitionObjectList : List<RequisitionObjectResponse> = ArrayList<RequisitionObjectResponse>()
    val args : PickupFragmentArgs by navArgs()
    private var saved: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_pickup, container, false)
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
            if(result.equals(INVOCATION_SOURCE_PICKUP_PRODUCT)) barCodeProduct.setText(barcodeSource) else barCodeLocation.setText(barcodeSource)
            validateDataRequisition()
        }
        setHasOptionsMenu(true)
        initLiveData()
        initViews(view)
        return view
    }

    private fun initViews(view: View) {
        username = view.findViewById(R.id.username)
        requisitionNumber = view.findViewById(R.id.textViewRequisitionNumber)
        syncButton = view.findViewById(R.id.syncButton)
        scanProductButton = view.findViewById(R.id.button_scan_product)
        scanLocationButton = view.findViewById(R.id.button_scan_location)
        removeButton = view.findViewById(R.id.button_save_remove)
        barCodeProduct = view.findViewById(R.id.barcode_product)
        barCodeLocation = view.findViewById(R.id.barcode_location)
        amountProduct = view.findViewById(R.id.edit_text_amount)
        novelty = view.findViewById(R.id.edit_text_novelty)
        progressBar = view.findViewById(R.id.progressBar)

        syncButton.isEnabled = false

        username.text = "¡Hola, "+args.username+"!"
        requisitionNumber.text = "Pedido No.${args.requisition}"
        user = args.login
        viewModel.findByUser(args.username)
        viewModelRequisition.findRequisitionList(args.requisition,user)
        viewModelRequisition.findRequisitionNumber(args.requisition,user)

        removeButton.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this.context)
            dialogBuilder.setMessage(getString(R.string.text_pickup_question))
                // if the dialog is cancelable
                .setCancelable(false)
                // positive button text and action
                .setPositiveButton(getString(R.string.text_confirm_yes), DialogInterface.OnClickListener {
                        _dialog, _id ->
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
                    if(required){
                        save(barCodeProduct.text.toString(),barCodeLocation.text.toString(),args.username,amountProduct.text.toString(),novelty.text.toString())
                        //save("053891138391",args.username,amountProduct.text.toString())
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
            navController.navigate(PickupFragmentDirections.actionPickupFragmentToCameraFragment(INVOCATION_SOURCE_PICKUP_PRODUCT))
        }
        scanLocationButton.setOnClickListener {
            navController.navigate(PickupFragmentDirections.actionPickupFragmentToCameraFragment(INVOCATION_SOURCE_PICKUP_LOCATION))
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
            if (result == true) {
                Toast.makeText(requireContext(), getString(R.string.save_item_ok), Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.syncLiveData.observe(viewLifecycleOwner) { result ->
            if (result) {
                syncButton.isEnabled = true
            }
        }
        viewModelRequisition.requisitionLiveData.observe(viewLifecycleOwner) { result ->
            if (result) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.requisition_field_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        viewModelRequisition.requisitionListLiveData.observe(viewLifecycleOwner) { result ->
            dataRequisitionObjectList = result[0].requisitionList!!
        }
        viewModelRequisition.requisitionNumberLiveData.observe(viewLifecycleOwner) { result ->
            if (result) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.requisition_number_field_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        viewModelRequisition.requisitionNumberResultLiveData.observe(viewLifecycleOwner) { result ->
            if(result.isEmpty()){
                Toast.makeText(
                    requireContext(),
                    getString(R.string.requisition_create_failed),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                requisitionNumberId = result
            }
        }
        viewModel.pickupLiveData.observe(viewLifecycleOwner) { result ->
            if(!saved){
                Toast.makeText(requireContext(), result, Toast.LENGTH_SHORT).show()
                saved = true
            }
        }
        viewModel.pickupUpdateLiveData.observe(viewLifecycleOwner) { result ->
            if(result != null && !saved) {
                if (result) {
                    viewModel.update(barCodeProduct.text.toString(),barCodeLocation.text.toString(),user,novelty.text.toString(),amountProduct.text.toString().toInt())
                } else {
                    viewModel.create(barCodeProduct.text.toString(),barCodeLocation.text.toString(),user,amountProduct.text.toString().toInt(),novelty.text.toString(),1)
                }
                validateRequisitionStatus()
                barCodeProduct.text.clear()
                barCodeLocation.text.clear()
                amountProduct.text.clear()
                novelty.text.clear()
                syncButton.isEnabled = true
            }
        }
        viewModel.pickupServiceCreateLiveData.observe(viewLifecycleOwner) { result ->
            if(result != null && !saved) {
                if (result) {
                    validateRequisitionStatus()
                    barCodeProduct.text.clear()
                    barCodeLocation.text.clear()
                    amountProduct.text.clear()
                    novelty.text.clear()
                } else {
                    // se valida en room
                    viewModel.findByProduct(barCodeProduct.text.toString(),barCodeLocation.text.toString(),user)
                }
            }
        }

    }

    private fun save(barcodeProduct: String,barcodeLocation: String,user: String, amount :String, novelty :String) {
        saved = false
        viewModel.createPickupService(requisitionNumberId,args.requisition,novelty,barcodeProduct,barcodeLocation)
    }

    private fun validateRequisitionStatus(){
        var validateStatus = true
        for (t in dataRequisitionObjectList) {
            var position = t.refPedido?.refpposicion
            if(t.refPedido?.refpproducto == barCodeProduct.text.toString() &&
                position == barCodeLocation.text.toString()){
                t.refPedido?.status = true
            }
            if(t.refPedido?.status == false && validateStatus){
                validateStatus = false
            }
        }
        navController.previousBackStackEntry?.savedStateHandle?.set(
            REQUISITION_CAPTURED_VALUE,args.requisition)
        if(validateStatus){
            viewModel.pickupLiveData.removeObservers(viewLifecycleOwner)
            viewModel.pickupServiceCreateLiveData.removeObservers(viewLifecycleOwner)
            activity?.onBackPressed()
        }
    }

    private fun validateDataRequisition(){
        var validate = true
        if(barCodeProduct.text.toString() != "" && barCodeLocation.text.toString() != ""){
            for (t in dataRequisitionObjectList) {
                var position = t.refPedido?.refpposicion
                var amountPosition = t.refPedido?.refpcantidad?.toBigDecimal()?.toInt().toString()
                if(t.refPedido?.refpproducto == barCodeProduct.text.toString() &&
                    position == barCodeLocation.text.toString()){
                    amountProduct.setText(amountPosition)
                    validate = false
                }
            }
            if(validate){
                Toast.makeText(
                    requireContext(),
                    getString(R.string.requisition_list_field_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.overflow_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                navController.navigate(PickupFragmentDirections.actionPickupFragmentToLoginFragment())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
        //return NavigationUI.onNavDestinationSelected(item!!, requireView().findNavController()) || super.onOptionsItemSelected(item)
    }
}
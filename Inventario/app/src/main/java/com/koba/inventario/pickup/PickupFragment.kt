package com.koba.inventario.pickup

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
    private lateinit var cleanButton : Button
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
    private var isDataRequisitionEmpty = true
    private lateinit var spinnerTipo: Spinner
    private var arrProductLocation : List<PickupObject> = ArrayList<PickupObject>()
    private var seleccion: String = ""
    val LENGTH_MESSAGE = Toast.LENGTH_LONG

    //private var databaseHandler: DatabaseHandler = DatabaseHandler()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {



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
            if(result.equals(INVOCATION_SOURCE_PICKUP_PRODUCT)) {
                barCodeProduct.setText(barcodeSource);
                onBlurProduct()
            } else barCodeLocation.setText(barcodeSource)
        }
        setHasOptionsMenu(true)
        initLiveData()
        initViews(view)
        return view
    }

    fun append(arr: List<PickupObject>, element: PickupObject): List<PickupObject> {
        val list: MutableList<PickupObject> = arr.toMutableList()
        list.add(element)
        return list
    }

    private fun initViews(view: View) {
        username = view.findViewById(R.id.username)
        requisitionNumber = view.findViewById(R.id.textViewRequisitionNumber)
        syncButton = view.findViewById(R.id.syncButton)
        scanProductButton = view.findViewById(R.id.button_scan_product)
        scanLocationButton = view.findViewById(R.id.button_scan_location)
        removeButton = view.findViewById(R.id.button_save_remove)
        cleanButton = view.findViewById(R.id.button_clean_product)
        barCodeProduct = view.findViewById(R.id.barcode_product)
        barCodeLocation = view.findViewById(R.id.barcode_location)
        amountProduct = view.findViewById(R.id.edit_text_amount)
        novelty = view.findViewById(R.id.edit_text_novelty)
        progressBar = view.findViewById(R.id.progressBar)
        spinnerTipo = view.findViewById(R.id.spinnerAlistamiento)
        requisitionNumberId = ""

        val elementos = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item)

        elementos.addAll("1 a 1", "Multiple")

        spinnerTipo.adapter = elementos


        spinnerTipo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // You can define your actions as you want
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                seleccion = spinnerTipo.selectedItem as String
            }
        }


        barCodeProduct.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                if(barCodeLocation.text != null && barCodeLocation.text.isNotEmpty() && barCodeProduct.text != null && barCodeProduct.text.isNotEmpty()) {
                    barCodeProduct.setText(
                        barCodeProduct.text.substring(
                            0,
                            barCodeProduct.text.length - 1
                        )
                    )
                    onBlurProduct()
                }


            }
            false
        })

        barCodeLocation.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                barCodeLocation.setText(barCodeLocation.text.substring(0, barCodeLocation.text.length-1))
                barCodeProduct.requestFocus()
            }
            false
        })

        syncButton.isEnabled = false

        username.text = "¡Hola, "+args.username+"!"
        requisitionNumber.text = "Pedido No.${args.requisition}"
        user = args.login
        viewModel.findByUser(args.username)
        viewModelRequisition.findRequisitionList(args.requisition,user)
        viewModelRequisition.findRequisitionNumber(args.requisition,user)

        cleanButton.setOnClickListener {
            barCodeProduct.text.clear()
            barCodeLocation.text.clear()
            amountProduct.text.clear()
            arrProductLocation = ArrayList<PickupObject>()
        }

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


                    //Si es 1 vs 1 no es necesaria esta validacion
                    if(!seleccion.equals("1 a 1")) {
                        if (TextUtils.isEmpty(barCodeProduct.text.toString())){
                            Toast.makeText(requireContext(), getString(R.string.required_field_product), LENGTH_MESSAGE).show()
                            required = false
                        }

                        if (TextUtils.isEmpty(amountProduct.text.toString())) {
                            Toast.makeText(requireContext(), getString(R.string.required_field_amount), LENGTH_MESSAGE)
                                .show()
                            required = false
                        }
                        if(required){
                            save(barCodeProduct.text.toString(),barCodeLocation.text.toString(),args.username,amountProduct.text.toString(),novelty.text.toString())
                        }
                    } else {
                        save(args.username,novelty.text.toString())
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
            Toast.makeText(requireContext(), getString(R.string.sync_complete), LENGTH_MESSAGE)
                .show()
            syncButton.isEnabled = false
            viewModel.findByUser(user)
        }

    }

    private fun onBlurProduct() {
        if(barCodeLocation.text != null && barCodeLocation.text.isNotEmpty() && barCodeProduct.text != null && barCodeProduct.text.isNotEmpty()) {
            activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

            //Si es 1 vs 1
            if(seleccion.equals("1 a 1") && barCodeLocation.text != null && barCodeProduct.text != null && barCodeProduct.text.isNotEmpty()) {
                var exists = false;

                var valid = validateProduct(barCodeProduct.text.toString(), barCodeLocation.text.toString())

                var amountTemp = 1
                if(valid) {
                    for(p in arrProductLocation) {
                        if(p.barcodeLocation == barCodeLocation.text.toString() && p.barcodeProduct == barCodeProduct.text.toString()) {
                            p.amount = p.amount +1;
                            exists = true;
                            amountTemp = p.amount;
                            break;
                        }
                    }

                    if(!exists) {
                        val p = PickupObject(barCodeProduct.text.toString(), barCodeLocation.text.toString(), 1);
                        arrProductLocation = append(arrProductLocation, p);
                    }
                    amountProduct.setText(amountTemp.toString());

                }

                barCodeProduct.text.clear()
                barCodeProduct.requestFocus()
            } else {
                var valid = validateProduct(barCodeProduct.text.toString(), barCodeLocation.text.toString())

                if(valid) {
                    amountProduct.requestFocus()
                }
            }
        }
    }

    private fun initLiveData() {

        viewModel.setDataBase(AppDatabase.getDatabase(requireContext().applicationContext))
        viewModel.createdLiveData.observe(viewLifecycleOwner) { result ->
            if (result == true) {
                Toast.makeText(requireContext(), getString(R.string.save_item_ok), LENGTH_MESSAGE).show()
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
                    LENGTH_MESSAGE
                ).show()
            }
        }

        viewModelRequisition.requisitionListLiveData.observe(viewLifecycleOwner) { result ->
            if(isDataRequisitionEmpty) {
                dataRequisitionObjectList = result[0].requisitionList!!
                isDataRequisitionEmpty = false
            }
        }
        viewModelRequisition.requisitionNumberLiveData.observe(viewLifecycleOwner) { result ->
            if (result) {
                //databaseHandler.add(PickupFragment::class.java.simpleName, "ERROR", getString(R.string.requisition_number_field_failed))
                Toast.makeText(
                    requireContext(),
                    getString(R.string.requisition_number_field_failed),
                    LENGTH_MESSAGE
                ).show()
            }
        }
        viewModelRequisition.requisitionNumberResultLiveData.observe(viewLifecycleOwner) { result ->
            if(result.isEmpty()){
                Toast.makeText(
                    requireContext(),
                    getString(R.string.requisition_create_failed),
                    LENGTH_MESSAGE
                ).show()
            } else {
                requisitionNumberId = result
            }
        }
        viewModel.pickupLiveData.observe(viewLifecycleOwner) { result ->
            if(!saved){
                Toast.makeText(requireContext(), result, LENGTH_MESSAGE).show()
                saved = true
            }
        }
        viewModel.pickupUpdateLiveData.observe(viewLifecycleOwner) { result ->
            if(result != null && !saved) {
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
        validateDataRequisition(barcodeProduct,barcodeLocation,user, amount, novelty)
    }

    private fun save(user: String, novelty :String) {
        validateDataRequisition(user, novelty)
    }

    private fun validateRequisitionStatus(){
        var validateStatus = true
        var pendingProducts = 0;
        if(seleccion.equals("1 a 1")) {
            for (t in dataRequisitionObjectList) {
                var position = t.refPedido?.refpposicion
                var amountPosition = t.refPedido?.refpcantidad?.toBigDecimal()?.toInt()

                for(p in arrProductLocation) {
                    var inputProduct = p.barcodeProduct.toString()
                    var inputLocation = p.barcodeLocation.toString()
                    if(t.refPedido?.refpproducto == inputProduct &&
                        position == inputLocation &&
                        amountPosition == p.amount){
                        t.refPedido?.status = true
                    }
                }

                if(t.refPedido?.status == false && validateStatus){
                    validateStatus = false
                }

                if(t.refPedido?.status == false){
                    pendingProducts += 1;
                }
            }
        } else {
            for (t in dataRequisitionObjectList) {
                var position = t.refPedido?.refpposicion
                if(t.refPedido?.refpproducto == barCodeProduct.text.toString() &&
                    position == barCodeLocation.text.toString()){
                    t.refPedido?.status = true
                }
                if(t.refPedido?.status == false && validateStatus){
                    validateStatus = false
                }

                if(t.refPedido?.status == false){
                    pendingProducts += 1;
                }
            }
        }

        if(pendingProducts > 0) {
            Toast.makeText(requireContext(), "Tiene "+pendingProducts+" productos pendientes por recoger", LENGTH_MESSAGE).show()
        }

        navController.previousBackStackEntry?.savedStateHandle?.set(
            REQUISITION_CAPTURED_VALUE,args.requisition)
        if(validateStatus){
            viewModel.pickupLiveData.removeObservers(viewLifecycleOwner)
            viewModel.pickupServiceCreateLiveData.removeObservers(viewLifecycleOwner)
            viewModelRequisition.requisitionListLiveData.removeObservers(viewLifecycleOwner)
            activity?.onBackPressed()
        }
    }

    private fun validateDataRequisition(user: String, novelty :String){
        var validate = true
        var productNotFound = 0
        var products = ""

        if(seleccion.equals("1 a 1")) {
            for (t in dataRequisitionObjectList) {
                var position = t.refPedido?.refpposicion
                var amountPosition = t.refPedido?.refpcantidad?.toBigDecimal()?.toInt()
                var productFound = false

                for(p in arrProductLocation) {
                    var inputProduct = p.barcodeProduct.toString()
                    var inputLocation = p.barcodeLocation.toString()
                    if(t.refPedido?.refpproducto == inputProduct &&
                        position == inputLocation &&
                        amountPosition == p.amount){
                        validate = false
                        productFound = true
                    }
                }

                if(!productFound) {
                    productNotFound ++;
                    products += " " +  t.refPedido?.refpproducto;
                }
            }

        }

        if(validate){
            Toast.makeText(
                requireContext(),
                getString(R.string.requisition_list_field_failed),
                LENGTH_MESSAGE
            ).show()
        } else if(productNotFound > 0) {
            Toast.makeText(
                requireContext(),
                "Falta registrar los productos: " + products,
                LENGTH_MESSAGE
            ).show()
        } else {
            saved = false
            if(seleccion.equals("1 a 1")) {
                //Si es 1 vs 1
                for(p in arrProductLocation) {
                    viewModel.createPickupService(requisitionNumberId,args.requisition,novelty,p.barcodeProduct,p.barcodeLocation, user)
                }
            }
        }
    }

    private fun validateProduct(product: String, location :String): Boolean{
        var validate = false
        var validateProduct = false
        var validateRequisition = 0
        if(seleccion.equals("1 a 1")) {
            for (t in dataRequisitionObjectList) {
                var position = t.refPedido?.refpposicion
                var amountPosition = t.refPedido?.refpcantidad?.toBigDecimal()?.toInt()

                var inputProduct = product
                var inputLocation = location
                if (t.refPedido?.refpproducto == inputProduct &&
                    position == inputLocation
                ) {
                    validate = true

                    for (p in arrProductLocation) {
                        if (t.refPedido?.refpproducto == inputProduct &&
                            position == inputLocation &&
                            amountPosition == p.amount
                        ) {
                            validateProduct = true
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.requisition_product_list_completed),
                                LENGTH_MESSAGE
                            ).show()

                        }
                    }
                }
            }


            if(validate) {
                for (t in dataRequisitionObjectList) {
                    var position = t.refPedido?.refpposicion
                    var amountPosition = t.refPedido?.refpcantidad?.toBigDecimal()?.toInt()
                    for(p in arrProductLocation) {
                        var inputProduct = p.barcodeProduct.toString()
                        var inputLocation = p.barcodeLocation.toString()
                        if(t.refPedido?.refpproducto == inputProduct &&
                            position == inputLocation &&
                            amountPosition == p.amount){
                            validateRequisition++;
                        }
                    }

                    if(validateRequisition == dataRequisitionObjectList.size) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.requisition_list_completed),
                            LENGTH_MESSAGE
                        ).show()
                    }
                }
            }

            if(!validate) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.requisition_list_field_failed),
                    LENGTH_MESSAGE
                ).show()
            }
        }



        return validate && !validateProduct
    }

    private fun validateDataRequisition(barcodeProduct: String,barcodeLocation: String,user: String, amount :String, novelty :String){
        var validate = true
        if(barCodeProduct.text.toString() != "" && barCodeLocation.text.toString() != ""){

            for (t in dataRequisitionObjectList) {
                var position = t.refPedido?.refpposicion
                var amountPosition = t.refPedido?.refpcantidad?.toBigDecimal()?.toInt().toString()
                if(t.refPedido?.refpproducto == barCodeProduct.text.toString() &&
                    position == barCodeLocation.text.toString() &&
                    amount == amountProduct.text.toString()){
                    validate = false
                }
            }


            if(validate){
                Toast.makeText(
                    requireContext(),
                    getString(R.string.requisition_list_field_failed),
                    LENGTH_MESSAGE
                ).show()
            } else {
                saved = false
                //Si es multiple
                viewModel.createPickupService(requisitionNumberId,args.requisition,novelty,barcodeProduct,barcodeLocation, user)
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
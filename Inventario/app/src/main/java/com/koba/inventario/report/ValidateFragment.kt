package com.koba.inventario.report

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
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
import com.koba.inventario.pickup.PickupObject


const val INVOCATION_SOURCE_VALIDATE_PRODUCT = "INVOCATION_SOURCE_VALIDATE_PRODUCT"
const val INVOCATION_SOURCE_VALIDATE_LOCATION = "INVOCATION_SOURCE_VALIDATE_LOCATION"

class ValidateFragment : Fragment() {

    private val viewModel: ValidateViewModel by activityViewModels()
    private lateinit var username : TextView
    private lateinit var syncButton : ImageButton
    private lateinit var scanProductButton : Button
    private lateinit var scanLocationButton : Button
    private lateinit var saveButton : Button
    private lateinit var barCodeProduct : EditText
    private lateinit var barCodeLocation : EditText
    private lateinit var navController: NavController
    private lateinit var progressBar: ProgressBar
    private lateinit var barcodeSource: String
    private lateinit var amountProduct : EditText
    private lateinit var countText : EditText
    private lateinit var user: String
    private var dataValidateList : ArrayList<ValidateUiModel> = ArrayList()
    private var saved: Boolean = false
    private lateinit var reference: String
    private lateinit var location: String
    private lateinit var amount: String
    private lateinit var adapter: ArrayAdapter<InventoryUiModel>
    private var countSave: Int = 0;
    private var countNoSave: Int = 0;
    private var idBdLocal: String = "";
    private var arrProductLocation : List<PickupObject> = ArrayList<PickupObject>()

    private lateinit var spinnerTipo: Spinner
    private var seleccionTipo: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (Build.VERSION.SDK_INT > 9) {
            val policy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_validate, container, false)
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
            if(result.equals(INVOCATION_SOURCE_VALIDATE_PRODUCT)) {
                barCodeProduct.setText(barcodeSource)
                onBlurProduct()
            } else barCodeLocation.setText(barcodeSource)
        }

        setHasOptionsMenu(true)
        initLiveData()
        initViews(view)
        return view
    }

    private fun onBlurProduct() {
        if(barCodeLocation.text != null && barCodeLocation.text.isNotEmpty() && barCodeProduct.text != null && barCodeProduct.text.isNotEmpty()) {
            activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

            //Si es 1 vs 1
            if(seleccionTipo.equals("1 a 1")) {
                var exists = false;

                var amountTemp = 1
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

                barCodeProduct.text.clear()
                barCodeProduct.requestFocus()
            } else {
                amountProduct.requestFocus()
            }
        }
    }

    fun append(arr: List<PickupObject>, element: PickupObject): List<PickupObject> {
        val list: MutableList<PickupObject> = arr.toMutableList()
        list.add(element)
        return list
    }

    private fun initViews(view: View) {
        username = view.findViewById(R.id.username)
        syncButton = view.findViewById(R.id.syncButton)
        scanProductButton = view.findViewById(R.id.button_scan_product)
        scanLocationButton = view.findViewById(R.id.button_scan_location)
        saveButton = view.findViewById(R.id.button_save_validate)
        barCodeProduct = view.findViewById(R.id.barcode_product)
        barCodeLocation = view.findViewById(R.id.barcode_location)
        progressBar = view.findViewById(R.id.progressBar)
        amountProduct = view.findViewById(R.id.edit_text_amount)
        countText = view.findViewById(R.id.editConteo)
        spinnerTipo = view.findViewById(R.id.spinnerAlistamiento)

        barCodeLocation.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                barCodeLocation.setText(barCodeLocation.text.substring(0, barCodeLocation.text.length-1))
                barCodeProduct.requestFocus()
                activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            }
            false
        })

        barCodeProduct.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                barCodeProduct.setText(barCodeProduct.text.substring(0, barCodeProduct.text.length-1))
                onBlurProduct()
                activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            }
            false
        })

        val elementos = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item)

        elementos.addAll("1 a 1", "Multiple")

        spinnerTipo.adapter = elementos


        spinnerTipo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // You can define your actions as you want
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                seleccionTipo = spinnerTipo.selectedItem as String
            }
        }

        syncButton.isEnabled = false
        val args : ValidateFragmentArgs by navArgs()
        username.text = "¡Hola, "+args.username+"!"
        user = args.login
        viewModel.findByUser(user, "1")

        saveButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            saveValidation();
            progressBar.visibility = View.INVISIBLE
        }

        scanProductButton.setOnClickListener {
            navController.navigate(ValidateFragmentDirections.actionValidateFragmentToCameraFragment(INVOCATION_SOURCE_VALIDATE_PRODUCT))
        }
        scanLocationButton.setOnClickListener {
            navController.navigate(ValidateFragmentDirections.actionValidateFragmentToCameraFragment(INVOCATION_SOURCE_VALIDATE_LOCATION))
        }



        syncButton.setOnClickListener {

            syncButton.isEnabled = false
            viewModel.findByUser(user, "1");

            viewModel.validateSyncData.value;
            if(viewModel.validateServiceCreateLiveData != null && viewModel.validateServiceCreateLiveData.value != null) {
                if(viewModel.validateServiceCreateLiveData.value == false) {
                    Toast.makeText(
                        requireContext(),
                        viewModel.validateLiveData.value.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                    syncButton.isEnabled = true
                } else {
                    Toast.makeText(requireContext(), getString(R.string.sync_complete) + "," + viewModel.validateLiveData.value, Toast.LENGTH_LONG)
                        .show()

                    viewModel.clearData()
                    syncButton.isEnabled = false
                }
            }
        }

    }

    private fun saveValidation(){
        var required = true

        if(!seleccionTipo.equals("1 a 1")) {
            if (TextUtils.isEmpty(barCodeProduct.text.toString())){
                Toast.makeText(requireContext(), getString(R.string.required_field_product), Toast.LENGTH_LONG).show()
                required = false
            }
            if (TextUtils.isEmpty(barCodeLocation.text.toString())){
                Toast.makeText(requireContext(), getString(R.string.required_field_location), Toast.LENGTH_LONG).show()
                required = false
            }
            if (TextUtils.isEmpty(amountProduct.text.toString())) {
                Toast.makeText(requireContext(), getString(R.string.required_field_amount), Toast.LENGTH_LONG)
                    .show()
                required = false
            }
            if (TextUtils.isEmpty(countText.text.toString())) {
                Toast.makeText(requireContext(), getString(R.string.required_field_conteo), Toast.LENGTH_LONG)
                    .show()
                required = false
            }
        }

        if(required){

            if(!seleccionTipo.equals("1 a 1")) {
                save(barCodeProduct.text.toString(), barCodeLocation.text.toString(),user,amountProduct.text.toString(), countText.text.toString())
                val dialogBuilder = AlertDialog.Builder(this.context)
                dialogBuilder.setMessage(getString(R.string.continue_position))
                    // if the dialog is cancelable
                    .setCancelable(false)
                    // positive button text and action
                    .setPositiveButton(getString(R.string.text_confirm_yes), DialogInterface.OnClickListener {
                            _dialog, _id -> barCodeProduct.requestFocus();

                        _dialog.cancel();

                    })
                    // negative button text and action
                    .setNegativeButton(getString(R.string.text_confirm_no), DialogInterface.OnClickListener {

                            _dialog, _id ->
                        barCodeLocation.requestFocus(); barCodeLocation.text.clear();

                        _dialog.cancel()


                    })
                val alert = dialogBuilder.create()
                // set title for alert dialog box
                alert.setTitle(getString(R.string.text_tittle_confirm))
                // show alert dialog
                alert.show()
            } else {
                for(p in arrProductLocation) {
                    save(p.barcodeProduct, p.barcodeLocation,user,p.amount.toString(), countText.text.toString())
                }

            }
        }
    }

    private fun initLiveData() {
        viewModel.setDataBase(AppDatabase.getDatabase(requireContext().applicationContext))
        viewModel.syncLiveData.observe(viewLifecycleOwner) { result ->
            if (result) {
                syncButton.isEnabled = true
            }
        }

        viewModel.validateServiceCreateLiveData.observe(viewLifecycleOwner) { result ->
            idBdLocal = viewModel.idBdLocal.value.toString();
            //count.setText()
            if(result != null && result && !saved) {
                saved = true;
                countSave += 1;
                if(viewModel.validateLiveData != null && viewModel.validateLiveData.value != null) {
                    Toast.makeText(requireContext(), viewModel.validateLiveData.value.toString(), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(), "Registro guardado", Toast.LENGTH_LONG).show()
                }

                barCodeProduct.text.clear()
                amountProduct.text.clear()
                syncButton.isEnabled = true
                viewModel.clearModel()
                viewModel.findAllInventory()
                this.dataValidateList = ArrayList()
            } else if(result != null  && !result){
                countNoSave += 1;
                syncButton.isEnabled = true
                reference = ""
                location = ""
                amount = ""
                this.dataValidateList = ArrayList()
                viewModel.findAllInventory()
                if(viewModel.validateLiveData != null && viewModel.validateLiveData.value != null) {
                    Toast.makeText(requireContext(), viewModel.validateLiveData.value.toString(), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(), "Error al crear el registro", Toast.LENGTH_LONG).show()
                }
                viewModel.clearModel()
            }
        }

        viewModel.findAllInventory()
    }

    private fun save(barcodeProduct: String,barcodeLocation: String,user: String, amount :String, id : String) {
        saved = false
        viewModel.createValidationService(barcodeProduct,  barcodeLocation, user, amount, id, "inventario", "0")
        barCodeProduct.text.clear()
        amountProduct.text.clear()
        barCodeProduct.requestFocus()
        syncButton.isEnabled = true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.overflow_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                navController.navigate(ValidateFragmentDirections.actionValidateFragmentToLoginFragment())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
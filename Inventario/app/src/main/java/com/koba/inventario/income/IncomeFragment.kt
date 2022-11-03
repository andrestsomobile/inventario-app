package com.koba.inventario.income

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
import com.koba.inventario.database.AppDatabase

const val INVOCATION_SOURCE_INCOME = "INVOCATION_SOURCE_INCOME"

class IncomeFragment : Fragment() {

    private val viewModel: IncomeViewModel by activityViewModels()
    private lateinit var username: TextView
    private lateinit var syncButton: ImageButton
    private lateinit var scanProductButton: Button
    private lateinit var saveButton: Button
    private lateinit var barCodeProduct: EditText
    private lateinit var amountProduct: EditText
    private lateinit var navController: NavController
    private lateinit var progressBar: ProgressBar


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_income, container, false)
        navController = findNavController()
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String>(
            BARCODE_CAPTURED_VALUE
        )?.observe(
            viewLifecycleOwner
        ) { result ->
            barCodeProduct.setText(result)
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
        saveButton = view.findViewById(R.id.button_save_income)
        barCodeProduct = view.findViewById(R.id.barcode_product)
        amountProduct = view.findViewById(R.id.edit_text_amount)
        progressBar = view.findViewById(R.id.progressBar)


        syncButton.isEnabled = false
        val args: IncomeFragmentArgs by navArgs()
        username.text = "¡Hola, " + args.username + "!"

        viewModel.findByUser(args.username)

        saveButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            var required = true
            if (TextUtils.isEmpty(barCodeProduct.text.toString())) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.required_field_product),
                    Toast.LENGTH_SHORT
                ).show()
                required = false
            }
            if (TextUtils.isEmpty(amountProduct.text.toString())) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.required_field_amount),
                    Toast.LENGTH_SHORT
                )
                    .show()
                required = false
            }
            if (required) {
                save(
                    barCodeProduct.text.toString(),
                    args.username,
                    amountProduct.text.toString()
                )
                //save("053891138391",args.username,amountProduct.text.toString())
            }
            progressBar.visibility = View.INVISIBLE
        }

        scanProductButton.setOnClickListener {
            navController.navigate(
                IncomeFragmentDirections.actionIncomeFragmentToCameraFragment(
                    INVOCATION_SOURCE_INCOME
                )
            )
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
                Toast.makeText(
                    requireContext(),
                    getString(R.string.save_item_ok),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        viewModel.syncLiveData.observe(viewLifecycleOwner) { result ->
            if (result) {
                syncButton.isEnabled = true
            }
        }

    }

    private fun save(barcodeProduct: String, user: String, amount: String) {
        // se debe hacer la conexion a los servicios y por ultimo guardar al room
        viewModel.findByProduct(barcodeProduct, user)
        viewModel.validateUpdateLiveData.observe(viewLifecycleOwner) { result ->
            if (result) {
                viewModel.update(barcodeProduct, user, amount.toInt())
            } else {
                viewModel.create(barcodeProduct, user, amount.toInt(), 1)
            }
            barCodeProduct.text.clear()
            amountProduct.text.clear()
            syncButton.isEnabled = true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.overflow_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                navController.navigate(IncomeFragmentDirections.actionIncomeFragmentToLoginFragment())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
        //return NavigationUI.onNavDestinationSelected(item!!, requireView().findNavController()) || super.onOptionsItemSelected(item)
    }

}
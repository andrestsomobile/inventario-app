package com.koba.inventario.pickup

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.koba.inventario.R
import com.koba.inventario.camera.BARCODE_CAPTURED_VALUE
import com.koba.inventario.camera.INVOCATION_SOURCE_VALUE
import com.koba.inventario.database.AppDatabase
import com.koba.inventario.positioning.PositionFragmentArgs

const val INVOCATION_SOURCE_REQUISITION_CODE = "INVOCATION_SOURCE_REQUISITION_CODE"

class RequisitionFragment : Fragment() {

    private val viewModel: RequisitionViewModel by activityViewModels()
    private lateinit var username : TextView
    private lateinit var addButton : Button
    private lateinit var cleanButton : Button
    private lateinit var requisitionCode : EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var requisitionRecyclerView : RecyclerView
    private lateinit var requisitionSearchView : SearchView
    private var data : ArrayList<RequisitionUiModel> = ArrayList()
    private var dataClean : ArrayList<RequisitionUiModel> = ArrayList()
    private var dataRequisitionList : ArrayList<RequisitionUiModel> = ArrayList()
    private lateinit var adapter: RequisitionAdapter
    private lateinit var navController: NavController
    private lateinit var user: String
    private var requisitionBackTrack: String = ""
    private lateinit var barcodeSource: String
    val args : PositionFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_requisition, container, false)
        navController = findNavController()
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String>(
            REQUISITION_CAPTURED_VALUE
        )?.observe(
            viewLifecycleOwner) { result ->
            requisitionBackTrack = result
        }
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
            if(result.equals(INVOCATION_SOURCE_REQUISITION_CODE)) requisitionCode.setText(barcodeSource)
            progressBar.visibility = View.VISIBLE
            if(!requisitionCode.text.toString().isEmpty()) {
                viewModel.findRequisitionList(requisitionCode.text.toString(),user)
            }
        }
        setHasOptionsMenu(true)
        initLiveData()
        initViews(view)
        return view
    }

    private fun initViews(view: View) {
        username = view.findViewById(R.id.username)
        addButton = view.findViewById(R.id.buttonAdd)
        cleanButton = view.findViewById(R.id.buttonClean)
        requisitionSearchView = view.findViewById(R.id.requisitionSearchView)
        requisitionCode = view.findViewById(R.id.requisitionCode)
        progressBar = view.findViewById(R.id.progressBar)

        requisitionRecyclerView = view.findViewById(R.id.requisitionRecyclerView)
        requisitionRecyclerView.layoutManager = LinearLayoutManager(this.context)

        adapter = RequisitionAdapter()
        requisitionRecyclerView.adapter = adapter
        adapter.setData(dataClean)
        adapter.setOnItemClickListener(object : RequisitionAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                val requisition = dataRequisitionList[position].requisition
                requisitionCode.text.clear()
                if(!dataRequisitionList[position].status){
                    navController.navigate(
                        RequisitionFragmentDirections.actionRequisitionFragmentToPickupFragment(
                            args.username, args.login, requisition.toString()
                        )
                    )
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.requisition_field_invalid),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })

        requisitionCode.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                requisitionCode.setText(requisitionCode.text.substring(0, requisitionCode.text.length-1))
                requisitionCode.clearFocus()

                if(!requisitionCode.text.toString().isEmpty()) {
                    viewModel.findRequisitionList(requisitionCode.text.toString(),user)
                }
                activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            }
            false
        })

        requisitionSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                filter(query)
                return false
            }
            override fun onQueryTextChange(newText: String): Boolean {
                filter(newText)
                return false
            }
        })

        username.text = "¡Hola, "+args.username+"!"
        user = args.login

        cleanButton.setOnClickListener {
            adapter = RequisitionAdapter()
            requisitionRecyclerView.adapter = adapter
            adapter.setData(dataClean)
            adapter.notifyDataSetChanged()
            dataRequisitionList = dataClean
            requisitionCode.text.clear()
        }

        addButton.setOnClickListener {
            navController.navigate(RequisitionFragmentDirections.actionRequisitionFragmentToCameraFragment(INVOCATION_SOURCE_REQUISITION_CODE))
        }
    }

    private fun initLiveData() {
        viewModel.setDataBase(AppDatabase.getDatabase(requireContext().applicationContext))
        viewModel.requisitionLiveData.observe(viewLifecycleOwner) { result ->
            if (result) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.requisition_field_failed),
                    Toast.LENGTH_SHORT
                ).show()
                requisitionCode.text.clear()
                progressBar.visibility = View.INVISIBLE
            }
        }

        viewModel.requisitionListLiveData.observe(viewLifecycleOwner) { result ->
            if(result.isNotEmpty()) {
                var validate = true
                for(t in dataRequisitionList){
                    if(t.requisition == result[0].requisition){
                        validate = false
                    }
                }
                if(validate){
                    dataRequisitionList.add(result[0])
                    adapter.setData(dataRequisitionList)
                    adapter.notifyDataSetChanged()
                    adapter.setOnItemClickListener(object : RequisitionAdapter.OnItemClickListener{
                        override fun onItemClick(position: Int) {
                            val requisition = dataRequisitionList[position].requisition
                            requisitionCode.text.clear()
                            if(!dataRequisitionList[position].status){
                                navController.navigate(
                                    RequisitionFragmentDirections.actionRequisitionFragmentToPickupFragment(
                                        args.username, args.login, requisition.toString()
                                    )
                                )
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.requisition_field_invalid),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    })
                }
            }
            adapter.setData(dataRequisitionList)
            adapter.notifyDataSetChanged()
            requisitionCode.text.clear()
            progressBar.visibility = View.INVISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.overflow_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                navController.navigate(RequisitionFragmentDirections.actionRequisitionFragmentToLoginFragment())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
        //return NavigationUI.onNavDestinationSelected(item!!, requireView().findNavController()) || super.onOptionsItemSelected(item)
    }

    private fun filter(text: String) {
        val filteredList: ArrayList<RequisitionUiModel> = ArrayList()
        //looping through existing elements
        for (s in data) {
            //if the existing elements contains the search input
            if (s.searchableText().contains(text,true)) {
                filteredList.add(s)
            }
        }

        adapter.filterList(filteredList)
    }
}
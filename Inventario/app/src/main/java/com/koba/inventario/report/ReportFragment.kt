package com.koba.inventario.report

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.SearchView
import androidx.fragment.app.Fragment
import android.widget.TextView
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.koba.inventario.R

class ReportFragment : Fragment() {

    private lateinit var username : TextView
    private lateinit var validateButton : Button
    private lateinit var reportRecyclerView : RecyclerView
    private lateinit var reportSearchView : SearchView
    private var data : ArrayList<ReportUiModel> = ArrayList()
    private lateinit var adapter: ReportAdapter
    private lateinit var navController: NavController
    private lateinit var user: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_report, container, false)
        navController = findNavController()
        setHasOptionsMenu(true)
        initViews(view)
        return view
    }

    private fun initViews(view: View) {
        username = view.findViewById(R.id.username)
        validateButton = view.findViewById(R.id.buttonValidate)
        reportSearchView = view.findViewById(R.id.reportSearchView)

        reportRecyclerView = view.findViewById(R.id.reportRecyclerView)
        reportRecyclerView.layoutManager = LinearLayoutManager(this.context)

        adapter = ReportAdapter()
        reportRecyclerView.adapter = adapter
        fillList(data)
        adapter.setData(data)

        reportSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                filter(query)
                return false
            }
            override fun onQueryTextChange(newText: String): Boolean {
                filter(newText)
                return false
            }
        })

        /*val args : ReportFragmentArgs by navArgs()
        username.text = "¡Hola, "+args.username+"!"
        user = args.login
        validateButton.setOnClickListener {
            navController.navigate(
                ReportFragmentDirections.actionReportFragmentToValidateFragment(
                    args.username, args.login
                )
            )
        }*/
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.overflow_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                //navController.navigate(ReportFragmentDirections.actionReportFragmentToLoginFragment())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
        //return NavigationUI.onNavDestinationSelected(item!!, requireView().findNavController()) || super.onOptionsItemSelected(item)
    }

    private fun filter(text: String) {
        val filteredList: ArrayList<ReportUiModel> = ArrayList()
        //looping through existing elements
        for (s in data) {
            //if the existing elements contains the search input
            if (s.searchableText().contains(text,true)) {
                filteredList.add(s)
            }
        }

        adapter.filterList(filteredList)
    }

    private fun fillList(data:ArrayList<ReportUiModel>){

        data.add(ReportUiModel(productId = 1234567890, warehouse="Bodega 1", floor = "Piso 1", side = "Lado 1", amount = 500))
        data.add(ReportUiModel(productId = 1234567891, warehouse="Bodega 1", floor = "Piso 1", side = "Lado 2", amount = 400))
        data.add(ReportUiModel(productId = 1234567892, warehouse="Bodega 1", floor = "Piso 2", side = "Lado 3", amount = 50))
        data.add(ReportUiModel(productId = 1234567893, warehouse="Bodega 1", floor = "Piso 2", side = "Lado 4", amount = 555))

        data.add(ReportUiModel(productId = 1234567894, warehouse="Bodega 2", floor = "Piso 1", side = "Lado 1", amount = 500))
        data.add(ReportUiModel(productId = 1234567895, warehouse="Bodega 2", floor = "Piso 1", side = "Lado 2", amount = 400))
        data.add(ReportUiModel(productId = 1234567896, warehouse="Bodega 2", floor = "Piso 2", side = "Lado 3", amount = 50))
        data.add(ReportUiModel(productId = 1234567897, warehouse="Bodega 2", floor = "Piso 2", side = "Lado 4", amount = 300))

        data.add(ReportUiModel(productId = 1234567898, warehouse="Bodega 3", floor = "Piso 1", side = "Lado 1", amount = 200))
        data.add(ReportUiModel(productId = 1234567899, warehouse="Bodega 3", floor = "Piso 2", side = "Lado 2", amount = 100))
        data.add(ReportUiModel(productId = 1234567880, warehouse="Bodega 3", floor = "Piso 3", side = "Lado 3", amount = 222))
        data.add(ReportUiModel(productId = 1234567881, warehouse="Bodega 3", floor = "Piso 4", side = "Lado 4", amount = 300))

        data.add(ReportUiModel(productId = 1234567871, warehouse="Bodega 1", floor = "Piso 1", side = "Lado 1", amount = 100))
        data.add(ReportUiModel(productId = 1234567872, warehouse="Bodega 2", floor = "Piso 1", side = "Lado 2", amount = 100))
        data.add(ReportUiModel(productId = 1234567873, warehouse="Bodega 3", floor = "Piso 3", side = "Lado 3", amount = 122))
        data.add(ReportUiModel(productId = 1234567874, warehouse="Bodega 4", floor = "Piso 3", side = "Lado 4", amount = 30))
    }

}
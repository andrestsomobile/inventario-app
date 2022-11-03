package com.koba.inventario.positioning

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

class PositioningFragment : Fragment() {

    private val viewModel: PositioningViewModel by activityViewModels()
    private lateinit var username : TextView
    private lateinit var positioningButton : Button
    private lateinit var positioningRecyclerView : RecyclerView
    private lateinit var positioningSearchView : SearchView
    private var data : List<PositioningUiModel> = ArrayList()
    private lateinit var adapter: PositioningAdapter
    private lateinit var navController: NavController
    private lateinit var user: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_positioning, container, false)
        navController = findNavController()
        setHasOptionsMenu(true)
        initLiveData()
        initViews(view)
        return view
    }

    private fun initViews(view: View) {
        username = view.findViewById(R.id.username)
        positioningButton = view.findViewById(R.id.buttonPositioning)
        positioningSearchView = view.findViewById(R.id.positioningSearchView)

        positioningRecyclerView = view.findViewById(R.id.positioningRecyclerView)
        positioningRecyclerView.layoutManager = LinearLayoutManager(this.context)

        positioningSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                filter(query)
                return false
            }
            override fun onQueryTextChange(newText: String): Boolean {
                filter(newText)
                return false
            }
        })

        val args : PositioningFragmentArgs by navArgs()
        username.text = "¡Hola, "+args.username+"!"
        user = args.login
        positioningButton.setOnClickListener {
            navController.navigate(
                PositioningFragmentDirections.actionPositioningFragmentToPositionFragment(
                    args.username, args.login
                )
            )
        }
    }

    private fun initLiveData() {

        viewModel.validatePositioningLiveData.observe(viewLifecycleOwner) { result ->
            if (result) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.positioning_field_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        viewModel.positioningLiveData.observe(viewLifecycleOwner) { result ->
            adapter = PositioningAdapter()
            positioningRecyclerView.adapter = adapter
            data = result
            adapter.setData(result)
            adapter.notifyDataSetChanged()
        }

        viewModel.findAllPositioning()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.overflow_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                navController.navigate(PositioningFragmentDirections.actionPositioningFragmentToLoginFragment())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
        //return NavigationUI.onNavDestinationSelected(item!!, requireView().findNavController()) || super.onOptionsItemSelected(item)
    }

    private fun filter(text: String) {
        val filteredList: ArrayList<PositioningUiModel> = ArrayList()
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
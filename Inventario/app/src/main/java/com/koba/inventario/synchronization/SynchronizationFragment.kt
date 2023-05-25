package com.koba.inventario.synchronization

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
import com.koba.inventario.database.AppDatabase
import com.koba.inventario.pickup.PickupViewModel
import com.koba.inventario.positioning.PositionViewModel
import com.koba.inventario.positioning.TrafficViewModel
import com.koba.inventario.report.ValidateViewModel

class SynchronizationFragment : Fragment() {

    private val viewModel: SynchronizationViewModel by activityViewModels()
    private val viewModelValidation: ValidateViewModel by activityViewModels()
    private val pickupViewModel: PickupViewModel by activityViewModels()
    private val positionViewModel: PositionViewModel by activityViewModels()
    private val viewModelTraffic: TrafficViewModel by activityViewModels()
    private lateinit var username : TextView
    private lateinit var processSyncRecyclerView : RecyclerView
    private lateinit var navController: NavController
    private var adapter = ProcessSyncAdapter(listOf())
    private lateinit var progressBar: ProgressBar
    private val args : SynchronizationFragmentArgs by navArgs()
    private lateinit var user: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_synchronization, container, false)
        navController = findNavController()
        setHasOptionsMenu(true)
        initViews(view)
        initLiveData()
        return view
    }

    private fun initViews(view: View) {
        username = view.findViewById(R.id.username)
        processSyncRecyclerView = view.findViewById(R.id.processRecyclerView)
        processSyncRecyclerView.layoutManager = LinearLayoutManager(this.context)
        progressBar = view.findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE

        username.text = "¡Hola, "+args.username+"!"
        user = args.login
        processSyncRecyclerView.adapter = adapter

    }

    private fun initLiveData() {
        viewModel.setDataBase(AppDatabase.getDatabase(requireContext().applicationContext))
        viewModel.syncLiveData.observe(viewLifecycleOwner) { result ->
            adapter.setItemsRecyclerView(result)
            progressBar.visibility = View.GONE
        }

        viewModel.syncValidationLiveData.observe(viewLifecycleOwner) { result ->
            if(result != null && result.isNotEmpty()) {
                viewModelValidation.findByUser(user, "1")
            }
        }

        viewModel.syncPickupLiveData.observe(viewLifecycleOwner) { result ->
            if(result != null && result.isNotEmpty()) {
                pickupViewModel.findByUser(user)
            }
        }

        viewModel.syncPositionLiveData.observe(viewLifecycleOwner) { result ->
            if(result != null && result.isNotEmpty()) {
                positionViewModel.findByUser(user)
                viewModelTraffic.findByUser(user)
            }
        }

        viewModel.findSynchronizationByUser(user)
        viewModel.findSynchronizationValidationByUser(user)
        viewModel.findSynchronizationPickupByUser(user)
        viewModel.findSynchronizationPositionByUser(user)


    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.overflow_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                navController.navigate(SynchronizationFragmentDirections.actionSynchronizationFragmentToLoginFragment())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
        //return NavigationUI.onNavDestinationSelected(item!!, requireView().findNavController()) || super.onOptionsItemSelected(item)
    }

}
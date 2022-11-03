package com.koba.inventario.dashboard

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.koba.inventario.R

class DashBoardFragment : Fragment() {

    private lateinit var username : TextView
    private lateinit var cardViewReport : CardView
    //private lateinit var cardViewStorageIn : CardView
    private lateinit var cardViewStorageOut : CardView
    private lateinit var cardViewPosition : CardView
    private lateinit var cardViewRelocate : CardView
    private lateinit var cardViewSynchronize : CardView
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        navController = findNavController()
        setHasOptionsMenu(true)
        initViews(view)
        return view
    }

    private fun initViews(view: View) {
        username = view.findViewById(R.id.username)
        cardViewReport = view.findViewById(R.id.cardview_one)
        //cardViewStorageIn = view.findViewById(R.id.cardview_two)
        cardViewStorageOut = view.findViewById(R.id.cardview_three)
        cardViewPosition = view.findViewById(R.id.cardview_four)
        cardViewRelocate = view.findViewById(R.id.cardview_five)
        cardViewSynchronize = view.findViewById(R.id.cardview_six)

        val args : DashBoardFragmentArgs by navArgs()
        username.text = "¡Hola, "+args.username+"!"

        cardViewReport.setOnClickListener {
            navController.navigate(
                DashBoardFragmentDirections.actionDashBoardFragmentToValidateFragment(
                    args.username, args.login
                )
            )
        }
        //cardViewStorageIn.setOnClickListener {
        //    navController.navigate(
        //        DashBoardFragmentDirections.actionDashBoardFragmentToIncomeFragment(
        //            args.username
        //        )
        //    )
        //}
        cardViewStorageOut.setOnClickListener {
            navController.navigate(
                DashBoardFragmentDirections.actionDashBoardFragmentToRequisitionFragment(
                    args.username, args.login
                )
            )
        }
        cardViewPosition.setOnClickListener {
            navController.navigate(
                DashBoardFragmentDirections.actionDashBoardFragmentToPositioningFragment(
                    args.username, args.login
                )
            )
        }
        cardViewRelocate.setOnClickListener {
            navController.navigate(
                DashBoardFragmentDirections.actionDashBoardFragmentToRelocationFragment(
                    args.username, args.login
                )
            )
        }
        cardViewSynchronize.setOnClickListener {
            navController.navigate(
                DashBoardFragmentDirections.actionDashBoardFragmentToSynchronizationFragment(
                    args.username, args.login
                )
            )
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.overflow_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                navController.navigate(DashBoardFragmentDirections.actionDashBoardFragmentToLoginFragment())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
        //return NavigationUI.onNavDestinationSelected(item!!, requireView().findNavController()) || super.onOptionsItemSelected(item)
    }

}
package com.koba.inventario.login

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.koba.inventario.ApiClient
import com.koba.inventario.R
import retrofit2.Call
import retrofit2.Response

class LoginFragment : Fragment() {

    private lateinit var navController: NavController
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var loginbutton: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        navController = findNavController()
        initViews(view)
        return view
    }

    private fun initViews(view: View) {
        username = view.findViewById(R.id.input_username)
        password = view.findViewById(R.id.input_password)
        progressBar = view.findViewById(R.id.progressBar)
        loginbutton = view.findViewById(R.id.button_login)

        //username.setText("sistemas")
        //password.setText("sanapi96")
        loginbutton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            if (TextUtils.isEmpty(username.text.toString()) || TextUtils.isEmpty(password.text.toString())) {
                Toast.makeText(requireContext(), getString(R.string.required_field), Toast.LENGTH_SHORT)
                    .show()
                progressBar.visibility = View.INVISIBLE
            } else login()
        }
    }


    private fun login() {
        val loginRequestBody = LoginRequestBody(
            login = username.text.toString(),
            clave = password.text.toString()
        )
        val loginResponseCall = ApiClient.userService.userLogin(loginRequestBody.login,loginRequestBody.clave)
        loginResponseCall?.enqueue(object : retrofit2.Callback<LoginResponse?>{
            override fun onResponse(
                call: Call<LoginResponse?>,
                response: Response<LoginResponse?>
            ) {
                if(response.isSuccessful and response.body()?.status.equals(getString(R.string.response_ok))){
                    progressBar.visibility = View.INVISIBLE
                    navController.navigate(
                        LoginFragmentDirections.actionLoginFragmentToMenuFragment(
                            response.body()?.data.toString(),username.text.toString()
                        )
                    )
                } else {
                    //navController.navigate(LoginFragmentDirections.actionLoginFragmentToMenuFragment(loginRequestBody.login.toString()))
                    Toast.makeText(requireContext(), getString(R.string.login_field_failed), Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.INVISIBLE
                }
            }

            override fun onFailure(call: Call<LoginResponse?>, t: Throwable) {
                //Toast.makeText(requireContext(), getString(R.string.service_error), Toast.LENGTH_SHORT).show()
                Toast.makeText(requireContext(), t.message.toString(), Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.INVISIBLE
            }
        })
    }

}
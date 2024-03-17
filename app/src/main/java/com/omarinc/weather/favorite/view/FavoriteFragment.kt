package com.omarinc.weather.favorite.view

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.omarinc.weather.currentHomeWeather.view.MyHourAdapter
import com.omarinc.weather.currentHomeWeather.viewmodel.HomeViewModel
import com.omarinc.weather.currentHomeWeather.viewmodel.ViewModelFactory
import com.omarinc.weather.databinding.FragmentFavoriteBinding
import com.omarinc.weather.db.WeatherLocalDataSourceImpl
import com.omarinc.weather.favorite.viewmodel.FavoriteViewModel
import com.omarinc.weather.model.FavoriteCity
import com.omarinc.weather.model.WeatherRepositoryImpl
import com.omarinc.weather.network.DataBaseState
import com.omarinc.weather.network.WeatherRemoteDataSourceImpl
import kotlinx.coroutines.launch
import javax.security.auth.login.LoginException

class favoriteFragment : Fragment(),OnClick {

    companion object {
        fun newInstance() = favoriteFragment()
    }

    private lateinit var viewModel: FavoriteViewModel
    private  lateinit var binding: FragmentFavoriteBinding
    lateinit var    myFavoriteAdapter:FavoriteAdapter
    private  val TAG = "FavoriteFragment"
    var onClick:OnClick?=null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onClick=this
        val factory = ViewModelFactory(
            WeatherRepositoryImpl.getInstance(
                WeatherRemoteDataSourceImpl.getInstance(requireContext()),
                WeatherLocalDataSourceImpl.getInstance(requireContext())
            ),
            requireActivity()
        )
        viewModel = ViewModelProvider(requireActivity(), factory).get(FavoriteViewModel::class.java)



        binding.fabAddFav.setOnClickListener {

            val action=favoriteFragmentDirections.actionFavoriteFragmentToMapFragment()
            findNavController().navigate(action)
        }

        viewModel.getAllFavorites()
        setupRecyclerView()

    }

    private fun setupRecyclerView()
    {

        myFavoriteAdapter=FavoriteAdapter {
            onClick?.onDeleteFavoriteClick(it)
        }

        binding.rvFavorites.apply {
            adapter=myFavoriteAdapter
        }

        lifecycleScope.launch {
            viewModel.favorite.collect{ result->
                when(result)
                {
                    is DataBaseState.Loading->
                    {
                     /// loading
                        Log.i(TAG, "setupRecyclerView: load")
                    } is DataBaseState.Success->
                    {
                        Log.i(TAG, "setupRecyclerView: Succ")
                        myFavoriteAdapter.submitList(result.favoriteCity)
                    }
                    is DataBaseState.Failure->
                    {
                        Log.i(TAG, "setupRecyclerView Failure ")
                    }
                }
            }
        }
    }

    override fun onDeleteFavoriteClick(city: FavoriteCity) {
        viewModel.deleteFavorite(city)
        Snackbar.make(binding.root,"${city.cityName} Deleted", Snackbar.LENGTH_SHORT).show()

    }

    override fun onCityClick(city: FavoriteCity) {
    }
}
package com.vifac.myturn.retrofitrxjavapolling

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import com.vifac.myturn.retrofitrxjavapolling.databinding.ActivityMainBinding

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.vifac.myturn.retrofitrxjavapolling.APIService.Companion.BASE_URL
import java.util.concurrent.TimeUnit
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import android.widget.Toast

import android.text.TextUtils
import android.util.Log
import androidx.activity.viewModels


class MainActivity : AppCompatActivity() {

    private val viewModel: FirstFragmentViewModel by viewModels()

    var retrofit: Retrofit? = null
    var apiService: APIService? = null
    var disposable: Disposable? = null


    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val gson = GsonBuilder()
            .setLenient()
            .create()


        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            //.addConverterFactory(ScalarsConverterFactory.create())
            .build()

        apiService = retrofit!!.create(APIService::class.java)


        disposable = Observable.interval(
            1000, 5000,
            TimeUnit.MILLISECONDS
        )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::callJokesEndpoint, this::onError)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onPause() {
        super.onPause()
        disposable!!.dispose()
    }

    override fun onResume() {
        super.onResume()
        if (disposable!!.isDisposed) {
            disposable = Observable.interval(
                1000, 5000,
                TimeUnit.MILLISECONDS
            )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::callJokesEndpoint, this::onError)
        }
    }
    private fun onError(throwable: Throwable) {
        Toast.makeText(
            this, "OnError in Observable Timer",
            Toast.LENGTH_LONG
        ).show()
    }


    private fun callJokesEndpoint(aLong: Long) {
        val observable : Observable<Jokes?>? = apiService!!.getRandomJoke("random")
        observable!!.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
            .map { result: Jokes? -> result!!.value }
            .subscribe({joke ->
                if (!TextUtils.isEmpty(joke)) {
                //textView.setText(joke)
                if (joke != null) {
                    viewModel.setJokeText(joke)
                    Log.d("xxx",joke)
                }
            } else {
                Toast.makeText(
                    this, "NO RESULTS FOUND",
                    Toast.LENGTH_LONG
                ).show()
            }},{ err -> Log.e("xxx", err.toString())})
    }



}


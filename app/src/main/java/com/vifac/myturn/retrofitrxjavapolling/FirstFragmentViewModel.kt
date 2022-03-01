package com.vifac.myturn.retrofitrxjavapolling

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FirstFragmentViewModel : ViewModel() {
    private val mutableJokeText = MutableLiveData<String>()
    val joketext : LiveData<String> get() = mutableJokeText

    fun setJokeText(joke : String) {
        mutableJokeText.value = joke
    }

}
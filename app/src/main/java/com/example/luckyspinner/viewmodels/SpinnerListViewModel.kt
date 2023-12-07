package com.example.luckyspinner.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luckyspinner.models.Spinner
import com.example.luckyspinner.util.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SpinnerListViewModel : ViewModel() {
    var spinnerList = MutableLiveData<List<Spinner>>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getSpinners()
        }
    }

    val db = FirebaseFirestore.getInstance()


    fun  getSpinners() {
        val sList : MutableList<Spinner> = ArrayList()
        val mokeChannelId = "asd"

        db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/mokeChannelId/${Constants.FS_USER_SPINNER}")
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    for (document : QueryDocumentSnapshot in it.result) {
                        Log.d(
                            Constants.FIRE_STORE,
                            document.id + " => " + document.data
                        )
                        val  s = Spinner.getSpinnerFromFirestore(document)
                        sList.add(s)
                    }
                    spinnerList.value = sList

                } else {
                    Log.w(
                        Constants.FIRE_STORE,
                        "Error getting documents.",
                        it.exception
                    )
                }

            }
    }

}
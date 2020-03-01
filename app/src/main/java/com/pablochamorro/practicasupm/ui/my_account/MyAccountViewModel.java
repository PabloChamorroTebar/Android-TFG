package com.pablochamorro.practicasupm.ui.my_account;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MyAccountViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public MyAccountViewModel() {
    }

    public LiveData<String> getText() {
        return mText;
    }
}
package com.pablochamorro.practicasupm.ui.practica;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PracticaViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public PracticaViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is share fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
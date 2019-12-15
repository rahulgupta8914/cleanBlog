package com.example.cleanblog.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    public MutableLiveData<String> data;

    public void data(String s){
        if(data == null)
        data = new MutableLiveData<>();
        data.setValue(s);
    }
}

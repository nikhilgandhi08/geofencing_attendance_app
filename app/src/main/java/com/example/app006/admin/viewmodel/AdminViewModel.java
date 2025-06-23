package com.example.app006.admin.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.example.app006.auth.LoginActivity;
import java.util.Calendar;

public class AdminViewModel extends AndroidViewModel {

    public MutableLiveData<String> greetingMessage = new MutableLiveData<>();

    public AdminViewModel(Application application) {
        super(application);
        updateGreetingMessage();
    }

    private void updateGreetingMessage() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String message;

        if (hour >= 5 && hour < 12) {
            message = "Good Morning!";
        } else if (hour >= 12 && hour < 17) {
            message = "Good Afternoon!";
        } else if (hour >= 17 && hour < 21) {
            message = "Good Evening!";
        } else {
            message = "Good Night!";
        }

        greetingMessage.postValue(message);
    }

    public void logoutUser(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}

package com.example.uberproject;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.uberproject.forms.LoginFragment;
import com.example.uberproject.forms.RegisterFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Zakomentarišite jedan od sledeća dva reda da birate koji fragment da učitate:

        // Za Login fragment:
//        loadFragment(new LoginFragment());

        // Za Register fragment:
        loadFragment(new RegisterFragment());
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    // Metoda za prebacivanje na Login
    public void showLoginFragment() {
        loadFragment(new LoginFragment());
    }

    // Metoda za prebacivanje na Register
    public void showRegisterFragment() {
        loadFragment(new RegisterFragment());
    }
}
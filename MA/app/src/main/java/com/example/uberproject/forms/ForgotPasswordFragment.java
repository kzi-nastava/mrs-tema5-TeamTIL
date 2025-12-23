package com.example.uberproject.forms;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.uberproject.R;
import androidx.appcompat.widget.AppCompatButton;
public class ForgotPasswordFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        AppCompatButton btnCancel = view.findViewById(R.id.btnCancel);
        AppCompatButton btnResetPassword = view.findViewById(R.id.btnResetPassword);

        btnCancel.setOnClickListener(v -> {
            // Vrati se na Login fragment
            getParentFragmentManager().popBackStack();
        });

        btnResetPassword.setOnClickListener(v -> {
            // Implementiraj reset password logiku
        });

        return view;
    }
}
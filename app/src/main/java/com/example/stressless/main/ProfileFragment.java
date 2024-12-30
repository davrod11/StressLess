package com.example.stressless.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.stressless.LoginActivity;
import com.example.stressless.main.profile.ConnectionsFragment;
import com.example.stressless.main.profile.AboutUsFragment;
import com.example.stressless.R;
import com.example.stressless.main.profile.SettingsFragment;
import com.example.stressless.main.profile.UserInfoFragment;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.section_user_info).setOnClickListener(v -> replaceFragment(new UserInfoFragment(), "UserInfoFragment"));
        view.findViewById(R.id.section_connections).setOnClickListener(v -> replaceFragment(new ConnectionsFragment(), "ConnectionsFragment"));
        view.findViewById(R.id.section_settings).setOnClickListener(v -> replaceFragment(new SettingsFragment(), "SettingsFragment"));
        view.findViewById(R.id.section_preferences).setOnClickListener(v -> replaceFragment(new AboutUsFragment(), "PreferencesFragment"));
        view.findViewById(R.id.section_logout).setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

    }
    private void replaceFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_layout, fragment, tag);
        fragmentTransaction.addToBackStack(tag);
        fragmentTransaction.commit();
    }
}

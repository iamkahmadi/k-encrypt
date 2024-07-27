package com.example.kencrypt;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class SecretPhrase extends Fragment {

    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_PREF = "secretKey";
    private SecretKey currentSecretKey;

    // Use instance variables
    private EditText editTextSecretKey;
    private TextView textViewSecretKey;
    private Button buttonGenerateKey, buttonCopyKey, btnSaveKey;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_secret_phrase, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize instance variables
        editTextSecretKey = view.findViewById(R.id.editTextSecretKey);
        textViewSecretKey = view.findViewById(R.id.textViewSecretKey);
        buttonGenerateKey = view.findViewById(R.id.buttonGenerateKey);
        buttonCopyKey = view.findViewById(R.id.buttonCopyKey);
        btnSaveKey = view.findViewById(R.id.buttonSaveKey);

        // Load key from SharedPreferences
        try {
            String savedKey = getKeyFromSharedPreferences();
            if (savedKey != null) {
                loadKeyToTextView(savedKey);
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error loading saved key: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        // Generate Key Button Listener
        buttonGenerateKey.setOnClickListener(v -> {
            String inputKey = editTextSecretKey.getText().toString();
            try {
                if (inputKey.isEmpty()) {
                    // Generate a new random key
                    currentSecretKey = generateRandomKey();
                    String encodedKey = Base64.getEncoder().encodeToString(currentSecretKey.getEncoded());
                    textViewSecretKey.setText(encodedKey);
                    editTextSecretKey.setText(encodedKey);
                    Toast.makeText(getContext(), "New key generated", Toast.LENGTH_SHORT).show();
                } else {
                    // Use the provided key
                    currentSecretKey = generateKey(inputKey);
                    String encodedKey = Base64.getEncoder().encodeToString(currentSecretKey.getEncoded());
                    textViewSecretKey.setText(encodedKey);
                }
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error generating key: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Save Key Button Listener
        btnSaveKey.setOnClickListener(v -> {
            String inputKeyCopy = editTextSecretKey.getText().toString();
            if (!inputKeyCopy.isEmpty()) {
                try {
                    saveKeyToSharedPreferences(inputKeyCopy);
                    loadKeyToTextView(inputKeyCopy);
                    Toast.makeText(getContext(), "Key saved", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Error saving key: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "No key to save", Toast.LENGTH_SHORT).show();
            }
        });

        // Copy Key Button Listener
        buttonCopyKey.setOnClickListener(v -> {
            String key = getKeyFromSharedPreferences();
            if (key != null) {
                try {
                    ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("SecretKey", key));
                    Toast.makeText(getContext(), "Key copied to clipboard", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Error copying key to clipboard: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "No key found to copy", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static SecretKey generateKey(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        try {
            return new SecretKeySpec(key.getBytes(), "AES");
        } catch (Exception e) {
            throw new InvalidKeySpecException("Invalid key specification: " + e.getMessage(), e);
        }
    }

    private SecretKey generateRandomKey() throws NoSuchAlgorithmException {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256); // AES key size can be 128, 192, or 256 bits
            return keyGenerator.generateKey();
        } catch (Exception e) {
            throw new NoSuchAlgorithmException("Error generating random key: " + e.getMessage(), e);
        }
    }

    private void saveKeyToSharedPreferences(String key) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PREF, key);
        editor.apply();
    }

    private String getKeyFromSharedPreferences() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_PREF, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadKeyToTextView(String k) {
        try {
            currentSecretKey = new SecretKeySpec(Base64.getDecoder().decode(k), "AES");
            String encodedKey = Base64.getEncoder().encodeToString(currentSecretKey.getEncoded());
            textViewSecretKey.setText(encodedKey);
            editTextSecretKey.setText(""); // Optionally clear the EditText if it's not needed
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error loading key to TextView: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}

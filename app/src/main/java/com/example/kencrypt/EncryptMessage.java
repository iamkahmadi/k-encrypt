package com.example.kencrypt;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
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

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class EncryptMessage extends Fragment {

    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_PREF = "secretKey";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_encrypt_message, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText etString = view.findViewById(R.id.et_string);
        Button btnEncrypt = view.findViewById(R.id.btn_encrypt);
        Button btnDecrypt = view.findViewById(R.id.btn_decrypt);
        Button btnCopyMessage = view.findViewById(R.id.btn_copy_message);
        Button btnClearMessageInput = view.findViewById(R.id.btn_clear_message_input);
        TextView tvResults = view.findViewById(R.id.tv_results);

        btnEncrypt.setOnClickListener(v -> {
            try {
                SecretKey secretKey = getKeyFromSharedPreferences();
                if (secretKey == null) {
                    Toast.makeText(getContext(), "No key found. Please generate or save a key first.", Toast.LENGTH_SHORT).show();
                    return;
                }
                String strResult = encryptMsg(etString.getText().toString(), secretKey);
                tvResults.setText(strResult);
            } catch (Exception e) {
                handleEncryptionError(e);
            }
        });

        btnDecrypt.setOnClickListener(v -> {
            try {
                SecretKey secretKey = getKeyFromSharedPreferences();
                if (secretKey == null) {
                    Toast.makeText(getContext(), "No key found. Please generate or save a key first.", Toast.LENGTH_SHORT).show();
                    return;
                }
                String strResult = decryptMsg(etString.getText().toString(), secretKey);
                tvResults.setText(strResult);
            } catch (Exception e) {
                handleDecryptionError(e);
            }
        });

        btnCopyMessage.setOnClickListener(v -> {
            String encryptedMsg = tvResults.getText().toString();
            if (!encryptedMsg.isEmpty()) {
                try {
                    ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("EncryptedMessage", encryptedMsg));
                    Toast.makeText(getContext(), "Encrypted message copied to clipboard", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Error copying to clipboard", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "No encrypted message to copy", Toast.LENGTH_SHORT).show();
            }
        });

        btnClearMessageInput.setOnClickListener(v -> {
            etString.setText("");
            tvResults.setText("");
        });
    }

    private void handleEncryptionError(Exception e) {
        e.printStackTrace();
        Toast.makeText(getContext(), "Error encrypting message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void handleDecryptionError(Exception e) {
        e.printStackTrace();
        Toast.makeText(getContext(), "Error decrypting message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    public String encryptMsg(String message, SecretKey secret)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        byte[] cipherText = cipher.doFinal(message.getBytes("UTF-8"));
        return Base64.encodeToString(cipherText, Base64.NO_WRAP);
    }

    public String decryptMsg(String cipherText, SecretKey secret)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidParameterSpecException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret);
        byte[] decode = Base64.decode(cipherText, Base64.NO_WRAP);
        return new String(cipher.doFinal(decode), "UTF-8");
    }

    private SecretKey getKeyFromSharedPreferences() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String key = sharedPreferences.getString(KEY_PREF, null);
        if (key == null) {
            return null;
        }
        try {
            byte[] decodedKey = Base64.decode(key, Base64.NO_WRAP);
            return new SecretKeySpec(decodedKey, "AES");
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error retrieving key", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}

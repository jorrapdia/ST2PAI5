package com.example.myapplication;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import java.io.*;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    // Setup Server information
    protected static String server = "127.0.0.1";
    protected static int port = 8443;
    private static final String[] PROTOCOLS = new String[]{"TLSv1.3"};
    private static final String[] CIPHER_SUITES = new String[]{"TLS_AES_128_GCM_SHA256"};
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static final String ERROR_MSG = "Ha ocurrido un error inesperado, inténtelo de nuevo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Capturamos el boton de Enviar
        View button = findViewById(R.id.button_send);

        // Llama al listener del boton Enviar
        button.setOnClickListener(view -> showDialog());

    }

    // Creación de un cuadro de dialogo para confirmar pedido
    private void showDialog() throws Resources.NotFoundException {
        EditText bedsField = (EditText) findViewById(R.id.bedsField);
        Integer bedsNumber = transformToInteger(bedsField.getText());

        EditText tablesField = (EditText) findViewById(R.id.tablesField);
        Integer tablesNumber = transformToInteger(tablesField.getText());

        EditText chairsField = (EditText) findViewById(R.id.chairsField);
        Integer chairsNumber = transformToInteger(chairsField.getText());

        EditText armchairsField = (EditText) findViewById(R.id.armchairsField);
        Integer armchairsNumber = transformToInteger(armchairsField.getText());

        EditText clientNumberField = (EditText) findViewById(R.id.clientField);

        String error = validateForm(bedsNumber, tablesNumber, chairsNumber, armchairsNumber, clientNumberField.getText().toString());
        if (error != null) {
            Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
        } else {
            // Catch ok button and send information
            new AlertDialog.Builder(this)
                    .setTitle("Enviar")
                    .setMessage("Se va a proceder al envío del pedido")
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                                DatabaseContract.ClientDbHelper dbHelper = new DatabaseContract.ClientDbHelper(getApplicationContext());
                                SQLiteDatabase db = dbHelper.getReadableDatabase();

                                String selection = DatabaseContract.ClientEntry.COLUMN_NAME_NAME + " = ?";
                                String[] selectionArgs = {clientNumberField.getText().toString()};

                                Cursor cursor = db.query(
                                        DatabaseContract.ClientEntry.TABLE_NAME,
                                        null,
                                        selection,
                                        selectionArgs,
                                        null,
                                        null,
                                        null
                                );

                                String popupMsg;
                                PublicKey publicKey = null;
                                PrivateKey privateKey = null;

                                if (cursor.moveToNext()) {
                                    byte[] pub = hexStringToByteArray(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ClientEntry.COLUMN_NAME_PUBLICKKEY)));
                                    byte[] pvt = hexStringToByteArray(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ClientEntry.COLUMN_NAME_PRIVATEKEY)));
                                    X509EncodedKeySpec publicKs = new X509EncodedKeySpec(pub);
                                    PKCS8EncodedKeySpec privateKs = new PKCS8EncodedKeySpec(pvt);

                                    try {
                                        KeyFactory kf = KeyFactory.getInstance("RSA");
                                        publicKey = kf.generatePublic(publicKs);
                                        privateKey = kf.generatePrivate(privateKs);
                                    } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                                        popupMsg = ERROR_MSG;
                                    }

                                    cursor.close();
                                } else {
                                    cursor.close();
                                    KeyPairGenerator keygen;
                                    try {
                                        keygen = KeyPairGenerator.getInstance("RSA");
                                        keygen.initialize(4096);
                                        KeyPair kp = keygen.generateKeyPair();

                                        publicKey = kp.getPublic();
                                        privateKey = kp.getPrivate();

                                        db = dbHelper.getWritableDatabase();
                                        ContentValues values = new ContentValues();
                                        values.put(DatabaseContract.ClientEntry.COLUMN_NAME_NAME, clientNumberField.getText().toString());
                                        values.put(DatabaseContract.ClientEntry.COLUMN_NAME_PUBLICKKEY, bytesToHex(publicKey.getEncoded()));
                                        values.put(DatabaseContract.ClientEntry.COLUMN_NAME_PRIVATEKEY, bytesToHex(privateKey.getEncoded()));
                                        db.insert(DatabaseContract.ClientEntry.TABLE_NAME, null, values);
                                    } catch (NoSuchAlgorithmException e) {
                                        popupMsg = ERROR_MSG;
                                    }
                                }

                                if (publicKey != null && privateKey != null) {
                                    try {
                                        Signature signature = Signature.getInstance("SHA512withRSA");
                                        signature.initSign(privateKey);
                                        String msg = bedsNumber + " " +
                                                tablesNumber + " " +
                                                chairsNumber + " " +
                                                armchairsNumber + " " +
                                                clientNumberField.getText().toString();
                                        signature.update(msg.getBytes());
                                        byte[] sign = signature.sign();

                                        String data = msg + ";" + bytesToHex(publicKey.getEncoded()) + ";" + bytesToHex(sign);
                                        popupMsg = sendRequest(data);

                                    } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | IOException e) {
                                        popupMsg = ERROR_MSG;
                                    }
                                }
                                Toast.makeText(MainActivity.this, "Petición enviada correctamente", Toast.LENGTH_SHORT).show();
                            }
                    )
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }
    }

    private String validateForm(Integer bedsNumber, Integer tablesNumber, Integer chairsNumber, Integer armchairsNumber, String clientNumber) {
        String errorMsg = null;

        if (bedsNumber == null || tablesNumber == null || chairsNumber == null || armchairsNumber == null || clientNumber.isEmpty()) {
            errorMsg = "Tienes que rellenar todos los campos";
        } else if (bedsNumber < 0 || bedsNumber > 300 || tablesNumber < 0 || tablesNumber > 300 || chairsNumber < 0 || chairsNumber > 300 ||
                armchairsNumber < 0 || armchairsNumber > 300) {
            errorMsg = "Los campos numéricos deben estar entre 0 y 300";
        }
        return errorMsg;
    }

    private Integer transformToInteger(Editable text) {
        Integer res = null;
        if (text.length() > 0) {
            res = Integer.valueOf(text.toString());
        }
        return res;
    }

    private String sendRequest(String data) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        assert classLoader != null;
        System.setProperty("javax.net.ssl.keyStore", "app/client.keystore");
        System.setProperty("javax.net.ssl.keyStorePassword", "prueba");
        System.setProperty("javax.net.ssl.trustStore", "app/server.keystore");
        System.setProperty("javax.net.ssl.trustStorePassword", "prueba");

        SSLSocket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        String response = "";

        try {
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            socket = (SSLSocket) factory.createSocket(server, port);

            socket.setEnabledProtocols(PROTOCOLS);
            socket.setEnabledCipherSuites(CIPHER_SUITES);

            socket.startHandshake();

            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));

            /* send data */
            out.write(data);
            out.flush();

            if (out.checkError())
                System.out.println("SSLSocketClient:  java.io.PrintWriter error");

            /* read response */
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response += inputLine;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null)
                socket.close();
            if (out != null)
                out.close();
            if (in != null)
                in.close();
        }
        return response;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

}

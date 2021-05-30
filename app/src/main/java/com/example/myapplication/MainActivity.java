package com.example.myapplication;

import android.app.AlertDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    // Setup Server information
    protected static String server = "192.168.1.133";
    protected static int port = 7070;

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
                    .setMessage("Se va a proceder al envio")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {

                        // 1. Extraer los datos de la vista

                        // 2. Firmar los datos

                        // 3. Enviar los datos

                        Toast.makeText(MainActivity.this, "Petición enviada correctamente", Toast.LENGTH_SHORT).show();
                    }

                    )
                    .

                            setNegativeButton(android.R.string.no, null)

                    .

                            show();
        }
    }

    private String validateForm(Integer bedsNumber, Integer tablesNumber, Integer chairsNumber, Integer armchairsNumber, String clientNumber) {
        String errorMsg = null;

        if (bedsNumber == null || tablesNumber == null || chairsNumber == null || armchairsNumber == null || clientNumber.isEmpty()) {
            errorMsg = "Tienes que rellenar todos los campos";
        } else if (bedsNumber < 0 || bedsNumber > 300 ||
                tablesNumber < 0 || tablesNumber > 300 ||
                chairsNumber < 0 || chairsNumber > 300 ||
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


}

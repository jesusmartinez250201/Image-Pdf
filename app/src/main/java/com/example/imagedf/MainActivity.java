package com.example.imagedf;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    EditText txtNombrePdf;
    Button btnSelectImages;
    Button btnCreatePdf;
    private static final String directorioInicial = Environment.getExternalStorageDirectory().toString();
    private static final String nombreDirectorio = "PDFs";
    ArrayList<Uri> listaImagenes = new ArrayList<>();
    ArrayList<String> listaRutasImagenes = new ArrayList<>();
    TextView txtVerRutas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtNombrePdf = findViewById(R.id.txtNombrePdf);
        btnSelectImages = findViewById(R.id.btnSelectImages);
        btnCreatePdf = findViewById(R.id.btnCreatePdf);
        txtVerRutas = findViewById(R.id.txtVerRutas);

        btnCreatePdf.setOnClickListener((view) -> {
            String nombrePdf = txtNombrePdf.getText().toString();
            if (nombrePdf.trim().isEmpty()) {
                //EL NOMBRE DEL PDF ESTA VACIO
                txtNombrePdf.setError("El nombre del pdf es obligatorio");
                txtNombrePdf.requestFocus();
            } else {
                createPdf();
            }
        });

        btnSelectImages.setOnClickListener((view) -> {
            selectImages();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                //CUANDO SE SELECCIONAN VARIAS IMAGENES
                //PRUEBA: vaciar el textView
                txtVerRutas.setText("");
                listaImagenes.clear();
                listaRutasImagenes.clear();
                int imagenesSeleccionadas = data.getClipData().getItemCount();
                //AGREGAR LAS IMAGENES A LA LISTA
                for (int i = 0; i < imagenesSeleccionadas; i++) {
                    Uri imagenUri = data.getClipData().getItemAt(i).getUri();
                    listaImagenes.add(imagenUri);
                    listaRutasImagenes.add(imagenUri.getPath());
                }
                //PRUEBA: Ver rutas de las imagenes seleccionadas
                for (String ruta : listaRutasImagenes) {
                    txtVerRutas.append(ruta + "\n");
                }
            } else if (data.getData() != null) {
                //SI SOLO SELECCIONA UNA IMAGEN
                //PRUEBA: Vaciar el TextView
                txtVerRutas.setText("");
                listaImagenes.clear();
                listaRutasImagenes.clear();
                Uri imagenUri = data.getData();
                listaImagenes.add(imagenUri);
                listaRutasImagenes.add(imagenUri.getPath());
                txtVerRutas.setText(listaRutasImagenes.get(0));
            }

        }

    }

    /**
     * Método para seleccionar las imágenes
     */
    private void selectImages() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Selecciona las imagenes"), 1);

    }

    /**
     * Método para crear el pdf
     */
    private void createPdf() {
        String nombrePdf = txtNombrePdf.getText().toString();
        File directorio = new File(directorioInicial + File.separator + nombreDirectorio);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }
        File pdf = new File(directorio, nombrePdf + ".pdf");
        if (pdf.exists()) {
            txtNombrePdf.setError("El nombre del pdf ya existe");
            txtNombrePdf.requestFocus();
        } else {
        }
    }

}

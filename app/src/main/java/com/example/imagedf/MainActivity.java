package com.example.imagedf;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    //String[] head_list={"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R"};
    private ListAdapter adapter=null;
    private ArrayList<String> array=new ArrayList<String>(Arrays.asList());


    //TODO: MOdificar para hacer posible ordenar las imagenes
    //TODO: *SELECTOR DE DIRECTORIOS
    //TODO: *REMPLAZAR URI POR NOMBRE DE ARCHIVO ESTO OPCIONAL?
    //TODO: AJUSTAR  EL TAMAÑO DE LAS IMAGENES A LA PAGINA O ROTAR(ESTO SI :C)
    EditText txtNombrePdf;
    Button btnSelectImages;
    Button btnCreatePdf;
    Button btnCambiarDirectorio;
    private static final String directorioInicial = Environment.getExternalStorageDirectory().toString();
    private static final String nombreDirectorio = "PDFs";
    private static String directorioActual = directorioInicial + File.separator + nombreDirectorio;
    ArrayList<Uri> listaImagenes = new ArrayList<>();
    ArrayList<String> listaRutasImagenes = new ArrayList<>();
    TextView txtVerRutas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Esto lo agregue yo para poner listView
        TouchListView tlv=(TouchListView) findViewById(R.id.touch_listview);
        adapter=new ListAdapter();
        tlv.setAdapter(adapter);
        //array.add("S");
        //array.add("A");
        //hasta aqui termina lo que agregue

        txtNombrePdf = findViewById(R.id.txtNombrePdf);
        btnSelectImages = findViewById(R.id.btnSelectImages);
        btnCreatePdf = findViewById(R.id.btnCreatePdf);
        txtVerRutas = findViewById(R.id.txtVerRutas);
        btnCambiarDirectorio = findViewById(R.id.btnCambiarDirectorio);

        btnCreatePdf.setOnClickListener((view) -> {
            String nombrePdf = txtNombrePdf.getText().toString();
            if (nombrePdf.trim().isEmpty()) {
                Toast.makeText(this, "Ingrese un nombre para el PDF", Toast.LENGTH_SHORT).show();
            } else {
                if (listaImagenes.size() > 0) {
                    createPdf();
                } else {
                    Toast.makeText(this, "No hay imagenes seleccionadas", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSelectImages.setOnClickListener((view) -> {
            selectImages();
        });

        tlv.setDropListener(onDrop);
        tlv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {
            }
        });

        //LISTENER PARA CAMBIAR DIRECTORIO
        btnCambiarDirectorio.setOnClickListener((view) -> {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
                Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                i.addCategory(Intent.CATEGORY_DEFAULT);
                startActivityForResult(Intent.createChooser(i, "Choose directory"), 9999);
            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK && data != null) {
                    if (data.getClipData() != null) {
                        //CUANDO SE SELECCIONAN VARIAS IMAGENES
                        //PRUEBA: vaciar el textView
                        txtVerRutas.setText("");
                        array.clear();
                        adapter.notifyDataSetChanged();
                        listaImagenes.clear();
                        listaRutasImagenes.clear();
                        int imagenesSeleccionadas = data.getClipData().getItemCount();
                        //AGREGAR LAS IMAGENES A LA LISTA
                        for (int i = 0; i < imagenesSeleccionadas; i++) {
                            Uri imagenUri = data.getClipData().getItemAt(i).getUri();
                            listaImagenes.add(imagenUri);
                            array.add(imagenUri.getPath());

                            listaRutasImagenes.add(imagenUri.getPath());
                            //Toast.makeText(this, "Ruta: " + imagenUri.getPath(), Toast.LENGTH_SHORT).show();
                        }
                        //esto termina de actualizar el listView
                        adapter.notifyDataSetChanged();
                        //PRUEBA: Ver rutas de las imagenes seleccionadas
                        for (String ruta : listaRutasImagenes) {

                            txtVerRutas.append(ruta + "\n");
                            //array.add(ruta);

                        }

                    } else if (data.getData() != null) {
                        //SI SOLO SELECCIONA UNA IMAGEN
                        //PRUEBA: Vaciar el TextView
                        txtVerRutas.setText("");
                        listaImagenes.clear();
                        array.clear();
                        //esto termina de actualizar el listView
                        adapter.notifyDataSetChanged();
                        listaRutasImagenes.clear();
                        Uri imagenUri = data.getData();
                        listaImagenes.add(imagenUri);
                        listaRutasImagenes.add(imagenUri.getPath());
                        array.add(imagenUri.getPath());
                        //esto termina de actualizar el listView
                        adapter.notifyDataSetChanged();
                        txtVerRutas.setText(listaRutasImagenes.get(0));
                    }

                }
                break;
            case 9999:
                if (resultCode == RESULT_OK && data != null) {
                    Uri uri = data.getData();
                    String path = uri.getPath();
                    String[] split = path.split(":");
                    String newPath = split[1];
                    directorioActual = directorioInicial + File.separator + newPath;
                    Toast.makeText(this, "Directorio actual: " + directorioActual, Toast.LENGTH_SHORT).show();
                }
                break;
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
        if (listaImagenes.size() == 0 ) {
            Toast.makeText(this, "No hay imagenes seleccionadas", Toast.LENGTH_SHORT).show();
            return;
        }
        String nombrePdf = txtNombrePdf.getText().toString();
        File directorio = new File(directorioActual);
        if (!directorio.exists()) {
            directorio.mkdir();
        }
        File pdf = new File(directorio, nombrePdf + ".pdf");
        if (pdf.exists()) {
            txtNombrePdf.setError("El nombre del pdf ya existe");
            txtNombrePdf.requestFocus();
        } else {
            File f = new File(directorioActual, nombrePdf + ".pdf");
            FileOutputStream ficheroPdf = null;
            try {
                ficheroPdf = new FileOutputStream(f);
                Document documento = new Document();
                PdfWriter.getInstance(documento, ficheroPdf);
                documento.open();
                for (int i = 0; i < listaImagenes.size(); i++) {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), listaImagenes.get(i));
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    Image imagen = Image.getInstance(stream.toByteArray());
                    imagen.scaleToFit(PageSize.A4.getWidth() - 20, PageSize.A4.getHeight() - 20);
                    imagen.setAlignment(Image.ALIGN_CENTER);
                    documento.add(imagen);
                    documento.newPage();
                }
                documento.close();
                Toast.makeText(this, "Pdf creado correctamente", Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (DocumentException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    //ESto es pa que jale el mover
    private TouchListView.DropListener onDrop=new TouchListView.DropListener() {
        @Override
        public void drop(int from, int to) {
            String item=adapter.getItem(from);
            adapter.remove(item);
            adapter.insert(item, to);
        }
    };
    //ESto es pa que jale el listview asi que no le muevas
    class ListAdapter extends ArrayAdapter<String> {
        ListAdapter() {
            super(MainActivity.this, R.layout.adapter_layout, array);
        }
        public View getView(int position, View convertView,
                            ViewGroup parent) {
            View row=convertView;
            if (row==null) {
                LayoutInflater inflater=getLayoutInflater();
                row=inflater.inflate(R.layout.adapter_layout, parent, false);
            }
            TextView label=(TextView)row.findViewById(R.id.label);
            label.setText(array.get(position));
            return(row);
        }
    }

}
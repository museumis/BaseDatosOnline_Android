package com.example.alumno.basedatosonline;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class Principal extends AppCompatActivity implements View.OnClickListener {

    EditText editId, editNombre, editDireccion;
    Button btnVerTodo, btnInsert, btnUpdate, btnDelete, btnPorId;
    EditText editResultado;
    ImageView img;
    Bitmap bitmap;
    ObtenerWebService hiloConsulta;

    String direccionWeb = "https://imartinr01.000webhostapp.com/";
    String webConsultaVerTodos = direccionWeb + "obtener_alumnos.php";
    String webConsultaPorId = direccionWeb + "obtener_alumno_por_id.php?idalumno=";
    String webConsultaInsertar = direccionWeb + "insertar_alumno.php";
    String webConsultaUpdate = direccionWeb + "actualizar_alumno.php";
    String webConsultaDelete = direccionWeb + "borrar_alumno.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);


        editId = (EditText) findViewById(R.id.editId);
        editNombre = (EditText) findViewById(R.id.editNombre);
        editDireccion = (EditText) findViewById(R.id.editDireccion);
        btnVerTodo = (Button) findViewById(R.id.btnVerTodo);
        btnPorId = (Button) findViewById(R.id.btnPorId);
        btnInsert = (Button) findViewById(R.id.btnInsertar);
        btnUpdate = (Button) findViewById(R.id.btnActualizar);
        btnDelete = (Button) findViewById(R.id.btnBorrar);
        editResultado = (EditText) findViewById(R.id.editResultado);
        img = (ImageView) findViewById(R.id.img);

        btnPorId.setOnClickListener(this);
        btnVerTodo.setOnClickListener(this);
        btnInsert.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //Se lanza hilo
            case R.id.btnVerTodo: {
                hiloConsulta = new ObtenerWebService();
                //Ejecuta el metodo de la clase como si fuera un hilo
                hiloConsulta.execute(webConsultaVerTodos, "1");
                break;
            }
            case R.id.btnPorId: {
                hiloConsulta = new ObtenerWebService();
                hiloConsulta.execute(webConsultaPorId + editId.getText().toString(), "2");
                break;
            }
            case R.id.btnInsertar: {
                hiloConsulta = new ObtenerWebService();
                //Pasamos parametro que se insertaran
                hiloConsulta.execute(webConsultaInsertar, "3", editNombre.getText().toString(), editDireccion.getText().toString());
                break;
            }
            case R.id.btnActualizar: {
                //Tambien se pasa el id porque se actualiza entero
                hiloConsulta = new ObtenerWebService();
                hiloConsulta.execute(webConsultaUpdate, "4", editId.getText().toString(), editNombre.getText().toString(), editDireccion.getText().toString());
                break;
            }
            case R.id.btnBorrar: {
                hiloConsulta = new ObtenerWebService();
                hiloConsulta.execute(webConsultaDelete, "5", editId.getText().toString());
                break;
            }

        }
    }//Fin del swit

    /**
     * Clase que ejecutara las consultas
     * <p>
     * Dar permiso de internet a manifest
     */
    public class ObtenerWebService extends AsyncTask<String, Void, String> {
        //Obligatorio, coje el resultado de doinackground
        @Override
        protected void onPostExecute(String respuestaDeLaConsulta) {
            editResultado.setText(respuestaDeLaConsulta);
            img.setImageBitmap(bitmap);
        }

        //Devuelve un string para el resultado
        //LLegan paramentros[0]=operacion // parametro[1]el string de la consulta para el suwitc
        //Retorna el JSON
        @Override
        protected String doInBackground(String... parametros) {

            String operacion = parametros[0];
            String resultado = "";

            switch (parametros[1]) {
                case "1": {
                    resultado = consultarVerTodo(operacion);
                    break;
                }
                case "2": {
                    resultado = consultarPorID(operacion);
                    break;
                }
                case "3": {
                    resultado = insertar(operacion, parametros[2], parametros[3]);//Los parametro que necesitamos para la insercion, nombrados en el onclick
                    break;
                }
                case "4": {
                    resultado = update(operacion, parametros[2], parametros[3], parametros[4]);

                    break;
                }
                case "5": {
                    resultado = borrado(operacion, parametros[2]);

                    break;
                }
            }

            return resultado;
        }
    }

    public String consultarVerTodo(String operacion) {
        URL url; //Url donde obtener la informacion
        String respuesta = "", devuelve = "";//Devuleve imprime los resultados en onPostExecute
        try {
            //CREAR CONEXION
            url = new URL(operacion);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0" + "(Linux: Android 1.5: es-Es) Ejemplo HTTP)");
            int codigoConexion = connection.getResponseCode();
            //Si nos hemos conectado
            if (codigoConexion == HttpsURLConnection.HTTP_OK) {
                //  Toast.makeText(this, "Conectado a la base de datos (165Principal)", Toast.LENGTH_SHORT).show();
                InputStream is = new BufferedInputStream(connection.getInputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String linea = br.readLine();
                while (linea != null) {
                    respuesta = respuesta + linea;
                    linea = br.readLine();
                    //JSON esta en string respuesta
                    //CREAR OBJECT
                    JSONObject jsonObjectRespuesta = new JSONObject(respuesta);
                    String estado = jsonObjectRespuesta.getString("estado");
                    //Si hay objetos en el jsn
                    if (estado.equals("1")) {
                        JSONArray jsonArrayObjetos = jsonObjectRespuesta.getJSONArray("alumnos");

                        for (int i = 0; i < jsonArrayObjetos.length(); i++) {
                            devuelve = devuelve
                                    + jsonArrayObjetos.getJSONObject(i).getString("idAlumno")
                                    + jsonArrayObjetos.getJSONObject(i).getString("nombre")
                                    + jsonArrayObjetos.getJSONObject(i).getString("direccion")
                                    + " \n ";
                        }
                    } else {
                        //No hay objetos en la base de datos
                        devuelve = "No hay objetos en el JSON";
                    }
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Toast.makeText(this, "La url falló", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "La conexión falló", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (JSONException e) {
            Toast.makeText(this, "El JSON falló", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        //Trae estado y lista de alumnos(respuestaJson)(1 hay objetos)

        return devuelve;
    }//Fin de  consultar todos

    public String consultarPorID(String operacion) {
        URL url;
        String carpetaImgs = "https://imartinr01.000webhostapp.com/img/";
        String respuesta = "", devuelve = "";
        try {
            //CREAR CONEXION
            url = new URL(operacion);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0" + "(Linux: Android 1.5: es-Es) Ejemplo HTTP)");
            int codigoConexion = connection.getResponseCode();
            //Si nos hemos conectado
            if (codigoConexion == HttpsURLConnection.HTTP_OK) {
                //Toast.makeText(this, "Conectado a la base de datos (165Principal)", Toast.LENGTH_SHORT).show();
                InputStream is = new BufferedInputStream(connection.getInputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String linea = br.readLine();
                while (linea != null) {
                    respuesta = respuesta + linea;
                    linea = br.readLine();
                    //JSON esta en string respuesta
                    //CREAR OBJECT
                    JSONObject jsonObjectRespuesta = new JSONObject(respuesta);
                    String estado = jsonObjectRespuesta.getString("estado");
                    //Si hay objetos en el jsn
                    if (estado.equals("1")) {
                        devuelve = devuelve
                                + jsonObjectRespuesta.getJSONObject("alumno").getString("idAlumno")
                                + jsonObjectRespuesta.getJSONObject("alumno").getString("nombre")
                                + jsonObjectRespuesta.getJSONObject("alumno").getString("direccion")
                                + "/n";

                        //Si no tiene imagen
                        if (jsonObjectRespuesta.getJSONObject("alumno").getString("rutaimagen").equals("noimagen")) {
                            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.puntorojo);
                        } else {
                            URL urlImagen = new URL(carpetaImgs + jsonObjectRespuesta.getJSONObject("alumno").getString("rutaimagen"));
                            System.out.println("weeeeeeee"+carpetaImgs + jsonObjectRespuesta.getJSONObject("alumno").getString("rutaimagen"));
                            HttpsURLConnection connectionImagen = (HttpsURLConnection) urlImagen.openConnection();
                            connectionImagen.connect();
                            bitmap = BitmapFactory.decodeStream(connectionImagen.getInputStream());
                        }
                    } else {
                        //No hay objetos en la base de datos
                        devuelve = "No hay objetos en el JSON";
                    }
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Toast.makeText(this, "La url falló", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "La conexión falló", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (JSONException e) {
            Toast.makeText(this, "El JSON falló", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        //Trae estado y lista de alumnos(respuestaJson)(1 hay objetos)

        return devuelve;
    }//Fin de  consultar por ID

    public String insertar(String operacion, String nombreAlumno, String direccionAlumno) {
        URL url;
        String respuesta = "", devuelve = "";

        try {
            url = new URL(operacion);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");//Sera un JSON
            connection.setRequestProperty("Accept", "application/json");//Opcion de aceptarlo
            connection.setDoInput(true);//Datos de entrada
            connection.setDoOutput(true);//Para datos de salida
            connection.setUseCaches(false);//Para que lo haga nuevo
            connection.connect();

            JSONObject jsonObjectRespuesta = new JSONObject();
            jsonObjectRespuesta.put("nombre", nombreAlumno);
            jsonObjectRespuesta.put("direccion", direccionAlumno);

            OutputStream os = connection.getOutputStream();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            bw.write(jsonObjectRespuesta.toString());
            bw.flush();
            bw.close();
            os.close();
            int codigoConexion = connection.getResponseCode();
            //Si nos hemos conectado
            if (codigoConexion == HttpsURLConnection.HTTP_OK) {
                InputStream is = new BufferedInputStream(connection.getInputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String linea = br.readLine();
                while (linea != null) {
                    respuesta = respuesta + linea;
                    linea = br.readLine();
                    //JSON esta en string respuesta
                    //CREAR OBJECT
                    jsonObjectRespuesta = new JSONObject(respuesta);
                    String estado = jsonObjectRespuesta.getString("estado");
                    if (estado.equals("1")) {
                        devuelve = "Alumno insertado correctamte";
                    } else {
                        devuelve = "No se insertó el alumno correctamte";
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return devuelve;
    }

    public String borrado(String operacion, String idAlumno) {
        URL url;
        String respuesta = "", devuelve = "";

        try {
            url = new URL(operacion);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");//Sera un JSON
            connection.setRequestProperty("Accept", "application/json");//Opcion de aceptarlo
            connection.setDoInput(true);//Datos de entrada
            connection.setDoOutput(true);//Para datos de salida
            connection.setUseCaches(false);//Para que lo haga nuevo
            connection.connect();

            JSONObject jsonObjectRespuesta = new JSONObject();
            jsonObjectRespuesta.put("idalumno", idAlumno);

            OutputStream os = connection.getOutputStream();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            bw.write(jsonObjectRespuesta.toString());
            bw.flush();
            bw.close();
            os.close();
            int codigoConexion = connection.getResponseCode();
            //Si nos hemos conectado
            if (codigoConexion == HttpsURLConnection.HTTP_OK) {
                InputStream is = new BufferedInputStream(connection.getInputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String linea = br.readLine();
                while (linea != null) {
                    respuesta = respuesta + linea;
                    linea = br.readLine();
                    //JSON esta en string respuesta
                    //CREAR OBJECT
                    jsonObjectRespuesta = new JSONObject(respuesta);
                    String estado = jsonObjectRespuesta.getString("estado");
                    if (estado.equals("1")) {
                        devuelve = "Alumno borrado correctamte";
                    } else {
                        devuelve = "No se borrado el alumno correctamte";
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return devuelve;
    }

    public String update(String operacion, String idAlumno, String nombreAlumno, String direccionAlumno) {
        URL url;
        String respuesta = "", devuelve = "";

        try {
            url = new URL(operacion);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");//Sera un JSON
            connection.setRequestProperty("Accept", "application/json");//Opcion de aceptarlo
            connection.setDoInput(true);//Datos de entrada
            connection.setDoOutput(true);//Para datos de salida
            connection.setUseCaches(false);//Para que lo haga nuevo
            connection.connect();

            JSONObject jsonObjectRespuesta = new JSONObject();
            jsonObjectRespuesta.put("idalumno", idAlumno);
            jsonObjectRespuesta.put("nombre", nombreAlumno);
            jsonObjectRespuesta.put("direccion", direccionAlumno);


            OutputStream os = connection.getOutputStream();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            bw.write(jsonObjectRespuesta.toString());
            bw.flush();
            bw.close();
            os.close();
            int codigoConexion = connection.getResponseCode();
            //Si nos hemos conectado
            if (codigoConexion == HttpsURLConnection.HTTP_OK) {
                InputStream is = new BufferedInputStream(connection.getInputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String linea = br.readLine();
                while (linea != null) {
                    respuesta = respuesta + linea;
                    linea = br.readLine();
                    //JSON esta en string respuesta
                    //CREAR OBJECT
                    jsonObjectRespuesta = new JSONObject(respuesta);
                    String estado = jsonObjectRespuesta.getString("estado");
                    if (estado.equals("1")) {
                        devuelve = "Alumno actualizado correctamte";
                    } else {
                        devuelve = "No se actualizó el alumno correctamte";

                    }

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return devuelve;
    }

}

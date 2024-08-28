package com.afk.agendafirebase

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.afk.agendafirebase.Objects.Contacts
import com.afk.agendafirebase.Objects.FirebaseReferences
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var btnGuardar: Button
    private lateinit var btnListar: Button
    private lateinit var btnLimpiar: Button
    private lateinit var txtNombre: EditText
    private lateinit var txtDireccion: EditText
    private lateinit var txtTelefono1: EditText
    private lateinit var txtTelefono2: EditText
    private lateinit var txtNotas: EditText
    private lateinit var cbkFavorite: CheckBox
    private lateinit var referencia: DatabaseReference
    private var savedContacto: Contacts? = null
    private var id: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initComponents()
        setEvents()
    }

    private fun initComponents() {
        referencia = FirebaseDatabase.getInstance().getReferenceFromUrl(
            FirebaseReferences.DB_URL + "/" + FirebaseReferences.DB_NAME + "/" + FirebaseReferences.TABLE_NAME
        )

        txtNombre = findViewById(R.id.txtNombre)
        txtTelefono1 = findViewById(R.id.txtTelefono1)
        txtTelefono2 = findViewById(R.id.txtTelefono2)
        txtDireccion = findViewById(R.id.txtDireccion)
        txtNotas = findViewById(R.id.txtNotas)
        cbkFavorite = findViewById(R.id.cbxFavorito)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnListar = findViewById(R.id.btnListar)
        btnLimpiar = findViewById(R.id.btnLimpiar)
    }

    private fun setEvents() {
        btnGuardar.setOnClickListener(this)
        btnListar.setOnClickListener(this)
        btnLimpiar.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        if (isNetworkAvailable()) {
            when (view.id) {
                R.id.btnGuardar -> {
                    var completo = true
                    if (txtNombre.text.toString().isEmpty()) {
                        txtNombre.error = "Introduce el nombre"
                        completo = false
                    }
                    if (txtTelefono1.text.toString().isEmpty()) {
                        txtTelefono1.error = "Introduce el teléfono "
                        completo = false
                    }
                    if (txtDireccion.text.toString().isEmpty()) {
                        txtDireccion.error = "Introduce la dirección"
                        completo = false
                    }
                    if (completo) {
                        val nContacto = Contacts().apply {
                            name = txtNombre.text.toString()
                            phoneNumber1 = txtTelefono1.text.toString()
                            phoneNumber2 = txtTelefono2.text.toString()
                            address = txtDireccion.text.toString()
                            notes = txtNotas.text.toString()
                            favorite = if (cbkFavorite.isChecked) 1 else 0
                        }
                        if (savedContacto == null) {
                            agregarContacto(nContacto)
                            Toast.makeText(applicationContext, "Contacto guardado ", Toast.LENGTH_SHORT).show()
                            limpiar()
                        } else {
                            actualizarContacto(id, nContacto)
                            Toast.makeText(applicationContext, "Contacto actualizado", Toast.LENGTH_SHORT).show()
                            limpiar()
                        }
                    }
                }
                R.id.btnLimpiar -> limpiar()
                R.id.btnListar -> {
                    val intent = Intent(this@MainActivity, ListActivity::class.java)
                    limpiar()
                    startActivityForResult(intent, 0)
                }
            }
        } else {
            Toast.makeText(applicationContext, "Se necesita tener conexión a internet", Toast.LENGTH_SHORT).show()
        }
    }

    private fun agregarContacto(c: Contacts) {
        val newContactoReference = referencia.push()
        val id = newContactoReference.key
        if (id != null) {
            c.id = id
            newContactoReference.setValue(c).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Firebase", "Contacto agregado: $c")
                } else {
                    Log.e("Firebase", "Error al agregar", task.exception)
                }
            }
        } else {
            Log.e("Firebase", "Error en ID")
        }
    }

    private fun actualizarContacto(id: String?, p: Contacts) {
        if (id != null) {
            p.id = id
            referencia.child(id).setValue(p).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Firebase", "Contacto actualizado: $p")
                } else {
                    Log.e("Firebase", "Error al actualizar", task.exception)
                }
            }
        } else {
            Log.e("Firebase", "Error en ID")
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val ni = cm.activeNetworkInfo
        return ni != null && ni.isConnected
    }

    private fun limpiar() {
        savedContacto = null
        txtNombre.text.clear()
        txtTelefono1.text.clear()
        txtTelefono2.text.clear()
        txtNotas.text.clear()
        txtDireccion.text.clear()
        cbkFavorite.isChecked = false
        id = ""
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (intent != null) {
            val oBundle = intent.extras
            if (resultCode == RESULT_OK) {
                val contacto = oBundle?.getSerializable("contacto") as? Contacts
                if (contacto != null) {
                    savedContacto = contacto
                    id = contacto.id.toString()
                    txtNombre.setText(contacto.name)
                    txtTelefono1.setText(contacto.phoneNumber1)
                    txtTelefono2.setText(contacto.phoneNumber2)
                    txtDireccion.setText(contacto.address)
                    txtNotas.setText(contacto.notes)
                    cbkFavorite.isChecked = contacto.favorite > 0
                }
            } else {
                limpiar()
            }
        }
    }
}

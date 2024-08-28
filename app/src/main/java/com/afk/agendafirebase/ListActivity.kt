package com.afk.agendafirebase

import android.app.Activity
import android.app.ListActivity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.afk.agendafirebase.Objects.Contacts
import com.afk.agendafirebase.Objects.FirebaseReferences
import com.google.firebase.database.*

class ListActivity : ListActivity() {

    private lateinit var basedatabase: FirebaseDatabase
    private lateinit var referencia: DatabaseReference
    private lateinit var btnNuevo: Button
    private val context: Context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        basedatabase = FirebaseDatabase.getInstance()
        referencia = basedatabase.getReferenceFromUrl(
            FirebaseReferences.DB_URL + "/" + FirebaseReferences.DB_NAME + "/" + FirebaseReferences.TABLE_NAME
        )
        btnNuevo = findViewById(R.id.btnNew)
        obtenerContactos()
        btnNuevo.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun obtenerContactos() {
        val contactos = ArrayList<Contacts>()
        val listener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                val contacto = dataSnapshot.getValue(Contacts::class.java)
                contacto?.let {
                    contactos.add(it)
                    val adapter = MyArrayAdapter(context, R.layout.layout_contact, contactos)
                    setListAdapter(adapter)
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        referencia.addChildEventListener(listener)
    }

    inner class MyArrayAdapter(
        context: Context,
        private val textViewRecursoId: Int,
        private val objects: ArrayList<Contacts>
    ) : ArrayAdapter<Contacts>(context, textViewRecursoId, objects) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = convertView ?: layoutInflater.inflate(this.textViewRecursoId, null)
            val lblNombre = view.findViewById<TextView>(R.id.lblNombreContacto)
            val lblTelefono = view.findViewById<TextView>(R.id.lblTelefonoContacto)
            val btnModificar = view.findViewById<Button>(R.id.btnModificar)
            val btnBorrar = view.findViewById<Button>(R.id.btnBorrar)

            if (objects[position].favorite > 0) {
                lblNombre.setTextColor(Color.BLACK)
                lblTelefono.setTextColor(Color.BLACK)
            } else {
                lblNombre.setTextColor(Color.BLACK)
                lblTelefono.setTextColor(Color.BLACK)
            }
            lblNombre.text = objects[position].name
            lblTelefono.text = objects[position].phoneNumber1

            btnBorrar.setOnClickListener {
                mostrarDialogoConfirmacion(objects[position], position)
            }

            btnModificar.setOnClickListener {
                val oBundle = Bundle()
                oBundle.putSerializable("contacto", objects[position])
                val intent = Intent()
                intent.putExtras(oBundle)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }

            return view
        }
    }

    private fun mostrarDialogoConfirmacion(contacto: Contacts, position: Int) {
        val builder = AlertDialog.Builder(this@ListActivity)
        builder.setTitle("Confirmar")
        builder.setMessage("¿Eliminar contacto?")
        builder.setPositiveButton("Sí") { dialog, _ ->
            borrarContacto(contacto.id)
            (listAdapter as MyArrayAdapter).remove(contacto)
            Toast.makeText(context, "Contacto eliminado", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        val alert = builder.create()
        alert.show()
    }

    private fun borrarContacto(childIndex: String) {
        referencia.child(childIndex).removeValue()
    }
}

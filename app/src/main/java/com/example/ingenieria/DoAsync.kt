package com.example.ingenieria
import android.os.AsyncTask

/*
Se utilizó esta clase para correr procesos en otro hilo dentro de la aplicación, con el fin de no bloquear
la interface de usuario cuando se realizan consultas al backend.
 */
class doAsync(val handler: () -> Unit) : AsyncTask<Void, Void, Void>() {
    override fun doInBackground(vararg params: Void?): Void? {
        handler()
        return null
    }
}
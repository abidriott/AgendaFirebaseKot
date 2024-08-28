package com.afk.agendafirebase.Objects

import java.io.Serializable

data class Contacts(
    var id: String = "",
    var name: String = "",
    var phoneNumber1: String = "",
    var phoneNumber2: String = "",
    var address: String = "",
    var notes: String = "",
    var favorite: Int = 0,
) : Serializable

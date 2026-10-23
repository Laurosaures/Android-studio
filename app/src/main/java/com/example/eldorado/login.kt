package com.example.eldorado

// Inhoud en knoppen van deze pagina.
class login : FigmaScreen() {

    override val page = "login"

    override fun showPage() {
        title("Login")
        text("", 40, 369, 310, 280, border = true)
        val address = input("Address", "address", 57, 387, 278, 60)
        val email = input("Enter E-mail", "email", 57, 479, 278, 60, true)
        button("Login", 122, 567, 149, 50) {
            prefs.edit()
                .putString("address", address.text.toString())
                .putString("email", email.text.toString()).apply()
            navigate(home::class.java)
        }
    }

}

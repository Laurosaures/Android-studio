package com.example.eldorado

// Inhoud en knoppen van deze pagina.
class shopingcart : FigmaScreen() {

    override val page = "cart"

    override fun showPage() {
        title("Shopping cart")
        text("", 43, 338, 309, 226, border = true)
        val count = prefs.getInt("cartAmount", 300).coerceIn(1, maximumAmount)
        // Alleen de toegevoegde game tonen.
        val cartGame = prefs.getInt("cartGame", selectedGame)
        gameRow(cartGame, 56, 355, 280, 56, clickable = false)
        text("GoldHarbor", 56, 424, 280, 36, 20f)
        text("€ ${count * 3} / ${count} M", 56, 474, 280, 40, 24f)
        text("", 43, 572, 309, 116, border = true)
        val address = input("Address", "address", 56, 580, 280, 48)
        val email = input("E-mail", "email", 56, 634, 280, 48, true)
        button("Buy now", 92, 715, 200, 60) {
            prefs.edit()
                .putString("address", address.text.toString())
                .putString("email", email.text.toString()).apply()
            navigate(order::class.java)
        }
    }
}

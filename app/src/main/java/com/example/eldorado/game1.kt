package com.example.eldorado

// Inhoud en knoppen van deze pagina.
class game1 : FigmaScreen() {

    override val page = "game"

    override fun showPage() {
        tabs(true)
        text("", 43, 338, 309, 246, border = true)
        text("Price € 3/M", 64, 340, 266, 30, 24f)
        val quantity = text("${amount}.M", 149, 372, 99, 70, 30f, true)
        val total = text("Total € ${amount * 3}/${amount}.M", 43, 599, 309, 60, 30f, true)
        fun update(delta: Int) {
            amount = (amount + delta).coerceIn(1, maximumAmount)
            quantity.text = "${amount}.M"
            total.text = "Total € ${amount * 3}/${amount}.M"
        }
        button("−", 64, 372, 69, 70, 40f) {
            update(-1)
        }
        button("+", 262, 372, 69, 70, 40f) {
            update(1)
        }
        text("Maximaal 1.000 M", 64, 479, 265, 38, 20f, true)
        text("GoldHarbor", 64, 534, 265, 40, 30f, true)
        button("Add to cart", 99, 713, 200, 60) {
            prefs.edit()
                .putInt("cartAmount", amount)
                .putInt("cartGame", selectedGame).apply()
            navigate(shopingcart::class.java)
        }
    }

}

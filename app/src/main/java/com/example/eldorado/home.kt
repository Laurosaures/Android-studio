package com.example.eldorado

// Inhoud en knoppen van deze pagina.
class home : FigmaScreen() {

    override val page = "home"

    override fun showPage() {
        tabs()
        for (i in 0..7) {
            gameRow(i, 24, 330 + i * 66, 345, 48)
        }
    }

}

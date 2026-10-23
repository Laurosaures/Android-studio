package com.example.eldorado

class order : FigmaScreen() {

    override val page = "order"

    override fun showPage() {
        title("Thanks for ordering")

        button("Back to home", 98, 426, 200, 50) {
            navigate(home::class.java)
        }
    }
}

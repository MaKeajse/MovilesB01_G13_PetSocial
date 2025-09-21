package com.example.petsocial.ui.main

import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar

class MainActivity : AppCompatActivity() {

    private lateinit var drawer: DrawerLayout
    private lateinit var list: ListView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var contentFrame: FrameLayout
    private val contentId = android.view.View.generateViewId()

    // Menú básico por ahora (todo placeholders)
    data class Item(val title: String, val screen: () -> Fragment)
    private val items = listOf(
        Item("Perfil") { Placeholder("Perfil (placeholder)") },
        Item("Fotos")  { Placeholder("Fotos (placeholder)") },
        Item("Videos") { Placeholder("Videos (placeholder)") },
        Item("Web")    { Placeholder("Web (placeholder)") },
        Item("Subir")  { Placeholder("Subir (placeholder)") }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Drawer raíz
        drawer = DrawerLayout(this)

        // Toolbar superior (para el ícono hamburguesa)
        val toolbar = MaterialToolbar(this).apply { title = "PetSocial" }
        setSupportActionBar(toolbar)

        // Columna: toolbar + contenedor de fragmentos
        val column = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        contentFrame = FrameLayout(this).apply { id = contentId }
        column.addView(toolbar, LinearLayout.LayoutParams.MATCH_PARENT, dp(56))
        column.addView(contentFrame, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)

        // Menú izquierdo: ListView
        list = ListView(this).apply {
            adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_list_item_activated_1,
                items.map { it.title }
            )
            choiceMode = ListView.CHOICE_MODE_SINGLE
            dividerHeight = 0
            layoutParams = DrawerLayout.LayoutParams(
                dp(280),
                DrawerLayout.LayoutParams.MATCH_PARENT,
                Gravity.START
            )
        }

        // Montar jerarquía en el Drawer
        drawer.addView(
            column,
            DrawerLayout.LayoutParams(
                DrawerLayout.LayoutParams.MATCH_PARENT,
                DrawerLayout.LayoutParams.MATCH_PARENT
            )
        )
        drawer.addView(list)
        setContentView(drawer)

        // Toggle del ícono hamburguesa
        toggle = ActionBarDrawerToggle(this, drawer, toolbar, 0, 0)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        // Navegación del ListView
        list.setOnItemClickListener { _, _, pos, _ ->
            open(items[pos].screen())
            list.setItemChecked(pos, true)
            drawer.closeDrawer(Gravity.START)
        }

        // Pantalla inicial
        if (savedInstanceState == null) {
            open(items.first().screen())
            list.setItemChecked(0, true)
        }
    }

    private fun open(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(contentId, fragment)
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (toggle.onOptionsItemSelected(item)) true else super.onOptionsItemSelected(item)

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    // Fragment de texto simple para probar (luego lo cambiamos por el real)
    class Placeholder(private val textValue: String) : Fragment() {
        override fun onCreateView(i: android.view.LayoutInflater, c: android.view.ViewGroup?, s: Bundle?) =
            TextView(requireContext()).apply {
                text = textValue
                textSize = 20f
                setPadding(24, 24, 24, 24)
            }
    }
}

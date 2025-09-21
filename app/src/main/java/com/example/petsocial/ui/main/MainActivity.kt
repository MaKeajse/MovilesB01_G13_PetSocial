package com.example.petsocial.ui.main

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
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

    // Menú básico por ahora (luego cambiamos "Perfil" por el fragment real)
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

        // Toolbar superior (para el icono de hamburguesa)
        val toolbar = MaterialToolbar(this).apply { title = "PetSocial" }
        setSupportActionBar(toolbar)

        // Columna principal: toolbar + contenedor de fragmentos (contenido)
        val column = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        contentFrame = FrameLayout(this).apply { id = contentId }
        column.addView(toolbar, LinearLayout.LayoutParams.MATCH_PARENT, dp(56))
        column.addView(
            contentFrame,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        )

        // ===== Sidebar con fondo naranja y esquinas DERECHAS redondeadas =====
        // Sidebar más angosto y con márgenes (ocupa menos espacio visual)
        val sideContainer = FrameLayout(this).apply {
            layoutParams = DrawerLayout.LayoutParams(
                dp(160), // <-- ANCHO DEL MENÚ (prueba 150–180 hasta que te guste)
                DrawerLayout.LayoutParams.MATCH_PARENT,
                Gravity.START
            ).apply {
                topMargin = dp(72)    // 56dp de la toolbar + 16dp extra
                bottomMargin = dp(24) // margen inferior como el mockup
            }

            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(Color.parseColor("#FFA45B"))
                cornerRadii = floatArrayOf(
                    rdp(16f), rdp(16f), // top-left (ligeramente redondo)
                    rdp(24f), rdp(24f), // top-right
                    rdp(24f), rdp(24f), // bottom-right
                    rdp(16f), rdp(16f)  // bottom-left (ligeramente redondo)
                )
            }
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }


        // ListView del menú (transparente; el color lo pone el contenedor)
        list = ListView(this).apply {
            adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_list_item_activated_1,
                items.map { it.title }
            )
            choiceMode = ListView.CHOICE_MODE_SINGLE
            divider = null
            setBackgroundColor(Color.TRANSPARENT)
        }

        // Inserta la lista dentro del contenedor redondeado
        sideContainer.addView(
            list,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // Monta la jerarquía en el Drawer
        drawer.addView(
            column,
            DrawerLayout.LayoutParams(
                DrawerLayout.LayoutParams.MATCH_PARENT,
                DrawerLayout.LayoutParams.MATCH_PARENT
            )
        )
        drawer.addView(sideContainer)
        setContentView(drawer)

        // Toggle del icono hamburguesa
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
        supportFragmentManager
            .beginTransaction()
            .replace(contentId, fragment)
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (toggle.onOptionsItemSelected(item)) true else super.onOptionsItemSelected(item)

    // Helpers
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun rdp(v: Float) = v * resources.displayMetrics.density

    // Placeholder simple para ver el cambio de pantallas
    class Placeholder(private val textValue: String) : Fragment() {
        override fun onCreateView(
            i: android.view.LayoutInflater,
            c: android.view.ViewGroup?,
            s: Bundle?
        ) = TextView(requireContext()).apply {
            text = textValue
            textSize = 20f
            setPadding(24, 24, 24, 24)
        }
    }
}


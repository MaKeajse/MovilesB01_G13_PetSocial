package com.example.petsocial.ui.main

import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import java.io.InputStream
import androidx.core.view.GravityCompat


class MainActivity : AppCompatActivity() {

    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var contentFrame: FrameLayout
    private lateinit var list: ListView
    private val contentId = View.generateViewId()

    // Paleta
    private val ORANGE = Color.parseColor("#FFA45B")
    private val BG_SCRIM = Color.parseColor("#1A000000") // 10% negro
    private val WHITE = Color.WHITE

    // Modelo del menú (icono por asset o emoji de respaldo)
    data class MenuItemM(
        val title: String,
        val assetIcon: String? = null,
        val emoji: String = "🐾",
        val screen: () -> Fragment
    )

    private val items = listOf(
        MenuItemM("Perfil", "icons/perfil.png", "👤") { Placeholder("Perfil (placeholder)") },
        MenuItemM("Fotos", "icons/fotos.png", "🖼️") { Placeholder("Fotos (placeholder)") },
        MenuItemM("Videos", "icons/videos.png", "▶️") { Placeholder("Videos (placeholder)") },
        MenuItemM("Web",   "icons/web.png",   "🌐") { Placeholder("Web (placeholder)") },
        MenuItemM("Subir", "icons/subir.png", "⬆️") { Placeholder("Subir (placeholder)") }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Drawer raíz
        drawer = DrawerLayout(this)

        // Toolbar fija con título centrado, naranja y en negrilla
        val toolbar = MaterialToolbar(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            setTitleTextColor(ORANGE)
            setTitleCentered(true)          // centra el título (Material 1.12+)
        }

        // poner título en negrilla con Spannable
        val boldTitle = android.text.SpannableString("Petsocial").apply {
            setSpan(
                android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                0, length, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        toolbar.title = boldTitle

        setSupportActionBar(toolbar)



        // Columna: toolbar + content
        val column = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        contentFrame = FrameLayout(this).apply { id = contentId }
        column.addView(toolbar, LinearLayout.LayoutParams.MATCH_PARENT, dp(56))
        column.addView(contentFrame, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)

        // Sidebar angosto con bordes derech. redondeados
        val sideContainer = FrameLayout(this).apply {
            layoutParams = DrawerLayout.LayoutParams(
                dp(160),                              // ancho del menú (ajústalo si quieres)
                DrawerLayout.LayoutParams.MATCH_PARENT,
                Gravity.START
            )

            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(ORANGE)                      // tu color naranja
                // Orden: TLx, TLy, TRx, TRy, BRx, BRy, BLx, BLy
                cornerRadii = floatArrayOf(
                    0f, 0f,               // top-left  -> PLANO
                    rdp(24f), rdp(24f),   // top-right -> REDONDO
                    rdp(24f), rdp(24f),   // bottom-right -> REDONDO
                    0f, 0f                // bottom-left -> PLANO
                )
            }
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }



        // ListView con adaptador personalizado (icono + texto + selección)
        list = ListView(this).apply {
            divider = null
            setBackgroundColor(Color.TRANSPARENT)
            adapter = MenuAdapter(items)
            setPadding(dp(6), dp(6), dp(6), dp(6))
        }
        sideContainer.addView(list, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))

        // Montaje
        drawer.addView(column, DrawerLayout.LayoutParams(DrawerLayout.LayoutParams.MATCH_PARENT, DrawerLayout.LayoutParams.MATCH_PARENT))
        drawer.addView(sideContainer)
        drawer.setScrimColor(BG_SCRIM) // oscurecer un poco el fondo al abrir
        setContentView(drawer)



        // Toggle hamburguesa
        toggle = ActionBarDrawerToggle(this, drawer, toolbar, 0, 0)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        // Click de navegación
        list.setOnItemClickListener { _, _, pos, _ ->
            (list.adapter as MenuAdapter).selected = pos
            (list.adapter as MenuAdapter).notifyDataSetChanged()
            open(items[pos].screen())
            drawer.closeDrawer(GravityCompat.START)

        }

        // Pantalla inicial
        if (savedInstanceState == null) {
            open(items.first().screen())
            (list.adapter as MenuAdapter).selected = 0
            (list.adapter as MenuAdapter).notifyDataSetChanged()
        }
    }

    private fun open(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(contentId, fragment).commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (toggle.onOptionsItemSelected(item)) true else super.onOptionsItemSelected(item)

    // ---------- Adaptador personalizado ----------
    inner class MenuAdapter(private val data: List<MenuItemM>) : BaseAdapter() {
        var selected = 0
        override fun getCount() = data.size
        override fun getItem(p: Int) = data[p]
        override fun getItemId(p: Int) = p.toLong()

        override fun getView(p: Int, convertView: View?, parent: ViewGroup?): View {
            val row = (convertView as? LinearLayout) ?: LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT)
                setPadding(dp(10), dp(12), dp(10), dp(12))
                minimumHeight = dp(44)

                val icon = ImageView(context).apply {
                    id = View.generateViewId()
                    layoutParams = LinearLayout.LayoutParams(dp(20), dp(20))
                }
                val label = TextView(context).apply {
                    id = View.generateViewId()
                    textSize = 16f
                    setPadding(dp(12), 0, 0, 0)
                }
                addView(icon); addView(label)
                tag = Holder(icon, label)
            }

            val h = row.tag as Holder
            val item = data[p]

            // Cargar icono desde assets o usar emoji
            if (item.assetIcon != null) {
                try {
                    val isx: InputStream = assets.open(item.assetIcon)
                    h.icon.setImageBitmap(BitmapFactory.decodeStream(isx))
                } catch (_: Exception) {
                    h.icon.setImageDrawable(textAsBitmapDrawable(item.emoji, 18f, WHITE))
                }
            } else {
                h.icon.setImageDrawable(textAsBitmapDrawable(item.emoji, 18f, WHITE))
            }

            // Estilos por selección
            if (p == selected) {
                // Burbuja blanca redondeada (resaltado)
                row.background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(WHITE)
                    cornerRadii = floatArrayOf(rdp(12f), rdp(12f), rdp(12f), rdp(12f), rdp(12f), rdp(12f), rdp(12f), rdp(12f))
                }
                h.label.setTextColor(ORANGE)
                tint(h.icon, ORANGE)
            } else {
                row.background = null
                h.label.setTextColor(WHITE)
                tint(h.icon, WHITE)
            }

            h.label.text = item.title
            return row
        }


    }

    // ---------- Helpers ----------
    private data class Holder(val icon: ImageView, val label: TextView)
    private fun tint(iv: ImageView, color: Int) = iv.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun rdp(v: Float) = v * resources.displayMetrics.density

    private fun textAsBitmapDrawable(text: String, sp: Float, color: Int): android.graphics.drawable.Drawable {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = sp * resources.displayMetrics.scaledDensity
        }
        val r = Rect()
        paint.getTextBounds(text, 0, text.length, r)
        val bmp = Bitmap.createBitmap(r.width() + dp(6), r.height() + dp(6), Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawText(text, dp(3).toFloat(), r.height().toFloat() + dp(2), paint)
        return android.graphics.drawable.BitmapDrawable(resources, bmp)
    }

    // Placeholder por ahora (luego conectamos Perfil real)
    class Placeholder(private val textValue: String) : Fragment() {
        override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
            TextView(requireContext()).apply { text = textValue; textSize = 20f; setPadding(24,24,24,24) }
    }
}



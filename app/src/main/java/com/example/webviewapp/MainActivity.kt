package com.example.webviewapp

import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class MainActivity : AppCompatActivity() {

    // Referencias a las vistas
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvContextMenu: TextView
    private lateinit var btnPopupMenu: com.google.android.material.floatingactionbutton.FloatingActionButton
    private lateinit var toolbar: Toolbar

    // URLs predefinidas
    private val WIKIPEDIA_URL = "https://www.wikipedia.org"
    private val GOOGLE_URL = "https://www.google.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar vistas
        initViews()

        // Configurar Toolbar
        setupToolbar()

        // Configurar WebView
        setupWebView()

        // Configurar menú contextual
        setupContextMenu()

        // Configurar menú popup
        setupPopupMenu()

        // Cargar página inicial
        webView.loadUrl(WIKIPEDIA_URL)
    }

    /**
     * Inicializa todas las referencias a las vistas
     */
    private fun initViews() {
        webView = findViewById(R.id.webview)
        progressBar = findViewById(R.id.progress_bar)
        tvContextMenu = findViewById(R.id.tv_context_menu)
        btnPopupMenu = findViewById(R.id.btn_popup_menu)
        toolbar = findViewById(R.id.toolbar)
    }

    /**
     * Configura el Toolbar personalizado
     */
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "WebView App"
    }

    /**
     * Configura el WebView con todas sus opciones
     */
    private fun setupWebView() {
        // Configuraciones básicas del WebView
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
        }

        // WebViewClient para evitar abrir navegador externo
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                // Mantener la navegación dentro del WebView
                view?.loadUrl(url ?: "")
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                // Mostrar barra de progreso al iniciar carga
                progressBar.visibility = View.VISIBLE
                progressBar.progress = 0
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Ocultar barra de progreso al terminar carga
                progressBar.visibility = View.GONE
            }
        }

        // WebChromeClient para manejo del progreso
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                // Actualizar progreso de la barra
                progressBar.progress = newProgress

                if (newProgress == 100) {
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    /**
     * Configura el menú contextual para el TextView
     */
    private fun setupContextMenu() {
        // Registrar el TextView para el menú contextual
        registerForContextMenu(tvContextMenu)
    }

    /**
     * Configura el botón del menú popup
     */
    private fun setupPopupMenu() {
        btnPopupMenu.setOnClickListener { view ->
            showPopupMenu(view)
        }
    }

    /**
     * Crea y configura el Options Menu (menú de tres puntos)
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    /**
     * Maneja las selecciones del Options Menu
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_reload -> {
                // Recargar página actual
                webView.reload()
                Toast.makeText(this, "Recargando página...", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.menu_go_google -> {
                // Navegar a Google
                webView.loadUrl(GOOGLE_URL)
                Toast.makeText(this, "Navegando a Google...", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.menu_close_app -> {
                // Cerrar aplicación
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Crea el menú contextual para el TextView
     */
    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)

        if (v?.id == R.id.tv_context_menu) {
            menuInflater.inflate(R.menu.context_menu, menu)
            menu?.setHeaderTitle("Opciones de texto")
        }
    }

    /**
     * Maneja las selecciones del menú contextual
     */
    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.context_change_text -> {
                // Cambiar texto del TextView
                val newText = if (tvContextMenu.text == "Mantén presionado aquí") {
                    "¡Texto cambiado!"
                } else {
                    "Mantén presionado aquí"
                }
                tvContextMenu.text = newText
                Toast.makeText(this, "Texto cambiado", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.context_show_toast -> {
                // Mostrar Toast
                Toast.makeText(this, "¡Hola! Este es un mensaje desde el menú contextual", Toast.LENGTH_LONG).show()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    /**
     * Muestra el PopupMenu con opciones para el WebView
     */
    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)

        // Aplicar estilo personalizado al PopupMenu
        try {
            val popup = PopupMenu::class.java.getDeclaredField("mPopup")
            popup.isAccessible = true
            val menuPopupHelper = popup.get(popupMenu)
            val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
            val setForceIcons = classPopupHelper.getMethod("setForceShowIcon", Boolean::class.java)
            setForceIcons.invoke(menuPopupHelper, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Configurar listener para las opciones del popup
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.popup_hide_webview -> {
                    // Ocultar WebView con animación
                    webView.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction {
                            webView.visibility = View.GONE
                            webView.alpha = 1f
                        }
                    Toast.makeText(this, "🙈 WebView ocultado", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.popup_show_webview -> {
                    // Mostrar WebView con animación
                    webView.visibility = View.VISIBLE
                    webView.alpha = 0f
                    webView.animate()
                        .alpha(1f)
                        .setDuration(300)
                    Toast.makeText(this, "👁️ WebView mostrado", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.popup_navigation_back -> {
                    // Navegar hacia atrás en WebView
                    if (webView.canGoBack()) {
                        webView.goBack()
                        Toast.makeText(this, "⬅️ Navegando hacia atrás", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "❌ No hay páginas anteriores", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.popup_navigation_forward -> {
                    // Navegar hacia adelante en WebView
                    if (webView.canGoForward()) {
                        webView.goForward()
                        Toast.makeText(this, "➡️ Navegando hacia adelante", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "❌ No hay páginas siguientes", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                else -> false
            }
        }

        // Mostrar el popup
        popupMenu.show()
    }

    /**
     * Maneja el botón "Atrás" para navegación en WebView
     */
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    /**
     * Limpia recursos del WebView al destruir la actividad
     */
    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
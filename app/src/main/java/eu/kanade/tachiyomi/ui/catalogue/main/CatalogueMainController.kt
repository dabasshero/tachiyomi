package eu.kanade.tachiyomi.ui.catalogue.main

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.*
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.source.online.LoginSource
import eu.kanade.tachiyomi.ui.base.controller.NucleusController
import eu.kanade.tachiyomi.ui.catalogue.CatalogueController
import eu.kanade.tachiyomi.ui.catalogue.global_search.CatalogueSearchController
import eu.kanade.tachiyomi.ui.catalogue.main.card.CatalogueMainCardAdapter
import eu.kanade.tachiyomi.ui.catalogue.main.card.CatalogueMainCardItem
import eu.kanade.tachiyomi.ui.latest_updates.LatestUpdatesController
import eu.kanade.tachiyomi.ui.setting.SettingsSourcesController
import eu.kanade.tachiyomi.widget.preference.SourceLoginDialog
import kotlinx.android.synthetic.main.catalogue_main_controller.view.*

/**
 * This controller shows and manages the different catalogues enabled by the user.
 * This controller should only handle UI actions, IO actions should be done by [CatalogueMainPresenter]
 * [SourceLoginDialog.Listener] refreshes the adapter on successful login of catalogues.
 */
class CatalogueMainController : NucleusController<CatalogueMainPresenter>(),
        SourceLoginDialog.Listener,
        CatalogueMainCardAdapter.OnBrowseClickListener,
        CatalogueMainCardAdapter.OnLatestClickListener {
    /**
     * Adapter containing sources.
     */
    private var adapter = CatalogueMainAdapter(this)

    /**
     * Boolean which is true when recent catalog is added to adapter.
     */
    var isAdded = false

    /**
     * Most recently used catalogues.
     */
    var recentCatalogue: CatalogueMainItem? = null

    /**
     * Called when controller is initialized.
     */
    init {
        // Enable the option menu
        setHasOptionsMenu(true)
    }

    /**
     * Initiate the view with [R.layout.catalogue_main_controller].
     *
     * @param inflater used to load the layout xml.
     * @param container containing parent views.
     */
    override fun inflateView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.catalogue_main_controller, container, false)
    }

    /**
     * Set  the title of controller
     */
    override fun getTitle(): String? {
        return applicationContext?.getString(R.string.label_catalogues)
    }

    /**
     * Create the [CatalogueMainPresenter] used in controller.
     */
    override fun createPresenter(): CatalogueMainPresenter {
        return CatalogueMainPresenter()
    }

    /**
     * Called when login dialog is closed, refreshes the adapter
     */
    override fun loginDialogClosed(source: LoginSource) {
        adapter.clear()
        isAdded = false
        presenter.loadSources()
        presenter.loadRecentSources()
    }

    override fun OnBrowseClickListener(item: CatalogueMainCardItem) {
        val source = item.source
        if (source is LoginSource && !source.isLogged()) {
            val dialog = SourceLoginDialog(source)
            dialog.targetController = this@CatalogueMainController
            dialog.showDialog(router)
        } else {
            // Update last used
            presenter.prefs.lastUsedCatalogueSource().set(item.source.id)
            if (isAdded)
                presenter.loadRecentSources()
            // Open the catalogue view.
            router.pushController(RouterTransaction.with(CatalogueController(null, item.source))
                    .pushChangeHandler(FadeChangeHandler())
                    .popChangeHandler(FadeChangeHandler()))
        }
    }

    override fun OnLatestClickListener(item: CatalogueMainCardItem) {
        router.pushController((RouterTransaction.with(LatestUpdatesController(null, item.source)))
                .popChangeHandler(FadeChangeHandler())
                .pushChangeHandler(FadeChangeHandler()))
    }

    /**
     * Adds option items to the options menu.
     *
     * @param menu menu containing options.
     * @param inflater used to load the menu xml.
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.catalogue_main, menu)

        // Initialize search option.
        menu.findItem(R.id.action_search).apply {
            val searchView = actionView as SearchView

            // Change hint to show global search.
            searchView.queryHint = applicationContext?.getString(R.string.action_global_search_hint)

            // Create query listener which opens the global search view.s
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    router.pushController((RouterTransaction.with(CatalogueSearchController(query)))
                            .popChangeHandler(FadeChangeHandler())
                            .pushChangeHandler(FadeChangeHandler()))
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    // TODO suggestions?
                    return true
                }
            })
        }
    }

    /**
     * Called when an option menu item has been selected by the user.

     * @param item The selected item.
     * *
     * @return True if this event has been consumed, false if it has not.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
        // Initialize option to open catalogue settings.
            R.id.action_settings -> {
                router.pushController((RouterTransaction.with(SettingsSourcesController()))
                        .popChangeHandler(FadeChangeHandler())
                        .pushChangeHandler(FadeChangeHandler()))
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    /**
     * Called when the view is created
     *
     * @param view view of controller
     * @param savedViewState information from previous state.
     */
    override fun onViewCreated(view: View, savedViewState: Bundle?) {
        super.onViewCreated(view, savedViewState)
        with(view) {
            // Create recycler and set adapter.
            recycler.layoutManager = LinearLayoutManager(context)
            recycler.setHasFixedSize(true)
            recycler.adapter = adapter
        }
    }

    /**
     * Called to update adapter containing sources.
     */
    fun setSources(sources: List<CatalogueMainItem>) {
        adapter.updateDataSet(sources)
    }

    /**
     * Called to update most recently uses source.
     */
    fun setLastUsedSource(source: CatalogueMainItem) {
        // Update most recent catalogue
        recentCatalogue = source
        recentCatalogue?.let {
            // If not yet added, add it to the adapter.
            if (!isAdded) {
                adapter.addItem(0, it)
                isAdded = true
            } else {
                // Update most recent catalogue in adapter.
                adapter.updateItem(0, it, null)
            }
        }

    }
}
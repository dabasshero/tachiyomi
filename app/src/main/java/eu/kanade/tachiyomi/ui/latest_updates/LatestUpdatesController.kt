package eu.kanade.tachiyomi.ui.latest_updates

import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.view.Menu
import android.view.ViewGroup
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.source.CatalogueSource
import eu.kanade.tachiyomi.ui.catalogue.CatalogueController
import eu.kanade.tachiyomi.ui.catalogue.CataloguePresenter

/**
 * Fragment that shows the manga from the catalogue. Inherit CatalogueFragment.
 */
class LatestUpdatesController(bundle: Bundle? = null, source: CatalogueSource? = null) : CatalogueController(bundle,source) {

    override fun createPresenter(): CataloguePresenter {
        return LatestUpdatesPresenter()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_search).isVisible = false
        menu.findItem(R.id.action_set_filter).isVisible = false
    }

    override fun createSecondaryDrawer(drawer: DrawerLayout): ViewGroup? {
        return null
    }

    override fun cleanupSecondaryDrawer(drawer: DrawerLayout) {

    }

}
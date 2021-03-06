package me.edujtm.tuyo

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import me.edujtm.tuyo.auth.AuthManager
import me.edujtm.tuyo.auth.GoogleAccount
import me.edujtm.tuyo.common.*
import me.edujtm.tuyo.data.model.PrimaryPlaylist
import me.edujtm.tuyo.databinding.ActivityMainBinding
import me.edujtm.tuyo.di.components.ActivityComponentProvider
import me.edujtm.tuyo.di.components.MainActivityComponent
import me.edujtm.tuyo.ui.login.LoginActivity
import javax.inject.Inject

class MainActivity : AppCompatActivity(), ActivityComponentProvider {

    @Inject lateinit var authManager: AuthManager

    private lateinit var appBarConfiguration: AppBarConfiguration

    override val activityInjector: MainActivityComponent by lazy {
        injector.mainActivityInjector
            .create(intent.getStringExtra(USER_EMAIL)!!)
    }

    private val mainViewModel : MainViewModel by viewModel { activityInjector.mainViewModel }
    private val ui : ActivityMainBinding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        activityInjector.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(ui.root)

        // --- UI setup ---

        val mainLayout = ui.mainLayout
        setSupportActionBar(mainLayout.toolbar)

        mainLayout.fab.setOnClickListener {
            val email = intent?.getStringExtra(USER_EMAIL) ?: "No Email"
            Snackbar.make(mainLayout.root, email, Snackbar.LENGTH_SHORT).show()
        }

        authManager.getUserAccount()?.let { account ->
            setupNavigationHeader(account)
        }

        // --- navigation setup ---

        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.navigation_home,
            R.id.navigation_playlist,
            R.id.navigation_search
        ), ui.drawerLayout)

        val navController = findNavController(R.id.nav_host_fragment)
        setupActionBarWithNavController(navController, appBarConfiguration)
        ui.navView.setupWithNavController(navController)
        ui.navView.setNavigationItemSelectedListener { menuItem ->
            ui.drawerLayout.closeDrawers()
            menuItem.isChecked = true
            when (menuItem.itemId) {
                R.id.navigation_liked_videos -> {
                    val direction = MainNavigationDirections.actionViewPlaylist(
                        primaryPlaylist = PrimaryPlaylist.LIKED_VIDEOS
                    )
                    navController.popBackStack(R.id.navigation_home, false)
                    navController.navigate(direction)
                }
                R.id.navigation_favorites -> {
                    val direction = MainNavigationDirections.actionViewPlaylist(
                        primaryPlaylist = PrimaryPlaylist.FAVORITES
                    )
                    navController.popBackStack(R.id.navigation_home, false)
                    navController.navigate(direction)
                }
                else -> navController.navigate(menuItem.itemId)
            }
            true
        }

        // --- listeners ---

        lifecycleScope.launch {
            mainViewModel.events.consumeEach { event ->
                when (event) {
                    // Allows fragments to query for google APIs' status
                    is MainViewModel.Event.CheckGooglePlayServices -> {
                        checkGoogleApiAvailability()
                    }
                }
            }
        }

        checkGoogleApiAvailability()
    }

    override fun onStart() {
        super.onStart()
        verifyUserLoggedIn()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_API_SERVICES) {
            checkGoogleApiAvailability()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                authManager.signOut {
                    startActivity<LoginActivity> { intent ->
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    }
                    finish()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun checkGoogleApiAvailability() {
        val status = GoogleApi.getAvailabilityStatus(this)
        when (status) {
            is GoogleApi.StatusResult.UserResolvableError -> showGoogleErrorDialog(status.resultCode)
            is GoogleApi.StatusResult.NotResolvableError -> {
                Snackbar.make(ui.mainLayout.root, "Google API services are not available", Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun showGoogleErrorDialog(resultCode: Int) {
        val dialog = GoogleApi.getErrorDialog(this, resultCode, REQUEST_API_SERVICES)
        dialog.show()
    }

    private fun verifyUserLoggedIn() {
        val account = authManager.getUserAccount()
        if (account == null) {
            startActivity<LoginActivity> { intent ->
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            finish()
        }
    }

    private fun setupNavigationHeader(account: GoogleAccount) {
        val header = ui.navView.getHeaderView(0)

        val userImageView = header.findViewById<ImageView>(R.id.drawer_user_image_iv)
        val usernameView = header.findViewById<TextView>(R.id.drawer_user_name_tv)
        val emailView = header.findViewById<TextView>(R.id.drawer_user_email_tv)

        emailView.text = account.email
        usernameView.text = account.displayName
        Glide.with(this)
            .load(account.photoUrl)
            .placeholder(R.mipmap.ic_launcher_round)
            .into(userImageView)
    }

    companion object {
        const val USER_EMAIL = "user_email"
        const val REQUEST_API_SERVICES = 2000
    }
}
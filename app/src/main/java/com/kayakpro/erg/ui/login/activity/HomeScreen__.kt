/*
package com.kayakpro.erg.ui.login.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.kayakpro.erg.R
import com.kayakpro.erg.databinding.ActivityHomeScreenBinding
import com.kayakpro.erg.drawer.AdvanceDrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeScreen__ : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomeScreenBinding
    private lateinit var bottomNavigation: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.appBarMain.toolbar)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        val drawerLayout: AdvanceDrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home
                ,R.id.nav_pt, R.id.nav_history, R.id.nav_provideos,
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        bottomNavigation.setupWithNavController(navController)

        drawerLayout.setViewScale(Gravity.START, 0.9f)
        drawerLayout.setViewElevation(Gravity.START, 0f)
        drawerLayout.setRadius(Gravity.START, 80f)




        navController.addOnDestinationChangedListener { navController: NavController, navDestination: NavDestination, bundle: Bundle? ->
            val id = navDestination.id
            visibleBottomNavigation(id)
        }
    }
    private fun visibleBottomNavigation(id: Int) {
        if (id == R.id.nav_home){
            binding.appBarMain.appbarmain.targetElevation = 0f
            binding.appBarMain.toolbar.visibility = View.VISIBLE
            bottomNavigation.visibility = View.VISIBLE
           // binding.appBarMain.visitor.visibility = View.VISIBLE
        }

     */
/*   else if (id == R.id.nav_face_authentication
            || id == R.id.nav_markface_attendance
            || id == R.id.nav_profile
            || id == R.id.nav_searchstaff
            || id == R.id.nav_privacy
            || id == R.id.nav_term
        ) {
            binding.appBarMain.visitor.visibility = View.GONE
            binding.appBarMain.toolbar.visibility = View.GONE
            bottomNavigation.visibility = View.GONE

        }
        else if (id == R.id.nav_attandencecalendar
            || id == R.id.id_verification_fragment
            || id == R.id.nav_attendance
            || id == R.id.nav_purposeactivity
            || id == R.id.nav_dailyactivity
            || id == R.id.AadhaarSourceFragment
            || id == R.id.nav_dailytrackactivity
            || id == R.id.nav_language
            || id == R.id.nav_feedback
            || id == R.id.nav_change_pin
            || id == R.id.ResetPinResultDialog
            || id == R.id.nav_visitor_id_verification
            || id == R.id.create_visit
            || id == R.id.newLeaveFragment
            || id == R.id.nav_leave_details
            || id == R.id.nav_attendance_details
            || id == R.id.nav_regularization
            || id == R.id.nav_visitor_req_dialog
            || id == R.id.AddVehicleDialog
            || id == R.id.nav_daterange
            || id == R.id.nav_visitorIDResultDialog
            || id == R.id.nav_ViewVisitRequestFragment
            || id == R.id.VisitApproveResultDialog
            || id == R.id.ReasonRejectionDialog
            || id == R.id.VisitProgressDialog
            || id == R.id.ApplyLeaveResultDialog
            || id == R.id.AttendanceMarkedResultDialog
            || id == R.id.nav_attendance_success
            || id == R.id.ChangeRequestResultDialog
            || id == R.id.ChekinValidateDialog
            || id == R.id.IDValidatedResultDialog
            || id == R.id.RegistrationResultDialog
            || id == R.id.visiter_create_view
            || id == R.id.nav_VisitorMainFragment
            || id == R.id.nav_ViewVisitDetailsFragment
            || id == R.id.nav_AccessPassFragment
            || id == R.id.nav_VisitDetails
            || id == R.id.nav_approval_details
            || id == R.id.CancleLeaveReasionDialog
            || id == R.id.ErrorDialog
            || id == R.id.RegularizationDialog
        ) {
            binding.appBarMain.visitor.visibility = View.GONE
            binding.appBarMain.appbarmain.targetElevation = 5f
            binding.appBarMain.toolbar.visibility = View.VISIBLE
            bottomNavigation.visibility = View.GONE

        } else {
            binding.appBarMain.visitor.visibility = View.GONE
            binding.appBarMain.toolbar.visibility = View.VISIBLE
            bottomNavigation.visibility = View.VISIBLE
            binding.appBarMain.appbarmain.targetElevation = 0f
        }*//*

    }
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}*/

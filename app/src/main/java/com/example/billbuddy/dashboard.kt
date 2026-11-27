package com.example.billbuddy

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class DashboardActivity : AppCompatActivity() {

    private val channelId = "due_bill_reminders"
    private val notificationId = 1001
    private val notifPermissionCode = 201

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        createDueBillNotificationChannel()
        setupClicks()
        highlightBottomNav(isDashboard = true)
    }

    private fun createDueBillNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Due Bill Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for upcoming and overdue utility bills"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun triggerDueBillReminderNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                notifPermissionCode
            )
            return
        }

        val tapIntent = Intent(this, UpcomingBillsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Next Due Bill: IESCO Electricity")
            .setContentText("Rs. 4,500 due Nov 22, 2026 (Due in 3 days)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(this).notify(notificationId, builder.build())
        Toast.makeText(this, "Due-bill reminder notification sent", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_dashboard_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_trigger_reminder -> {
                triggerDueBillReminderNotification()
                true
            }
            R.id.action_refresh -> {
                Toast.makeText(this, "Dashboard refreshed", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_settings -> {
                Toast.makeText(this, "Opening Settings", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_about -> {
                Toast.makeText(this, "Bill Overview · Lecture 11 & 12 Demo", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupClicks() {
        findViewById<View>(R.id.btnNotifyBell).setOnClickListener {
            triggerDueBillReminderNotification()
        }

        val openUpcoming = View.OnClickListener {
            startActivity(Intent(this, UpcomingBillsActivity::class.java))
        }

        findViewById<View>(R.id.tvReviewPending).setOnClickListener(openUpcoming)
        findViewById<View>(R.id.qaViewUpcoming).setOnClickListener(openUpcoming)
        findViewById<View>(R.id.tvViewAllUpcoming).setOnClickListener(openUpcoming)
        findViewById<View>(R.id.navUpcoming).setOnClickListener(openUpcoming)

        findViewById<View>(R.id.qaAddBill).setOnClickListener {
            Toast.makeText(this, "Add New Bill clicked", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.qaScanBill).setOnClickListener {
            Toast.makeText(this, "Scan Bill clicked", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.tvViewDetails).setOnClickListener {
            Toast.makeText(this, "IESCO Electricity · Rs. 4,500", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.fabAddBill).setOnClickListener {
            Toast.makeText(this, "Quick Add Bill clicked", Toast.LENGTH_SHORT).show()
        }
    }

    fun highlightBottomNav(isDashboard: Boolean) {
        val tvDash = findViewById<TextView>(R.id.tvNavDashboard)
        val tvUpc = findViewById<TextView>(R.id.tvNavUpcoming)
        tvDash.setTextColor(getColor(if (isDashboard) R.color.navy_primary else R.color.navy_muted))
        tvUpc.setTextColor(getColor(if (!isDashboard) R.color.navy_primary else R.color.navy_muted))
    }
}
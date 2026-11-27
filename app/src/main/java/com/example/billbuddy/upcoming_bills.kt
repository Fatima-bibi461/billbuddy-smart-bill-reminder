package com.example.billbuddy

import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

data class BillItem(
    val id: Int,
    val title: String,
    val categoryAndDate: String,
    val amount: String,
    var status: String // "Upcoming", "Due Soon", "Overdue", "Paid"
)

class UpcomingBillsActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var adapter: BillAdapter
    private val allBills = mutableListOf<BillItem>()
    private val displayedBills = mutableListOf<BillItem>()
    private var activeFilter = "All"

    private lateinit var tvCountTotal: TextView
    private lateinit var tvCountPending: TextView
    private lateinit var tvCountOverdue: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upcoming_bills)

        tvCountTotal = findViewById(R.id.tvCountTotal)
        tvCountPending = findViewById(R.id.tvCountPending)
        tvCountOverdue = findViewById(R.id.tvCountOverdue)
        listView = findViewById(R.id.listViewBills)

        seedSampleBills()
        adapter = BillAdapter()
        listView.adapter = adapter

        // Attach long-press Context Menu to ListView items
        registerForContextMenu(listView)

        setupFilters()
        setupBottomNav()
        applyFilter("All")
    }

    private fun seedSampleBills() {
        allBills.clear()
        allBills.addAll(
            listOf(
                BillItem(1, "IESCO", "Electricity · Nov 22, 2026", "Rs. 4,500", "Due Soon"),
                BillItem(2, "SNGPL", "Gas · Nov 18, 2026", "Rs. 2,200", "Overdue"),
                BillItem(3, "Internet Provider", "Internet · Nov 28, 2026", "Rs. 3,000", "Upcoming"),
                BillItem(4, "Water Bill", "Water · Nov 15, 2026", "Rs. 850", "Overdue"),
                BillItem(5, "Postpaid Bill", "Mobile · Nov 24, 2026", "Rs. 1,200", "Due Soon"),
                BillItem(6, "Monthly Rent", "Rent · Nov 05, 2026", "Rs. 15,000", "Paid")
            )
        )
    }

    private fun setupFilters() {
        val filterViews = mapOf(
            "All" to findViewById<TextView>(R.id.filterAll),
            "Upcoming" to findViewById<TextView>(R.id.filterUpcoming),
            "Due Soon" to findViewById<TextView>(R.id.filterDueSoon),
            "Overdue" to findViewById<TextView>(R.id.filterOverdue)
        )

        filterViews.forEach { (label, view) ->
            view.setOnClickListener {
                activeFilter = label
                filterViews.forEach { (key, tv) ->
                    val selected = key == label
                    tv.setBackgroundResource(
                        if (selected) R.drawable.bg_filter_selected else R.drawable.bg_filter_unselected
                    )
                    tv.setTextColor(
                        getColor(if (selected) R.color.white else R.color.navy_muted)
                    )
                }
                applyFilter(label)
            }
        }
    }

    private fun applyFilter(filter: String) {
        displayedBills.clear()
        when (filter) {
            "All" -> displayedBills.addAll(allBills)
            else -> displayedBills.addAll(allBills.filter { it.status == filter })
        }
        updateCounters()
        adapter.notifyDataSetChanged()
    }

    private fun updateCounters() {
        tvCountTotal.text = allBills.size.toString()
        val pendingCount = allBills.count { it.status != "Paid" }
        val overdueCount = allBills.count { it.status == "Overdue" }
        tvCountPending.text = pendingCount.toString()
        tvCountOverdue.text = overdueCount.toString()
    }

    // Long-press Context Menu
    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        if (v.id == R.id.listViewBills) {
            val info = menuInfo as? AdapterView.AdapterContextMenuInfo
            val bill = info?.position?.let { displayedBills.getOrNull(it) }
            menu.setHeaderTitle(bill?.title ?: "Bill Actions")
            menuInflater.inflate(R.menu.menu_bill_item_actions, menu)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as? AdapterView.AdapterContextMenuInfo ?: return super.onContextItemSelected(item)
        val bill = displayedBills.getOrNull(info.position) ?: return super.onContextItemSelected(item)
        return handleBillMenuAction(bill, item.itemId) || super.onContextItemSelected(item)
    }

    // Three-dot PopupMenu
    private fun showBillPopupMenu(anchor: View, bill: BillItem) {
        val popup = PopupMenu(this, anchor)
        popup.menuInflater.inflate(R.menu.menu_bill_item_actions, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            handleBillMenuAction(bill, menuItem.itemId)
        }
        popup.show()
    }

    private fun handleBillMenuAction(bill: BillItem, actionId: Int): Boolean {
        return when (actionId) {
            R.id.bill_action_edit -> {
                Toast.makeText(this, "Edit ${bill.title}", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.bill_action_mark_paid -> {
                bill.status = "Paid"
                applyFilter(activeFilter)
                Toast.makeText(this, "${bill.title} marked as Paid", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.bill_action_share -> {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Bill: ${bill.title} (${bill.amount}) - ${bill.categoryAndDate}")
                }
                startActivity(Intent.createChooser(shareIntent, "Share Bill"))
                true
            }
            R.id.bill_action_delete -> {
                allBills.remove(bill)
                applyFilter(activeFilter)
                Toast.makeText(this, "${bill.title} deleted", Toast.LENGTH_SHORT).show()
                true
            }
            else -> false
        }
    }

    private fun setupBottomNav() {
        findViewById<TextView>(R.id.tvNavDashboard).setTextColor(getColor(R.color.navy_muted))
        findViewById<TextView>(R.id.tvNavUpcoming).setTextColor(getColor(R.color.navy_primary))

        findViewById<View>(R.id.navDashboard).setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
        findViewById<View>(R.id.fabAddBill).setOnClickListener {
            Toast.makeText(this, "Add Bill clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private inner class BillAdapter : BaseAdapter() {
        override fun getCount(): Int = displayedBills.size
        override fun getItem(position: Int): BillItem = displayedBills[position]
        override fun getItemId(position: Int): Long = displayedBills[position].id.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(parent.context)
                .inflate(R.layout.item_bill_row, parent, false)

            val bill = getItem(position)
            val tvTitle = view.findViewById<TextView>(R.id.tvBillTitle)
            val tvSubtitle = view.findViewById<TextView>(R.id.tvBillSubtitle)
            val tvAmount = view.findViewById<TextView>(R.id.tvBillAmount)
            val tvStatus = view.findViewById<TextView>(R.id.tvBillStatus)
            val btnMore = view.findViewById<ImageButton>(R.id.btnMoreActions)

            tvTitle.text = bill.title
            tvSubtitle.text = bill.categoryAndDate
            tvAmount.text = bill.amount
            tvStatus.text = bill.status

            when (bill.status) {
                "Overdue" -> {
                    tvStatus.setBackgroundResource(R.drawable.bg_pill_red)
                    tvStatus.setTextColor(getColor(R.color.status_red_text))
                }
                "Due Soon" -> {
                    tvStatus.setBackgroundResource(R.drawable.bg_pill_amber)
                    tvStatus.setTextColor(getColor(R.color.status_amber_text))
                }
                "Paid" -> {
                    tvStatus.setBackgroundResource(R.drawable.bg_pill_green)
                    tvStatus.setTextColor(getColor(R.color.status_green_text))
                }
                else -> {
                    tvStatus.setBackgroundResource(R.drawable.bg_rounded_soft_blue)
                    tvStatus.setTextColor(getColor(R.color.navy_primary))
                }
            }

            btnMore.setOnClickListener { anchor ->
                showBillPopupMenu(anchor, bill)
            }

            return view
        }
    }
}
package com.example.opensociety.ui.contacts

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import java.util.*

class SelectDateFragment(dateTextView: TextView) : DialogFragment(),
    DatePickerDialog.OnDateSetListener {
    var dateTextView = dateTextView
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val yy = calendar[Calendar.YEAR]
        val mm = calendar[Calendar.MONTH]
        val dd = calendar[Calendar.DAY_OF_MONTH]
        return  DatePickerDialog(requireContext(), this, yy, mm, dd)
    }

    override fun onDateSet(view: DatePicker, yy: Int, mm: Int, dd: Int) {
        populateSetDate(yy, mm + 1, dd)
    }

    fun populateSetDate(year: Int, month: Int, day: Int) {
        dateTextView.setText("$year-$month-$day")
    }
}
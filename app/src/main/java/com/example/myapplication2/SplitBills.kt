package com.example.myapplication2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication2.databinding.ActivitySplitBillsBinding

class SplitBills : AppCompatActivity() {

    private lateinit var binding: ActivitySplitBillsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplitBillsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSave.setOnClickListener {
            val amount= binding.edamount.text.toString()
            val reason= binding.edreason.text.toString()
            val message= generateText(amount, reason)
            val intent= Intent()
            intent.putExtra("message",message)
            intent.putExtra("amount",amount)
            setResult(Activity.RESULT_OK,intent)
            finish()
        }
    }

    private fun generateText(amount: String, reason: String): String {
        val message = "You spent $amount for $reason."
        return message
    }

}

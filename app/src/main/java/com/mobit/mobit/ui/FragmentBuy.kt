package com.mobit.mobit.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.mobit.mobit.R
import com.mobit.mobit.data.CoinInfo
import com.mobit.mobit.data.Transaction
import com.mobit.mobit.databinding.FragmentBuyBinding
import com.mobit.mobit.viewmodel.MyViewModel
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.floor

/*
코인 매수 기능이 구현될 Fragment 입니다.
*/
class FragmentBuy : Fragment() {

    lateinit var getContent: ActivityResultLauncher<Intent>

    // UI 변수 시작
    lateinit var binding: FragmentBuyBinding
    // UI 변수 끝

    val myViewModel: MyViewModel by activityViewModels()

    val intFormatter = DecimalFormat("###,###").apply {
        this.roundingMode = RoundingMode.DOWN
    }
    val doubleFormatter8 = DecimalFormat("###,###.########").apply {
        this.roundingMode = RoundingMode.DOWN
    }
    val doubleFormatter2 = DecimalFormat("###,###.##").apply {
        this.roundingMode = RoundingMode.DOWN
    }
    var orderCount: Double = 0.0
    var orderPrice: Double = 0.0

    var buyIndex: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBuyBinding.inflate(layoutInflater)
        getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            binding.orderCount.clearFocus()
            when (it.resultCode) {
                Activity.RESULT_OK -> {
                    val code: String = it.data!!.getStringExtra("code")!!
                    val name: String = it.data!!.getStringExtra("name")!!
                    val nowOrderPrice: Double = it.data!!.getDoubleExtra("unitPrice", 0.0)
                    val count: Double = it.data!!.getDoubleExtra("count", 0.0)
                    val price: Double = nowOrderPrice * count
                    val fee: Double = price * 0.0005
                    val time: String = getNowTime()
                    val transaction = Transaction(
                        code,
                        name,
                        time,
                        Transaction.BID,
                        count,
                        nowOrderPrice,
                        price,
                        fee,
                        price + fee
                    )

                    buyIndex = myViewModel.bidCoin(
                        code,
                        name,
                        nowOrderPrice,
                        count
                    )

                    if (buyIndex in 0 until myViewModel.asset.value!!.coins.size) {
                        myViewModel.addTransaction(transaction)

                        val thread: Thread = object : Thread() {
                            override fun run() {
                                myViewModel.myDBHelper!!.setKRW(myViewModel.asset.value!!.krw)
                                myViewModel.myDBHelper!!.insertTransaction(transaction)

                                if (myViewModel.myDBHelper!!.findCoinAsset(code)) {
                                    myViewModel.myDBHelper!!.updateCoinAsset(myViewModel.asset.value!!.coins[buyIndex])
                                } else {
                                    myViewModel.myDBHelper!!.insertCoinAsset(myViewModel.asset.value!!.coins[buyIndex])
                                }
                            }
                        }
                        thread.start()

                        Toast.makeText(context, "매수주문이 정상 처리되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e(
                            "FragmentBuy",
                            "buyIndex is $buyIndex, but coins range is [0, ${myViewModel.asset.value!!.coins.size})."
                        )
                        Toast.makeText(
                            context,
                            "매수주문을 처리하는데 오류가 발생했습니다.\n 다시 시도해 주세요.",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    binding.canOrderPrice.text =
                        getString(
                            R.string.transaction_krw_string,
                            intFormatter.format(myViewModel.asset.value!!.krw)
                        )
                    resetOrderTextView()
                }
                Activity.RESULT_CANCELED -> {
                    Log.i("resultCode", "RESULT_CANCELED")
                }
            }
        }

        init()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.apply {
            canOrderPrice.text =
                getString(
                    R.string.transaction_krw_string,
                    intFormatter.format(myViewModel.asset.value!!.krw)
                )
            orderCount.setText("0")
            orderCountSpinner.setSelection(0)
        }
    }

    fun init() {
        myViewModel.coinInfo.observe(viewLifecycleOwner, Observer {
            for (coinInfo in myViewModel.coinInfo.value!!) {
                if (coinInfo.code == myViewModel.selectedCoin.value!!) {
                    orderPrice = coinInfo.price.realTimePrice
                    break
                }
            }
            binding.orderPrice.text =
                if (orderPrice > 100.0)
                    intFormatter.format(orderPrice)
                else
                    doubleFormatter2.format(orderPrice)
            val totalPrice = orderCount * orderPrice
            binding.orderTotalPrice.text =
                if (totalPrice > 100.0)
                    getString(R.string.transaction_krw_string, intFormatter.format(totalPrice))
                else
                    getString(R.string.transaction_krw_string, doubleFormatter2.format(totalPrice))
        })

        // spinner 아이템을 보여주는 view를 커스텀하기 위해서 adapter를 만들어준다
        val spinnerAdapter = ArrayAdapter<String>(
            requireContext(),
            R.layout.spinner_item,
            resources.getStringArray(R.array.orderCountSpinner)
        )

        binding.apply {
            canOrderPrice.text =
                getString(
                    R.string.transaction_krw_string,
                    intFormatter.format(myViewModel.asset.value!!.krw)
                )
            orderCountSpinner.adapter = spinnerAdapter
            orderCountSpinner.setSelection(0, false)
            orderCountSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val krw = myViewModel.asset.value!!.krw
                    var canOrderCount: Double = 0.0
                    when (position) {
                        0 -> {

                        }
                        // 최대
                        1 -> {
                            canOrderCount = krw / (this@FragmentBuy.orderPrice * 1.0005)
                        }
                        // 50%
                        2 -> {
                            canOrderCount = (krw / 2) / (this@FragmentBuy.orderPrice * 1.0005)
                        }
                        // 25%
                        3 -> {
                            canOrderCount = (krw / 4) / (this@FragmentBuy.orderPrice * 1.0005)
                        }
                        // 10%
                        4 -> {
                            canOrderCount = (krw / 10) / (this@FragmentBuy.orderPrice * 1.0005)
                        }
                        else -> {
                            Log.e("FragmentBuy Spinner", "position is $position")
                        }
                    }
                    canOrderCount = floor(canOrderCount * 1e8) / 1e8
                    orderCount.setText(doubleFormatter8.format(canOrderCount))
                    this@FragmentBuy.orderCount = canOrderCount
                    val totalPrice = canOrderCount * this@FragmentBuy.orderPrice
                    orderTotalPrice.text =
                        if (totalPrice > 100.0)
                            getString(
                                R.string.transaction_krw_string,
                                intFormatter.format(totalPrice)
                            )
                        else
                            getString(
                                R.string.transaction_krw_string,
                                doubleFormatter2.format(totalPrice)
                            )
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            orderCount.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.isNullOrEmpty()) {
                        this@FragmentBuy.orderCount = 0.0
                    } else {
                        try {
                            this@FragmentBuy.orderCount = s.toString().replace(",", "").toDouble()
                            this@FragmentBuy.orderCount =
                                floor(this@FragmentBuy.orderCount * 1e8) / 1e8
                        } catch (e: NumberFormatException) {
                            this@FragmentBuy.orderCount = 0.0
                            orderCount.setText("0")
                            Toast.makeText(
                                context,
                                "지정 가능한 범위가 아닙니다.\n 다시 입력해주세요.",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("FragmentBuy", e.toString())
                        } catch (e: Exception) {
                            Log.e("FragmentBuy", e.toString())
                        }
                        if (this@FragmentBuy.orderCount < 0) {
                            this@FragmentBuy.orderCount = 0.0
                            orderCount.setText("0")
                        }
                    }
                    val totalPrice = this@FragmentBuy.orderCount * this@FragmentBuy.orderPrice
                    orderTotalPrice.text =
                        if (totalPrice > 100.0)
                            getString(
                                R.string.transaction_krw_string,
                                intFormatter.format(totalPrice)
                            )
                        else
                            getString(
                                R.string.transaction_krw_string,
                                doubleFormatter2.format(totalPrice)
                            )
                }

                override fun afterTextChanged(s: Editable?) {}
            })
            orderCount.setOnFocusChangeListener(object : View.OnFocusChangeListener {
                override fun onFocusChange(v: View?, hasFocus: Boolean) {
                    if (v != null) {
                        if (!hasFocus) {
                            val text = orderCount.text.toString()
                            if (text.isNotEmpty()) {
                                var number: Double = 0.0
                                try {
                                    number = text.replace(",", "").toDouble()
                                    number = floor(number * 1e8) / 1e8
                                } catch (e: NumberFormatException) {
                                    number = 0.0
                                    orderCount.setText("0")
                                    Toast.makeText(
                                        context,
                                        "지정 가능한 범위가 아닙니다.\n 다시 입력해주세요.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    Log.e("FragmentBuy", e.toString())
                                } catch (e: Exception) {
                                    Log.e("FragmentBuy", e.toString())
                                }
                                this@FragmentBuy.orderCount = if (number > 0.0) number else 0.0
                            } else {
                                this@FragmentBuy.orderCount = 0.0
                                orderCount.setText("0")
                            }
                        }
                    }
                }
            })
            // 주문 개수와 주문 가격 초기화
            resetBtn.setOnClickListener {
                resetOrderTextView()
            }
            // 코인 매수
            buyBtn.setOnClickListener {
                orderCount.clearFocus()
                val nowOrderPrice = this@FragmentBuy.orderPrice
                val nowOrderCount = this@FragmentBuy.orderCount
                if (nowOrderCount != 0.0 && nowOrderCount * nowOrderPrice >= 5000.0) {
                    var coin: CoinInfo? = null
                    for (coinInfo in myViewModel.coinInfo.value!!) {
                        if (coinInfo.code == myViewModel.selectedCoin.value!!) {
                            coin = coinInfo
                            break
                        }
                    }

                    val flag = myViewModel.asset.value!!.canBidCoin(
                        coin!!.code,
                        coin!!.name,
                        nowOrderPrice,
                        nowOrderCount
                    )
                    if (flag) {
                        val intent: Intent = Intent(context, PopupBuySellActivity::class.java)
                        intent.putExtra("type", 1)
                        intent.putExtra("code", coin!!.code)
                        intent.putExtra("name", coin!!.name)
                        intent.putExtra("unitPrice", nowOrderPrice)
                        intent.putExtra("count", nowOrderCount)
                        getContent.launch(intent)
                    } else {
                        Toast.makeText(context, "주문가능 금액이 부족합니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "주문 가능한 최소 금액은 5,000KRW입니다.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    fun resetOrderTextView() {
        this@FragmentBuy.orderCount = 0.0
        binding.orderCount.setText(intFormatter.format(orderCount))
        binding.orderPrice.text =
            if (orderPrice > 100.0)
                intFormatter.format(orderPrice)
            else
                doubleFormatter2.format(orderPrice)
        binding.orderTotalPrice.text = getString(R.string.transaction_krw_string, "0")
    }

    fun getNowTime(): String {
        val current = LocalDateTime.now()
        // yyyy-MM-ddThh:mm:ss 형태로 날짜를 저장한다.
        // https://developer.android.com/reference/java/time/format/DateTimeFormatter#ISO_LOCAL_DATE_TIME
        val formatted = current.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        return formatted
    }
}
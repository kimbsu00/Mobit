package com.mobit.mobit

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
import com.mobit.mobit.data.CoinInfo
import com.mobit.mobit.data.MyViewModel
import com.mobit.mobit.data.Transaction
import com.mobit.mobit.databinding.FragmentBuyBinding
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/*
코인 매수 기능이 구현될 Fragment 입니다.
*/
class FragmentBuy : Fragment() {

    lateinit var getContent: ActivityResultLauncher<Intent>

    // UI 변수 시작
    lateinit var binding: FragmentBuyBinding
    // UI 변수 끝

    val myViewModel: MyViewModel by activityViewModels()

    val formatter = DecimalFormat("###,###")
    val formatter2 = DecimalFormat("###,###.####")
    val formatter3 = DecimalFormat("###,###.##")
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
                        price - fee,
                        fee,
                        price
                    )

                    buyIndex = myViewModel.bidCoin(
                        code,
                        name,
                        nowOrderPrice,
                        count
                    )

                    myViewModel.addTransaction(transaction)

                    val thread: Thread = object : Thread() {
                        override fun run() {
                            myViewModel.myDBHelper!!.setKRW(myViewModel.asset.value!!.krw)
                            myViewModel.myDBHelper!!.insertTransaction(transaction)

                            if (myViewModel.myDBHelper!!.findCoinAsset(code)) {
                                val ret =
                                    myViewModel.myDBHelper!!.updateCoinAsset(myViewModel.asset.value!!.coins[buyIndex])
                            } else {
                                val ret =
                                    myViewModel.myDBHelper!!.insertCoinAsset(myViewModel.asset.value!!.coins[buyIndex])
                            }
                        }
                    }
                    thread.start()

                    binding.canOrderPrice.text =
                        "${formatter.format(myViewModel.asset.value!!.krw)}KRW"
                    resetOrderTextView()
                    Toast.makeText(context, "매수주문이 정상 처리되었습니다.", Toast.LENGTH_SHORT).show()
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
            canOrderPrice.text = "${formatter.format(myViewModel.asset.value!!.krw)}KRW"
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
                    formatter.format(orderPrice)
                else
                    formatter3.format(orderPrice)
            binding.orderTotalPrice.text = "${formatter.format(orderPrice * orderCount)}KRW"
        })

        // spinner 아이템을 보여주는 view를 커스텀하기 위해서 adapter를 만들어준다
        val spinnerAdapter = ArrayAdapter<String>(
            requireContext(),
            R.layout.spinner_item,
            resources.getStringArray(R.array.orderCountSpinner)
        )

        binding.apply {
            canOrderPrice.text = "${formatter.format(myViewModel.asset.value!!.krw)}KRW"
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
                    orderCount.setText(formatter2.format(canOrderCount))
                    val totalPrice = canOrderCount * this@FragmentBuy.orderPrice
                    orderTotalPrice.text = "${formatter.format(totalPrice)}KRW"
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
                        this@FragmentBuy.orderCount = s.toString().replace(",", "").toDouble()
                        if (this@FragmentBuy.orderCount < 0) {
                            this@FragmentBuy.orderCount = 0.0
                            orderCount.setText("0")
                        }
                    }
                    val totalPrice = this@FragmentBuy.orderCount * this@FragmentBuy.orderPrice
                    orderTotalPrice.text = "${formatter.format(totalPrice)}KRW"
                }

                override fun afterTextChanged(s: Editable?) {}
            })
            orderCount.setOnFocusChangeListener(object : View.OnFocusChangeListener {
                override fun onFocusChange(v: View?, hasFocus: Boolean) {
                    if (v != null) {
                        if (!hasFocus) {
                            val text = orderCount.text.toString()
                            if (text.isNotEmpty()) {
                                val number = text.replace(",", "").toDouble()
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
        binding.orderCount.setText(formatter.format(orderCount))
        binding.orderPrice.text =
            if (orderPrice > 100.0)
                formatter.format(orderPrice)
            else
                formatter3.format(orderPrice)
        binding.orderTotalPrice.text = "0KRW"
    }

    fun getNowTime(): String {
        val current = LocalDateTime.now()
        // yyyy-MM-ddThh:mm:ss 형태로 날짜를 저장한다.
        // https://developer.android.com/reference/java/time/format/DateTimeFormatter#ISO_LOCAL_DATE_TIME
        val formatted = current.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        return formatted
    }
}
package com.mobit.android.ui

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
import com.mobit.android.R
import com.mobit.android.data.CoinAsset
import com.mobit.android.data.CoinInfo
import com.mobit.android.data.Transaction
import com.mobit.android.databinding.FragmentSellBinding
import com.mobit.android.viewmodel.MyViewModel
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.floor

/*
코인 매도 기능이 구현될 Fragment 입니다.
*/
class FragmentSell : Fragment() {

    lateinit var getContent: ActivityResultLauncher<Intent>

    // UI 변수 시작
    lateinit var binding: FragmentSellBinding
    // UI 변수 끝

    val myViewModel: MyViewModel by activityViewModels()

    var selectedCoin: CoinAsset? = null
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

    var coinAsset: CoinAsset? = null

    var listener: OnPopupActivityControl? = null

    interface OnPopupActivityControl {
        fun onPopupActivityShow()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSellBinding.inflate(layoutInflater)
        getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            binding.orderCount.clearFocus()
            when (it.resultCode) {
                Activity.RESULT_OK -> {
                    val code = it.data!!.getStringExtra("code")!!
                    val name = it.data!!.getStringExtra("name")!!
                    val nowOrderPrice: Double = it.data!!.getDoubleExtra("unitPrice", 0.0)
                    val count: Double = it.data!!.getDoubleExtra("count", 0.0)
                    val price: Double = nowOrderPrice * count
                    val fee: Double = price * 0.0005
                    val time: String = getNowTime()
                    val transaction = Transaction(
                        code,
                        name,
                        time,
                        Transaction.ASK,
                        count,
                        nowOrderPrice,
                        price,
                        fee,
                        price - fee
                    )
                    coinAsset = myViewModel.askCoin(
                        code,
                        nowOrderPrice,
                        count
                    )
                    myViewModel.addTransaction(transaction)

                    val thread: Thread = object : Thread() {
                        override fun run() {
                            myViewModel.myDBHelper!!.setKRW(myViewModel.asset.value!!.krw)
                            myViewModel.myDBHelper!!.insertTransaction(transaction)

                            if (myViewModel.asset.value!!.coins.contains(coinAsset!!)) {
                                myViewModel.myDBHelper!!.updateCoinAsset(coinAsset!!)
                            } else {
                                myViewModel.myDBHelper!!.deleteCoinAsset(coinAsset!!)
                            }
                        }
                    }
                    thread.start()

                    binding.canOrderCoin.text = getString(
                        R.string.transaction_coin_string,
                        doubleFormatter8.format(coinAsset!!.number),
                        myViewModel.selectedCoin.value!!.split('-')[1]
                    )
                    resetOrderTextView()
                    Toast.makeText(context, "매도주문이 정상 처리되었습니다.", Toast.LENGTH_SHORT).show()
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
            val totalPrice = orderPrice * orderCount
            binding.orderTotalPrice.text =
                if (totalPrice >= 100.0)
                    getString(R.string.transaction_krw_string, intFormatter.format(totalPrice))
                else
                    getString(R.string.transaction_krw_string, doubleFormatter2.format(totalPrice))
        })
        myViewModel.selectedCoin.observe(viewLifecycleOwner, Observer {
            var check = false
            for (coinAsset in myViewModel.asset.value!!.coins) {
                if (coinAsset.code == it) {
                    check = true
                    selectedCoin = coinAsset
                    binding.canOrderCoin.text = getString(
                        R.string.transaction_coin_string,
                        doubleFormatter8.format(coinAsset.number),
                        coinAsset.code.split('-')[1]
                    )
                    break
                }
            }
            if (!check) {
                binding.canOrderCoin.text =
                    getString(R.string.transaction_coin_string, "0", it.split('-')[1])
            }
        })

        // spinner 아이템을 보여주는 view를 커스텀하기 위해서 adapter를 만들어준다
        val spinnerAdapter = ArrayAdapter<String>(
            requireContext(),
            R.layout.spinner_item,
            resources.getStringArray(R.array.orderCountSpinner)
        )

        binding.apply {
            for (coinAsset in myViewModel.asset.value!!.coins) {
                if (coinAsset.code == myViewModel.selectedCoin.value!!) {
                    selectedCoin = coinAsset
                    canOrderCoin.text = getString(
                        R.string.transaction_coin_string,
                        doubleFormatter8.format(coinAsset.number),
                        coinAsset.code.split('-')[1]
                    )
                    break
                }
            }
            if (selectedCoin == null) {
                canOrderCoin.text = getString(
                    R.string.transaction_coin_string,
                    "0",
                    myViewModel.selectedCoin.value!!.split('-')[1]
                )
            }
            orderCountSpinner.adapter = spinnerAdapter
            orderCountSpinner.setSelection(0, false)
            orderCountSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    var canOrderCount: Double = 0.0
                    // 선택된 코인이 매수한 적 없는 코인인 경우,
                    // selectedCoin은 null이 되므로 조건문을 추가해야 한다.
                    if (selectedCoin != null) {
                        when (position) {
                            0 -> {

                            }
                            // 최대
                            1 -> {
                                canOrderCount = selectedCoin!!.number
                            }
                            // 50%
                            2 -> {
                                canOrderCount = selectedCoin!!.number / 2
                            }
                            // 25%
                            3 -> {
                                canOrderCount = selectedCoin!!.number / 4
                            }
                            // 10%
                            4 -> {
                                canOrderCount = selectedCoin!!.number / 10
                            }
                            else -> {
                                Log.e("FragmentSell Spinner", "position is $position")
                            }
                        }
                    }
                    canOrderCount = floor(canOrderCount * 1e8) / 1e8
                    orderCount.setText(doubleFormatter8.format(canOrderCount))
                    this@FragmentSell.orderCount = canOrderCount
                    val totalPrice = this@FragmentSell.orderCount * this@FragmentSell.orderPrice
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
                        this@FragmentSell.orderCount = 0.0
                    } else {
                        try {
                            this@FragmentSell.orderCount = s.toString().replace(",", "").toDouble()
                        } catch (e: NumberFormatException) {
                            this@FragmentSell.orderCount = 0.0
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
                        if (this@FragmentSell.orderCount < 0) {
                            this@FragmentSell.orderCount = 0.0
                            orderCount.setText("0")
                        }
                    }
                    val totalPrice = this@FragmentSell.orderCount * this@FragmentSell.orderPrice
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
                                this@FragmentSell.orderCount = if (number > 0.0) number else 0.0
                            } else {
                                this@FragmentSell.orderCount = 0.0
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
            // 코인 매도
            sellBtn.setOnClickListener {
                orderCount.clearFocus()
                val nowOrderPrice = this@FragmentSell.orderPrice
                if (this@FragmentSell.orderCount != 0.0 && this@FragmentSell.orderCount * nowOrderPrice >= 5000.0) {
                    var coin: CoinInfo? = null
                    for (coinInfo in myViewModel.coinInfo.value!!) {
                        if (coinInfo.code == myViewModel.selectedCoin.value!!) {
                            coin = coinInfo
                            break
                        }
                    }

                    val flag = myViewModel.asset.value!!.canAskCoin(
                        coin!!.code,
                        nowOrderPrice,
                        this@FragmentSell.orderCount
                    )
                    if (flag) {
                        listener?.onPopupActivityShow()
                        val intent: Intent = Intent(context, PopupBuySellActivity::class.java)
                        intent.putExtra("type", 2)
                        intent.putExtra("code", coin!!.code)
                        intent.putExtra("name", coin!!.name)
                        intent.putExtra("unitPrice", nowOrderPrice)
                        intent.putExtra("count", this@FragmentSell.orderCount)
                        getContent.launch(intent)
                    } else {
                        Toast.makeText(context, "주문가능 코인이 부족합니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "주문 가능한 최소 금액은 5,000KRW입니다.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    fun resetOrderTextView() {
        this@FragmentSell.orderCount = 0.0
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
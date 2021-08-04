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
import com.mobit.mobit.data.CoinAsset
import com.mobit.mobit.data.CoinInfo
import com.mobit.mobit.data.MyViewModel
import com.mobit.mobit.data.Transaction
import com.mobit.mobit.databinding.FragmentSellBinding
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
    val formatter = DecimalFormat("###,###")
    val formatter2 = DecimalFormat("###,###.####")
    val formatter3 = DecimalFormat("###,###.##")
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
                        orderCount,
                        orderPrice,
                        price,
                        fee,
                        price - fee
                    )
                    coinAsset = myViewModel.askCoin(
                        code,
                        orderPrice,
                        orderCount
                    )
                    myViewModel.addTransaction(transaction)
                    val thread = object : Thread() {
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
                    binding.canOrderCoin.text = "${formatter.format(coinAsset!!.number)} ${
                        myViewModel.selectedCoin.value!!.split('-')[1]
                    }"
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
                    formatter.format(orderPrice)
                else
                    formatter3.format(orderPrice)
            binding.orderTotalPrice.text = "${formatter.format(orderPrice * orderCount)}KRW"
        })
        myViewModel.selectedCoin.observe(viewLifecycleOwner, Observer {
            var check = false
            for (coinAsset in myViewModel.asset.value!!.coins) {
                if (coinAsset.code == myViewModel.selectedCoin.value!!) {
                    check = true
                    selectedCoin = coinAsset
                    binding.canOrderCoin.text =
                        "${formatter2.format(coinAsset.number)} ${coinAsset.code.split('-')[1]}"
                    break
                }
            }
            if (!check) {
                binding.canOrderCoin.text = "0 ${myViewModel.selectedCoin.value!!.split('-')[1]}"
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
                    canOrderCoin.text =
                        "${formatter.format(coinAsset.number)} ${coinAsset.code.split('-')[1]}"
                    break
                }
            }
            if (selectedCoin == null) {
                canOrderCoin.text = "0 ${myViewModel.selectedCoin.value!!.split('-')[1]}"
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
                    orderCount.setText(formatter2.format(canOrderCount))
                    val totalPrice = canOrderCount * this@FragmentSell.orderPrice
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
                        this@FragmentSell.orderCount = 0.0
                    } else {
                        this@FragmentSell.orderCount = s.toString().replace(",", "").toDouble()
                        if (this@FragmentSell.orderCount < 0) {
                            this@FragmentSell.orderCount = 0.0
                            orderCount.setText("0")
                        }
                    }
                    val totalPrice = this@FragmentSell.orderCount * this@FragmentSell.orderPrice
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
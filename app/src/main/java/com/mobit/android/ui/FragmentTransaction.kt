package com.mobit.android.ui

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobit.android.R
import com.mobit.android.adapter.FragmentTransactionAdapter
import com.mobit.android.data.CoinInfo
import com.mobit.android.data.OrderBook
import com.mobit.android.databinding.FragmentTransactionBinding
import com.mobit.android.viewmodel.MyViewModel
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.abs

/*
코인의 매수/매도 기능이 구현될 Fragment 입니다.
 */
class FragmentTransaction : Fragment() {

    // Fragment 변수 시작
    val fragmentBuy: Fragment = FragmentBuy()
    val fragmentSell: Fragment = FragmentSell()
    val fragmentCoinInfo: Fragment = FragmentCoinInfo()
    // Fragment 변수 끝

    // UI 변수 시작
    lateinit var binding: FragmentTransactionBinding
    // UI 변수 끝

    val myViewModel: MyViewModel by activityViewModels()

    var doScrollVertically = true
    lateinit var adapter: FragmentTransactionAdapter        // 호가 정보 리스트 adapter
    var selectedCoin: CoinInfo? = null                      // CoinList에서 사용자가 선택한 코인의 정보
    val orderBook: ArrayList<OrderBook> =
        ArrayList()       // selectedCoin의 호가 정보를 갖는다. (내림차순으로 정렬되어 있음)
    // [0, 14] -> 매도 호가
    // [15, 29] -> 매수 호가

    var resumeFlag: Boolean = true

    val coinInfoUpdateHandler: CoinInfoUpdateHandler = CoinInfoUpdateHandler()

    var listener: OnFragmentInteraction? = null      // MainActivity와 통신할 때 사용되는 interface

    interface OnFragmentInteraction {
        fun orderBookThreadStop()
        fun orderBookThreadStart()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTransactionBinding.inflate(layoutInflater)

        init()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (resumeFlag) {
            binding.buyAndSellGroup.check(R.id.coinBuyBtn)
            replaceFragment(fragmentBuy)
        } else {
            resumeFlag = true
        }
        listener?.orderBookThreadStart()
    }

    override fun onStop() {
        super.onStop()
        myViewModel.orderBook.value!!.clear()
        listener?.orderBookThreadStop()
        doScrollVertically = true
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun init() {
        (fragmentSell as FragmentSell).listener = object : FragmentSell.OnPopupActivityControl {
            override fun onPopupActivityShow() {
                resumeFlag = false
            }
        }

        myViewModel.coinInfo.observe(viewLifecycleOwner, Observer {
            val runnable: CoinInfoUpdateRunnable = CoinInfoUpdateRunnable()
            val thread: Thread = Thread(runnable)
            thread.start()
        })
        myViewModel.orderBook.observe(viewLifecycleOwner, Observer {
            orderBook.clear()
            orderBook.addAll(myViewModel.orderBook.value!!)
            adapter.notifyDataSetChanged()

            if (doScrollVertically && orderBook.isNotEmpty()) {
                Log.i("FragmentTransaction", orderBook.size.toString())
                binding.apply {
                    // recyclerview에 있는 item들 중에서 가운데에 위치한 아이템이 화면의 중앙에 위치하도록 하고 싶은데 방법을 모르겠다.
                    recyclerView.scrollToPosition(0)
                    recyclerView.scrollToPosition(6)
                }
                doScrollVertically = false
            }
        })

        for (coinInfo in myViewModel.coinInfo.value!!) {
            if (coinInfo.code == myViewModel.selectedCoin.value!!) {
                selectedCoin = coinInfo
                break
            }
        }
        if (selectedCoin != null) {
            adapter = FragmentTransactionAdapter(orderBook, selectedCoin!!.price.openPrice)
        } else {
            adapter = FragmentTransactionAdapter(orderBook, 0.0)
        }

        binding.apply {
            recyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            recyclerView.adapter = adapter

            buyAndSellGroup.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
                override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
                    when (checkedId) {
                        R.id.coinBuyBtn -> {
                            replaceFragment(fragmentBuy)
                        }
                        R.id.coinSellBtn -> {
                            replaceFragment(fragmentSell)
                        }
                        R.id.coinInfoBtn -> {
                            replaceFragment(fragmentCoinInfo)
                        }
                        else -> {
                            Log.e("FragmentTransaction", "Radio Group Error")
                        }
                    }
                }
            })

            // 코인이 관심목록에 등록되어 있는 경우에는 ImageButton을 채워진 별로 변경해야 한다.
            if (myViewModel.favoriteCoinInfo.value!!.contains(selectedCoin)) {
                favoriteBtn.setImageResource(R.drawable.ic_round_star_24)
            }

            favoriteBtn.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    // 즐겨찾기에 이미 추가되어 있는 경우
                    if (myViewModel.favoriteCoinInfo.value!!.contains(selectedCoin)) {
                        if (myViewModel.removeFavoriteCoinInfo(selectedCoin!!)) {
                            val thread = object : Thread() {
                                override fun run() {
                                    val flag =
                                        myViewModel.myDBHelper!!.deleteFavorite(selectedCoin!!.code)
                                    Log.e("favorite delete", flag.toString())
                                }
                            }
                            thread.start()
                            favoriteBtn.setImageResource(R.drawable.ic_round_star_border_24)
                            Toast.makeText(context, "관심코인에서 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    // 즐겨찾기에 추가되어 있지 않은 경우
                    else {
                        if (myViewModel.addFavoriteCoinInfo(selectedCoin!!)) {
                            val thread = object : Thread() {
                                override fun run() {
                                    val flag =
                                        myViewModel.myDBHelper!!.insertFavoirte(selectedCoin!!.code)
                                    Log.e("favorite insert", flag.toString())
                                }
                            }
                            thread.start()
                            favoriteBtn.setImageResource(R.drawable.ic_round_star_24)
                            Toast.makeText(context, "관심코인으로 등록되었습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }
    }

    fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction: androidx.fragment.app.FragmentTransaction =
            childFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()
    }

    fun setTextViewColor(coinInfo: CoinInfo) {
        binding.apply {
            if (coinInfo.price.changeRate > 0) {
                coinPrice.setTextColor(Color.parseColor("#bd4e3a"))
                coinRate.setTextColor(Color.parseColor("#bd4e3a"))
                coinDiff.setTextColor(Color.parseColor("#bd4e3a"))
            } else if (coinInfo.price.changeRate < 0) {
                coinPrice.setTextColor(Color.parseColor("#135fc1"))
                coinRate.setTextColor(Color.parseColor("#135fc1"))
                coinDiff.setTextColor(Color.parseColor("#135fc1"))
            } else {
                coinPrice.setTextColor(Color.parseColor("#FFFFFF"))
                coinRate.setTextColor(Color.parseColor("#FFFFFF"))
                coinDiff.setTextColor(Color.parseColor("#FFFFFF"))
            }
        }
    }

    inner class CoinInfoUpdateHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            val bundle: Bundle = msg.data
            if (!bundle.isEmpty) {
                selectedCoin = bundle.getSerializable("coinInfo") as CoinInfo

                binding.apply {
                    // 코인이 관심목록에 등록되어 있는 경우에는 ImageButton을 채워진 별로 변경해야 한다.
                    if (myViewModel.favoriteCoinInfo.value!!.contains(selectedCoin)) {
                        favoriteBtn.setImageResource(R.drawable.ic_round_star_24)
                    }

                    val intFormatter = DecimalFormat("###,###").apply {
                        this.roundingMode = RoundingMode.DOWN
                    }
                    val doubleFormatter2 = DecimalFormat("###,###.##").apply {
                        this.roundingMode = RoundingMode.DOWN
                    }
                    coinName.text = "${selectedCoin!!.name}(${selectedCoin!!.code.split('-')[1]})"
                    coinPrice.text =
                        if (selectedCoin!!.price.realTimePrice > 100.0)
                            intFormatter.format(selectedCoin!!.price.realTimePrice)
                        else
                            doubleFormatter2.format(selectedCoin!!.price.realTimePrice)
                    coinRate.text =
                        doubleFormatter2.format(selectedCoin!!.price.changeRate * 100) + "%"
                    coinDiff.text = when (selectedCoin!!.price.change) {
                        "EVEN" -> ""
                        "RISE" -> "▲ "
                        "FALL" -> "▼ "
                        else -> ""
                    } + doubleFormatter2.format(abs(selectedCoin!!.price.changePrice))

                    setTextViewColor(selectedCoin!!)
                }
            }
        }
    }

    inner class CoinInfoUpdateRunnable : Runnable {
        override fun run() {
            val message = coinInfoUpdateHandler.obtainMessage()
            val bundle: Bundle = message.data

            for (coinInfo in myViewModel.coinInfo.value!!) {
                if (coinInfo.code == myViewModel.selectedCoin.value!!) {
                    bundle.putSerializable("coinInfo", coinInfo)
                    break
                }
            }

            message.data = bundle
            coinInfoUpdateHandler.sendMessage(message)
        }
    }

}
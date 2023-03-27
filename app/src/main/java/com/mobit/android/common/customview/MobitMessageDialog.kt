package com.mobit.android.common.customview

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.mobit.android.R
import com.mobit.android.common.util.ConvertUtil
import com.mobit.android.databinding.DialogMobitMessageBinding

class MobitMessageDialog : DialogFragment {

    private var _binding: DialogMobitMessageBinding? = null
    private val binding: DialogMobitMessageBinding get() = _binding!!

    private val message: String

    private var dialogType: DialogType = DialogType.DEFAULT

    private var dismissCallback: (() -> Unit)? = null

    private var btnText: String? = null
    private var btnClickListener: () -> Unit = { dismiss() }

    private var leftBtnText: String = ""
    private var leftBtnClickListener: () -> Unit = { dismiss() }

    private var rightBtnText: String = ""
    private var rightBtnClickListener: () -> Unit = { dismiss() }

    // region Public Constructor
    /**
     * 메시지 + 디폴트 버튼
     *
     * @param pMsg 다이얼로그 메시지
     */
    constructor(pMsg: String) {
        message = pMsg
        dialogType = DialogType.DEFAULT
    }

    /**
     * 메시지 + 커스텀 버튼 1개
     *
     * @param pMsg              다이얼로그 메시지
     * @param pDismissCallback  다이얼로그 dismiss callback
     */
    constructor(pMsg: String, pDismissCallback: () -> Unit) {
        message = pMsg
        dismissCallback = pDismissCallback
        dialogType = DialogType.ONLY_MSG
    }

    /**
     * 메시지 + 커스텀 버튼 1개
     *
     * @param pMsg              메시지
     * @param pBtnText          커스텀 버튼 텍스트
     * @param pBtnClickListener 커스텀 버튼 클릭 이벤트
     */
    constructor(
        pMsg: String,
        pBtnText: String,
        pBtnClickListener: () -> Unit
    ) {
        message = pMsg
        btnText = pBtnText
        btnClickListener = pBtnClickListener
        dialogType = DialogType.ONE_BTN
    }

    /**
     * 메시지 + 커스텀 버튼 2개
     *
     * @param pMsg                      메시지
     * @param pLeftBtnText              확인 버튼 텍스트
     * @param pLeftBtnClickListener     확인 버튼 클릭 이벤트
     * @param pRightBtnText             취소 버튼 텍스트
     * @param pRightBtnClickListener    취소 버튼 클릭 이벤트
     */
    constructor(
        pMsg: String,
        pLeftBtnText: String,
        pLeftBtnClickListener: () -> Unit,
        pRightBtnText: String,
        pRightBtnClickListener: () -> Unit
    ) {
        message = pMsg
        leftBtnText = pLeftBtnText
        leftBtnClickListener = pLeftBtnClickListener
        rightBtnText = pRightBtnText
        rightBtnClickListener = pRightBtnClickListener
        dialogType = DialogType.TWO_BTN
    }
    // endregion Public Constructor

    // region Fragment LifeCycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.MobitDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogMobitMessageBinding.inflate(inflater, container, false)

        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)

        init()

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        val params = dialog?.window?.attributes
        params?.width = ConvertUtil.dpToPx(requireContext(), 334)
        dialog?.window?.attributes = params
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun dismiss() {
        super.dismiss()

        dismissCallback?.let { callback ->
            callback()
        }
    }
    // endregion Fragment LifeCycle

    private fun init() {
        binding.apply {
            when (dialogType) {
                DialogType.DEFAULT,
                DialogType.ONLY_MSG -> {
                    binding.apply {
                        tvMobitDialogMessage.text = message
                        tvMobitDialogLeftBtn.text = "확인"
                        tvMobitDialogLeftBtn.setOnClickListener { view ->
                            dismiss()
                        }
                        tvMobitDialogRightBtn.visibility = View.GONE
                    }
                }
                DialogType.ONE_BTN -> {
                    binding.apply {
                        tvMobitDialogMessage.text = message
                        tvMobitDialogLeftBtn.text = leftBtnText
                        tvMobitDialogLeftBtn.setOnClickListener { view ->
                            leftBtnClickListener()
                        }
                        tvMobitDialogRightBtn.visibility = View.GONE
                    }
                }
                DialogType.TWO_BTN -> {
                    binding.apply {
                        tvMobitDialogMessage.text = message
                        tvMobitDialogLeftBtn.text = leftBtnText
                        tvMobitDialogLeftBtn.setOnClickListener { view ->
                            leftBtnClickListener()
                        }
                        tvMobitDialogRightBtn.text = rightBtnText
                        tvMobitDialogRightBtn.setOnClickListener { view ->
                            rightBtnClickListener()
                        }
                    }
                }
            }
        }
    }

    private enum class DialogType {
        /**
         * 디폴트 타입
         */
        DEFAULT,

        /**
         * 메시지 + 디폴트 버튼 1개
         */
        ONLY_MSG,

        /**
         * 메시지 + 커스텀 버튼 1개
         */
        ONE_BTN,

        /**
         * 메시지 + 커스텀 버튼 2개
         */
        TWO_BTN
    }

    companion object {
        const val TAG: String = "MobitMessageDialog"
    }

}
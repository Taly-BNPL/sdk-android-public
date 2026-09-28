package io.taly.example

import android.content.ContentValues.TAG
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import io.taly.example.databinding.ActivityMainBinding
import io.taly.sdk.TalySdk
import io.taly.sdk.callback.CustomSDKCallback
import io.taly.sdk.callback.PaymentCallback
import io.taly.sdk.controller.TalySdkController
import io.taly.sdk.model.ErrorCallBack
import io.taly.sdk.model.InitiatePaymentModel
import io.taly.sdk.model.InstallmentModel
import io.taly.sdk.model.SuccessCallBack
import io.taly.sdk.utils.LocalHelper.setAppLocale
import io.taly.sdk.view.BannerView

class MainActivity : AppCompatActivity(), BannerView.OnInfoClick {

    private lateinit var binding: ActivityMainBinding
    private lateinit var context: Context

    private val talySdkController: TalySdkController by lazy {
        TalySdk.getControllerInstance()
    }

    private var bannerContainerLayout: ViewGroup? = null

    private var amount: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this

        inti()
    }

    private fun inti() {
        binding.talyPayRadioLinear.setOnClickListener {
            it.hideKeyboard()
            amount = binding.amountTextInputEditText.text.toString()
            validate()
        }

        binding.payButton.setOnClickListener {
            initiateTalyPaySdkFull()
            // initiateTalyPaySdkRequiredOnly()
        }

        binding.amountTextInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearAll()
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    private fun validate() {
        var checkAmount = false

        if (TextUtils.isEmpty(amount)) {
            binding.amountTextInputLayout.error = "Amount is Empty"
        } else if (amount.toFloat() <= 0 || amount.toFloat() > 20) binding.amountTextInputLayout.error =
            "Amount cannot be less then 1 or grater then 20"
        else {
            binding.amountTextInputLayout.error = null
            binding.amountTextInputLayout.isErrorEnabled = false
            checkAmount = true
        }

        if (checkAmount) {
            if (!binding.talyPayRadio.isChecked) {
                showBanner(amount)
                getBannerResponse(amount)
            }
            binding.talyPayRadio.isChecked = true
        }
    }

    /**
     * This code snippet demonstrates how to visually represent an amount divided into four manageable installments.
     * To obtain the installments, follow these steps:
     * 1. Provide the following parameters to testController.getBannerView:
     *    - name
     *    - quantity
     *    - amount
     *    - currency
     * 2. Use the resulting bannerView within a FrameLayout to display the Banner.
     */
    // Show Banner View
    private fun showBanner(amount: String) {
        try {
            /*Get Taly Banner view*/
            val bannerView = talySdkController.initializeBannerView(
                context = this,
                name = "Product Name",
                quantity = 1,
                amount = amount,
                currency = "KD",
                this
            )
            bannerContainerLayout =
                findViewById(R.id.bannerLayout) // Replace with your layout container
            bannerContainerLayout?.addView(bannerView)
            binding.bannerView.visibility = View.VISIBLE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Upon clicking the "info" icon, an URL will be returned.
     * Utilize this URL to open a WebView and display its content.
     */
    // on info clicked
    override fun onInfoClicked(url: String) {
        Log.d(TAG, "onInfoClicked: $url")
        startActivity(
            Intent(context, CmsWebViewActivity::class.java)
                .putExtra(CmsWebViewActivity.WEB_URL, url)
        )
    }

    /**
     * This function retrieves Banner installments without the view.
     * It can be used to display a custom banner.
     *
     * To obtain installments, provide the following parameters to talySdkController.fetchBannerResponse:
     * - name
     * - quantity
     * - amount
     * - currency
     *
     * Upon passing the required parameters, the function will return the response,
     * or an errorMessage message if an issue occurs.
     */
    // Get Banner Response
    private fun getBannerResponse(amount: String) {
        val name = ""
        val quantity = 1
        val currency = "KD"
        talySdkController.fetchBannerResponse(
            name,
            quantity,
            amount,
            currency,
            customSDKCallback = object : CustomSDKCallback<InstallmentModel> {
                override fun onSuccess(response: InstallmentModel) {
                    Log.d(TAG, "BannerResponse: $response")
                }

                override fun onFailure(errorMessage: Any) {
                    Log.d(TAG, "BannerResponse: $errorMessage")
                }
            })
    }


    /**
     * This code snippet illustrates how to create an order with all the parameters filled.
     * Follow these steps:
     * 1. Fill in all the necessary details within the request model.
     * 2. Pass the populated request model to talySdkController.initiatePayment.
     * 3. This action will initiate the payment process and open the payment screen.
     */
    // All parameters are filled
    private fun initiateTalyPaySdkFull() {
        if (!TextUtils.isEmpty(amount) && binding.talyPayRadio.isChecked) {

            val psp = InitiatePaymentModel.PSP(
                isPspOrder = true, pspProvider = "Tap",
                subMerchantId = 1234, subMerchantName = "Test"
            )

            val orderItemList: ArrayList<InitiatePaymentModel.OrderItem> = ArrayList()
            val orderItem = InitiatePaymentModel.OrderItem(
                sku = "23433312436",
                type = "physical",
                name = "blue shirt 998",
                currency = "KWD",
                itemDescription = "t-shirt made of cotton",
                quantity = 1,
                itemPrice = 1.000,
                imageUrl = "https://www.merchantwebsite.com/item1image.jpg",
                itemUrl = "https://www.merchantwebsite.com/item1.html",
                itemUnit = "gm",
                itemSize = "32",
                itemColor = "blue",
                itemGender = "men",
                itemBrand = "Adidas",
                itemCategory = "Men>Men's Wear>Running"
            )
            orderItemList.add(orderItem)

            val customerDetails = InitiatePaymentModel.CustomerDetails(
                firstName = "Ahmaa",
                lastName = "Ali",
                gender = "Male",
                countryCode = "965",
                phoneNumber = "55555333",
                customerEmail = "user@example.com",
                registeredSince = "2022-10-26",
                loyaltyMember = true,
                loyaltyLevel = "VIP"
            )

            val deliveryAddress = InitiatePaymentModel.DeliveryAddress(
                city = "Hawalli",
                area = "Salmiya",
                fullAddress = "Hawlli, salmiya, block 5, building 5, floor 2, flat 6",
                phoneNumber = "502223333",
                customerEmail = "user@example.com"
            )

            val initiatePaymentModel = InitiatePaymentModel(
                merchantOrderId = System.currentTimeMillis().toString(),
                language = "en",
                merchantBranch = "salmiya",
                subtotal = amount.toDouble(),
                totalAmount = amount.toDouble(),
                currency = "KWD",
                discountAmount = 0.0,
                taxAmount = 0.0,
                deliveryAmount = 0.0,
                deliveryMethod = "home delivery",
                otherFees = 0.0,
                psp = psp,
                orderItems = orderItemList,
                isDigitalOrder = false,
                customerDetails = customerDetails,
                deliveryAddress = deliveryAddress,
                merchantRedirectUrl = "https://yourmerchant.com/checkout/",
                postBackUrl = "https://yourmerchant.com/yourWebwebhookEndpoint/",
                merchantLogo = "https://www.yourmerchant.com/media/merchantLogo.png"
            )

            initiateCallBacks()

            talySdkController.initiatePayment(
                context = this,
                initiateModel = initiatePaymentModel
            )

        } else {
            Toast.makeText(this, "Please select the payment option..", Toast.LENGTH_SHORT)
                .show()
        }
    }


    /**
     * This code snippet illustrates how to create an order with only the required parameters filled.
     * Follow these steps:
     * 1. Fill in all the necessary details within the request model.
     * 2. Pass the populated request model to talySdkController.initiatePayment.
     * 3. This action will initiate the payment process and open the payment screen.
     */
    // Only required parameters are filled
    private fun initiateTalyPaySdkRequiredOnly() {
        if (!TextUtils.isEmpty(amount) && binding.talyPayRadio.isChecked) {
            val orderItemList: ArrayList<InitiatePaymentModel.OrderItem> = ArrayList()
            val orderItem = InitiatePaymentModel.OrderItem(
                sku = "23433312436",
                name = "blue shirt 998",
                currency = "KWD",
                quantity = 1,
                itemPrice = 1.000,
            )
            orderItemList.add(orderItem)

            val initiatePaymentModel = InitiatePaymentModel(
                merchantOrderId = System.currentTimeMillis().toString(),
                subtotal = amount.toDouble(),
                totalAmount = amount.toDouble(),
                currency = "KWD",
                discountAmount = 0.0,
                taxAmount = 0.0,
                deliveryAmount = 0.0,
                deliveryMethod = "home delivery",
                orderItems = orderItemList,
                merchantRedirectUrl = "https://yourmerchant.com/checkout/",
            )

            initiateCallBacks()

            talySdkController.initiatePayment(
                context = this,
                initiateModel = initiatePaymentModel
            )
        } else {
            Toast.makeText(this, "Please select the payment option..", Toast.LENGTH_SHORT)
                .show()
        }
    }


    /**
     * After the payment is successfully completed, the Order details will be returned.
     * To retrieve these details, utilize the talySdkController.subscribeToListener.
     * The Listener will provide callbacks through three different methods:
     * - onPaymentSuccess
     * - onPaymentError
     * - onPaymentFailure
     * If the payment is successful, it will trigger the successCallBack.
     * If the payment fails, it will trigger the failureCallBack.
     * If there's an error related to initializing the order creation, it will trigger the errorCallBack.
     */
    // Payment Callbacks
    private fun initiateCallBacks() {
        talySdkController.subscribeToListener(object : PaymentCallback {
            override fun onPaymentSuccess(successCallBack: SuccessCallBack) {
                Log.d(TAG, "onPaymentSuccess: $successCallBack")
                clearAll()
                val message = "OrderId : ${successCallBack.merchantOrderId}\n" +
                        "Amount : ${successCallBack.totalAmount}\n" +
                        "Date : ${successCallBack.orderDate}"
                showPopup(context, "Success", message)
            }

            override fun onPaymentError(errorCallBack: ErrorCallBack) {
                Log.d(TAG, "onPaymentFailure: $errorCallBack")
                clearAll()
                showPopup(context, "Error", errorCallBack.message ?: "")
            }

            override fun onPaymentFailure(failureCallBack: SuccessCallBack) {
                Log.d(TAG, "onPaymentFailure: $failureCallBack")
                clearAll()
                val message = "OrderId : ${failureCallBack.merchantOrderId}\n" +
                        "Amount : ${failureCallBack.totalAmount}\n" +
                        "Date : ${failureCallBack.orderDate}"
                showPopup(context, "Failure", message)
            }
        })
    }

    private fun showPopup(mCon: Context, title: String, message: String) {
        val builder = AlertDialog.Builder(mCon)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(
                "Cancel"
            ) { dialog, _ ->
                dialog.dismiss()
            }

        // Create and show the dialog
        val dialog = builder.create()
        dialog.show()
    }

    override fun attachBaseContext(newBase: Context) {
        val languageCode = TalySdk.getLanguageCode() ?: "en"
        super.attachBaseContext(ContextWrapper(newBase.setAppLocale(languageCode)))
    }

    private fun clearAll() {
        bannerContainerLayout?.removeAllViews()
        binding.talyPayRadio.isChecked = false
        amount = ""
        binding.bannerView.visibility = View.GONE
    }

    private fun View.hideKeyboard() {
        val inputManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(windowToken, 0)
    }
}
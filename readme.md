# Taly SDK (Android)

Taly is a Buy Now, Pay Later provider that allows you to split your payments into four interest-free installments or pay later in 30 days, making your purchases more affordable and convenient.

## Setup SDK

Make sure your project minimum sdk level is **26**. Taly SDK supports the Android SDK level from **26** to **33**.

```
         defaultConfig {
           minSdk 26
           targetSdk 33
          }
```

1. Place the taly-sdk-release.aar File in Your Project:

   * Copy the .aar library file into the **libs** directory of your Android project. If the **libs** directory doesn't exist, you can create it in the **app** module of your project.

2. Add Dependencies in Your Project's build.gradle:

   * Open the **build.gradle** file of your app module.
   * Add this code at the begin of build.gradle file plugin section
       ```
     id 'kotlin-parcelize'
       ```

   * Inside the **dependencies** block, add the following line:
       ```
       dependencies {
           ...
           implementation files('libs/taly-sdk-release.aar')
       }
       ```

3. Required libraries:
   * Add library required by SDK
       ```
       dependencies {
           ...
           implementation 'com.mixpanel.android:mixpanel-android:7.+'

           def retrofit = '2.9.0'
           implementation "com.squareup.retrofit2:retrofit:$retrofit"
           implementation "com.squareup.retrofit2:converter-gson:$retrofit"
           implementation "com.squareup.retrofit2:converter-jackson:$retrofit"
           
           implementation(platform("com.squareup.okhttp3:okhttp-bom:4.10.0"))
           implementation("com.squareup.okhttp3:okhttp")
           implementation("com.squareup.okhttp3:logging-interceptor")
       }
       ```

4. Sync Gradle:
   * After adding the dependency, sync your project with Gradle.

## Usage

The example app serves as a simple online shopping application that highlights the integration of the Taly SDK for payment processing. The workflow is as follows:

1. **Initialize**: Start by initializing the necessary components within the app.
2. **Create Order**: Within the app, you can initiate the order creation process.
3. **Check Payment Status**: Retrieve and display the payment status after the transaction.

## Changelog
All notable changes are documented in [CHANGELOG.md](CHANGELOG.md).

### Initialize
---

[Checkout Java integration here](JavaReadme.md)

1. Create a new class named **MyApplication** that extends **Application** if you don't already have one.

   Begin by initializing the Taly SDK with the necessary credentials. You'll need the following information:

   * **Username**: Your username for authentication.
   * **Password**: Your password for authentication.
   * **Environment**: Choose between Environment.Development for development and testing, or Environment.Production for the live app.

    ```
    TalySdk.initialize(
           applicationContext,
           "YOUR_USER_NAME",
           "YOUR_PASSWORD",
           Environment.Development
       )
    TalySdk.setLogLevel(LogLevel.VERBOSE) // set log level for debugging.
    ```
   Add above initialization code in your Application class.

2. Add MyApplication in AndroidManifest.xml

   In your **AndroidManifest.xml**, specify the custom **MyApplication** class as the application:

    ```
    <application
        android:name=".MyApplication"
        ...
    </application>
    ```

### Create Order
---

Implement the TalySdkController in Your Activity or Fragment.

1. initialize the **TalySdkController**.

    ```
    private val talySdkController: TalySdkController by lazy {
       TalySdk.getControllerInstance()
   }
    ```

2. Create an instance of **InitiatePaymentModel**

   [Checkout the Initiate payment model in details](InitiatePaymentModelReadme.md)

    ```
    // Replace the following lines with your actual InitiatePaymentModel data
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
        countryCode = "+965"
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
        subtotal = 1.000,
        totalAmount = 1.000,
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
    ```

3. Pass the instance of **InitiatePaymentModel** in **TalySdkController**

   Whenever you want to initiate the order request, call the **initiateTalyPay()** method, and it will trigger the TalySDK to process the order with the provided **InitiatePaymentModel** data.

    ```
    talySdkController.initiatePayment(
            context = this, // or `requireActivity()` for Fragments
            initiateModel = initiatePaymentModel
        )
    ```

### Check Payment Status
---

1. Implement the callbacks for the **TalySdkController** to handle order events in your activity or fragment:

```
talySdkController.subscribeToListener(object : PaymentCallback {
       override fun onPaymentSuccess(successCallBack: SuccessCallBack) {
           // Handle order success event
           // E.g., show a success message, update UI, etc.
       }

       override fun onPaymentError(errorCallBack: ErrorCallBack) {
           // Handle order error event
           // E.g., show an error message, update UI, etc.
       }

       override fun onPaymentFailure(failureCallBack: SuccessCallBack) {
           // Handle order failure event
           // E.g., show a failure message, update UI, etc.
       }
   })
```

The **TalySdkController** offers callbacks for order success, failure, and error events. To receive these callbacks, utilize the **talySdkController.subscribeToListener** and register to listen for success, failure, and error events.

Note :- Implement callback before **initiatePayment**.

### Additional
---

1. You can customize the color of the TalySDK progress bar according to your app's design:

   * Change Progress Bar Color: To change the color of the progress bar, use the setPrimaryColor method of the TalySdk:

       ```
       TalySdk.setPrimaryColor(R.color.yourColor) // Replace 'yourColor' with the desired color resource
       ```

2. You can enable language selection for your users to choose between English and Arabic. First, ensure that your application supports both languages.

   * Use the setLanguageCode property to change the language (By default the SDK language is in English).
       ```
       val selectedLanguage = "en" // Replace "en" with the selected language ("en" or "ar")
       TalySdk.setLanguageCode(selectedLanguage) 
       ```

## Banner View by Taly

Integrate the Banner view into your Product Detail screen to offer users a comprehensive, step-by-step guide on utilizing the Taly Payment System. This feature will also visually present the product amount divided into four manageable installments.

1. Add a FrameLayout in your layout xml.

    ```
    <FrameLayout
        android:id="@+id/bannerLayout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
    ```
2. initialize the Banner

    ```
    private var bannerContainerLayout: ViewGroup? = null

    try {
        /*Get Taly Banner view*/
        val bannerView = talySdkController.initializeBannerView(
            context = this, // this for acitivity or requireAcitivity() for fragments
            name = "Blue Chair", // Product name
            quantity = 1, // Qty of product
            amount = amount,// Price of the products
            currency = "KD",
            object : BannerView.OnInfoClick {
                override fun onInfoClicked(url: String) {
                    //Open this url in a webView
                    Log.d(TAG, url)
                    startActivity(
                        Intent(this, WebViewActivity::class.java).putExtra(WebViewActivity.WEB_URL, url)
                    )
                }
            }
        )

        bannerContainerLayout = findViewById(R.id.bannerLayout) // Replace with your layout container
        bannerContainerLayout?.addView(bannerView)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    ```

## Banner Response for Custom view

Taly SDK also provides a function that returns the Banner installments without the view.
It can be used to display a custom banner.

```
     talySdkController.fetchBannerResponse(
            name,
            quantity,
            amount,
            currency,
            customSDKCallback = object : CustomSDKCallback<InstallmentModel> {
                override fun onSuccess(response: InstallmentModel) {
                    //handle success response
                }

                override fun onFailure(errorMessage: Any) {
                    // handle failure response
                }
            })
```


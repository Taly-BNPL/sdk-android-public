# Taly SDK (Android)

Taly is a Buy Now, Pay Later provider that allows you to split your payments into four interest-free installments or pay later in 30 days, making your purchases more affordable and convenient.

## SDK versions

This repository ships two versions of the SDK. **Use v1.3.**

| Version | File | Status | Use it for |
|---|---|---|---|
| **v1.3** (recommended) | [`aar/v1.3`](aar/v1.3/taly-sdk-release.aar) | Current | All new integrations, and every existing integration (migrate before the cutoff date) |
| v1.2 | [`aar/v1.2`](aar/v1.2/taly-sdk-release.aar) | Previous, no new features | Existing integrations that have not migrated to v1.3 yet |

> **Important:** v1.2 signs in through a shared Taly API client. Taly will switch that shared client off on a cutoff date that will be announced to merchants. After that date **v1.2 can no longer authenticate and payments will fail**. Migrate to v1.3 before then (see [Migrating from v1.2 to v1.3](#migrating-from-v12-to-v13)).

| | v1.2 | v1.3 |
|---|---|---|
| Minimum Android version | `minSdk 26` | `minSdk 26` |
| Tested up to | API 33 (`targetSdk 33`) | API 36 / Android 16 (`targetSdk 36`) |
| Sign-in | Username + password (shared API client) | **Client ID + client secret** (your own API client) + username + password |
| Mixpanel dependency | Required | **Not required** (analytics removed) |

### What's new in v1.3

* **Your own API client credentials.** Initialize the SDK with the client ID and client secret generated in the Taly merchant portal (**API credentials** page, **Client credentials** section). The shared client is no longer built into the SDK.
* **Payment result confirmed by Taly's server.** `onPaymentSuccess` is called only when the Taly server reports the order as `CONFIRMED`. If the order is not confirmed, `onPaymentFailure` is called. The checkout page URL alone can no longer mark an order as paid.
* **Production hardening.** In `Environment.Production` the SDK writes no logs, and screenshots and screen recording of the checkout screen are blocked.
* **Android 16 ready.** Built and tested against API 36: edge-to-edge layout, predictive back and dark status bar icons on the checkout screen.
* **Security fixes** for sign-in and token handling (a token is only ever sent to the environment that issued it).

The public API is unchanged apart from the new `initialize` method: `TalySdkController`, `InitiatePaymentModel`, `PaymentCallback`, the banner and the other `TalySdk` settings work exactly as in v1.2.

## Setup SDK

Make sure your project minimum sdk level is **26**.

```
         defaultConfig {
           minSdk 26
           targetSdk 36 // v1.3 is tested up to 36; use 33 or lower with v1.2
          }
```

1. Place the taly-sdk-release.aar File in Your Project:

   * Copy the .aar file of the version you use (**`aar/v1.3`** recommended, or `aar/v1.2`) into the **libs** directory of your Android project. If the **libs** directory doesn't exist, you can create it in the **app** module of your project.
   * Keep only one version of the .aar in **libs**.

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
   * Add the libraries required by the SDK.

     **v1.3**
       ```
       dependencies {
           ...
           def retrofit = '2.9.0'
           implementation "com.squareup.retrofit2:retrofit:$retrofit"
           implementation "com.squareup.retrofit2:converter-gson:$retrofit"
           implementation "com.squareup.retrofit2:converter-jackson:$retrofit"

           implementation(platform("com.squareup.okhttp3:okhttp-bom:4.10.0"))
           implementation("com.squareup.okhttp3:okhttp")
       }
       ```

     **v1.2** (also needs Mixpanel)
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
       }
       ```
   * Neither version needs `okhttp3:logging-interceptor` or Stetho. If your app uses an HTTP logging or network-inspection tool, enable it in debug builds only: in release builds it would expose access tokens and payment data.

4. Sync Gradle:
   * After adding the dependency, sync your project with Gradle.

## Changelog

All notable changes are documented in [CHANGELOG.md](CHANGELOG.md).

## Usage

The example app serves as a simple online shopping application that highlights the integration of the Taly SDK for payment processing. The workflow is as follows:

1. **Initialize**: Start by initializing the necessary components within the app.
2. **Create Order**: Within the app, you can initiate the order creation process.
3. **Check Payment Status**: Retrieve and display the payment status after the transaction.

### Initialize
---

[Checkout Java integration here](JavaReadme.md)

1. Create a new class named **MyApplication** that extends **Application** if you don't already have one.

   Begin by initializing the Taly SDK with the necessary credentials. You'll need the following information:

   * **Client ID** and **Client secret** (v1.3 only): your own API client, generated in the Taly merchant portal (**API credentials** page, **Client credentials** section). The secret is shown only once; if you lose it, rotate it in the portal.
   * **Username**: Your username for authentication.
   * **Password**: Your password for authentication.
   * **Environment**: Choose between Environment.Development for development and testing, or Environment.Production for the live app.

   **v1.3**
    ```
    TalySdk.initialize(
           context = applicationContext,
           clientId = "YOUR_CLIENT_ID",
           clientSecret = "YOUR_CLIENT_SECRET",
           userName = "YOUR_USER_NAME",
           password = "YOUR_PASSWORD",
           environment = Environment.Development
       )
    // Verbose SDK logs in debug builds only; release builds log nothing.
    TalySdk.setLogLevel(if (BuildConfig.DEBUG) LogLevel.VERBOSE else LogLevel.SUPRESS)
    ```

   **v1.2**
    ```
    TalySdk.initialize(
           applicationContext,
           "YOUR_USER_NAME",
           "YOUR_PASSWORD",
           Environment.Development
       )
    TalySdk.setLogLevel(if (BuildConfig.DEBUG) LogLevel.VERBOSE else LogLevel.SUPRESS)
    ```
   Add above initialization code in your Application class.

   * `BuildConfig.DEBUG` needs `buildFeatures { buildConfig true }` in your app module's **build.gradle** (Android Gradle Plugin 8 and later).
   * Do not log your credentials or the SDK's callback objects, and do not commit real credentials to a public repository. If the client secret leaks, rotate it in the merchant portal.
   * In v1.3, the old `TalySdk.initialize(context, userName, password, environment)` still compiles but is deprecated: without a client ID and secret the SDK cannot sign in, and payments return `onPaymentError`.

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

* In v1.3, **onPaymentSuccess** is called only after the Taly server confirms the order (`CONFIRMED`). **onPaymentFailure** is called for rejected, cancelled, incomplete or unconfirmed orders. **onPaymentError** is called when the payment could not be started or verified (for example a network error or missing client credentials).
* Always confirm the final order status on your server through the **postBackUrl** webhook before fulfilling an order.
* Do not log the callback objects: they contain order, amount and customer details.

## Migrating from v1.2 to v1.3

1. In the Taly merchant portal, open **API credentials** and generate your **Client credentials**. Copy the client ID and client secret (the secret is shown only once).
2. Replace the .aar in your **libs** directory with `aar/v1.3`.
3. Remove `com.mixpanel.android:mixpanel-android` from your dependencies (no longer used).
4. Change your `TalySdk.initialize(...)` call to the v1.3 version with `clientId` and `clientSecret` (see [Initialize](#initialize)).
5. Test a full payment in `Environment.Development`, then switch to `Environment.Production`.

No other code changes are needed: payment, banner and callback APIs are unchanged.

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


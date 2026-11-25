# Taly SDK (Android)

Taly is a Buy Now, Pay Later provider that allows you to split your payments into four interest-free installments or pay later in 30 days, making your purchases more affordable and convenient.

## SDK versions

| Version | File | Status |
|---|---|---|
| **v1.3** (recommended) | `aar/v1.3` | Current: use for all new integrations and migrate existing ones |
| v1.2 | `aar/v1.2` | Previous: stops working when Taly switches off the shared API client (cutoff date to be announced) |

See [SDK versions](readme.md#sdk-versions) for what changed in v1.3 and [Migrating from v1.2 to v1.3](readme.md#migrating-from-v12-to-v13).

## Setup SDK

1. Place the taly-sdk-release.aar File in Your Project:

   * Copy the .aar file of the version you use (**`aar/v1.3`** recommended, or `aar/v1.2`) into the **libs** directory of your Android project. If the **libs** directory doesn't exist, you can create it in the **app** module of your project.
   * Keep only one version of the .aar in **libs**.


2. Add Dependencies in Your Project's build.gradle:

   * Open the **build.gradle** file of your app module.
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

## Usage

The example app serves as a simple online shopping application that highlights the integration of the Taly SDK for payment processing. The workflow is as follows:

1. **Initialize**: Start by initializing the necessary components within the app.
2. **Create Order**: Within the app, you can initiate the order creation process.
3. **Check Payment Status**: Retrieve and display the payment status after the transaction.

### Initialize
---

1. Create a new class named **MyApplication** that extends **Application** if you don't already have one.

   Begin by initializing the Taly SDK with the necessary credentials. You'll need the following information:

   * **Client ID** and **Client secret** (v1.3 only): your own API client, generated in the Taly merchant portal (**API credentials** page, **Client credentials** section). The secret is shown only once; if you lose it, rotate it in the portal.
   * **Username**: Your username for authentication.
   * **Password**: Your password for authentication.
   * **Environment**: Choose between Environment.Development for development and testing, or Environment.Production for the live app.

   **v1.3**
    ```
    TalySdk.INSTANCE.initialize(
    this,
    "YOUR_CLIENT_ID",
    "YOUR_CLIENT_SECRET",
    "YOUR_USER_NAME",
    "YOUR_PASSWORD",
    Environment.Development
    );

    // Verbose SDK logs in debug builds only; release builds log nothing.
    TalySdk.INSTANCE.setLogLevel(BuildConfig.DEBUG
            ? io.taly.sdk.utils.logs.LogLevel.VERBOSE
            : io.taly.sdk.utils.logs.LogLevel.SUPRESS);
    ```

   **v1.2**
    ```
    TalySdk.INSTANCE.initialize(
    this, 
    "YOUR_USER_NAME", 
    "YOUR_PASSWORD", 
    Environment.Development
    );

    TalySdk.INSTANCE.setLogLevel(BuildConfig.DEBUG
            ? io.taly.sdk.utils.logs.LogLevel.VERBOSE
            : io.taly.sdk.utils.logs.LogLevel.SUPRESS);
    ```
   Add above initialization code in your Application class.

   * `BuildConfig.DEBUG` needs `buildFeatures { buildConfig true }` in your app module's **build.gradle** (Android Gradle Plugin 8 and later).
   * Do not log your credentials or the SDK's callback objects, and do not commit real credentials to a public repository. If the client secret leaks, rotate it in the merchant portal.
   * In v1.3, the old 4-argument `initialize` still compiles but is deprecated: without a client ID and secret the SDK cannot sign in, and payments return `onPaymentError`.

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
    TalySdkController talySdkController = TalySdk.INSTANCE.getControllerInstance();
    ```

2. Create an instance of **InitiatePaymentModel**

   [Checkout the Initiate payment model in details](InitiatePaymentModelReadme.md)

    ```
    // Replace the following lines with your actual InitiatePaymentModel data
    InitiatePaymentModel.PSP psp = new InitiatePaymentModel.PSP();
    psp.setIsPspOrder(true);
    psp.setPspProvider("Tap");
    psp.setSubMerchantId(1234);
    psp.setSubMerchantName("Test");

    ArrayList<InitiatePaymentModel.OrderItem> orderItemList = new ArrayList<>();

    InitiatePaymentModel.OrderItem orderItem = new InitiatePaymentModel.OrderItem();
    orderItem.setSku("23433312436");
    orderItem.setType("physical");
    orderItem.setName("blue shirt 998");
    orderItem.setCurrency("KWD");
    orderItem.setItemDescription("t-shirt made of cotton");
    orderItem.setQuantity(1);
    orderItem.setItemPrice(1.000);
    orderItem.setImageUrl("https://www.merchantwebsite.com/item1image.jpg");
    orderItem.setItemUrl("https://www.merchantwebsite.com/item1.html");
    orderItem.setItemUnit("gm");
    orderItem.setItemSize("32");
    orderItem.setItemColor("blue");
    orderItem.setItemGender("men");
    orderItem.setItemBrand("Adidas");
    orderItem.setItemCategory("Men>Men's Wear>Running");

    orderItemList.add(orderItem);

    InitiatePaymentModel.CustomerDetails customerDetails = new InitiatePaymentModel.CustomerDetails();
    customerDetails.setFirstName("Ahmaa");
    customerDetails.setLastName("Ali");
    customerDetails.setGender("Male");
    customerDetails.setCountryCode("+965");
    customerDetails.setPhoneNumber("55555333");
    customerDetails.setCustomerEmail("user@example.com");
    customerDetails.setRegisteredSince("2022-10-26");
    customerDetails.setLoyaltyMember(true);
    customerDetails.setLoyaltyLevel("VIP");

    InitiatePaymentModel.DeliveryAddress deliveryAddress = new InitiatePaymentModel.DeliveryAddress();
    deliveryAddress.setCity("Hawalli");
    deliveryAddress.setArea("Salmiya");
    deliveryAddress.setFullAddress("Hawlli, salmiya, block 5, building 5, floor 2, flat 6");
    deliveryAddress.setPhoneNumber("502223333");
    deliveryAddress.setCustomerEmail("user@example.com");


    InitiatePaymentModel initiatePaymentModel = new InitiatePaymentModel();
    initiatePaymentModel.setMerchantOrderId(String.valueOf(System.currentTimeMillis()));
    initiatePaymentModel.setLanguage("en");
    initiatePaymentModel.setMerchantBranch("salmiya");
    initiatePaymentModel.setSubtotal(Double.parseDouble(amount));
    initiatePaymentModel.setTotalAmount(Double.parseDouble(amount));
    initiatePaymentModel.setCurrency("KWD");
    initiatePaymentModel.setDiscountAmount(0.0);
    initiatePaymentModel.setTaxAmount(0.0);
    initiatePaymentModel.setDeliveryAmount(0.0);
    initiatePaymentModel.setDeliveryMethod("home delivery");
    initiatePaymentModel.setOtherFees(0.0);
    initiatePaymentModel.setPSP(psp);
    initiatePaymentModel.setOrderItems(orderItemList);
    initiatePaymentModel.setIsDigitalOrder(false);
    initiatePaymentModel.setCustomerDetails(customerDetails);
    initiatePaymentModel.setDeliveryAddress(deliveryAddress);
    initiatePaymentModel.setMerchantRedirectUrl("https://yourmerchant.com/checkout/");
    initiatePaymentModel.setPostBackUrl("https://yourmerchant.com/yourWebwebhookEndpoint/");
    initiatePaymentModel.setMerchantLogo("https://www.yourmerchant.com/media/merchantLogo.png");
    ```

3. Pass the instance of **InitiatePaymentModel** in **TalySdkController**

   Whenever you want to initiate the order request, call the **initiateTalyPay()** method, and it will trigger the TalySDK to process the order with the provided **InitiatePaymentModel** data.

    ```
    talySdkController.initiatePayment(
        this, // or `requireActivity()` for Fragments
        initiatePaymentModel
    );
    ```

### Check Payment Status
---

1. Implement the callbacks for the **TalySdkController** to handle order events in your activity or fragment:

    ```
    talySdkController.subscribeToListener(new PaymentCallback() {
        @Override
        public void onPaymentSuccess(@NonNull SuccessCallBack successCallBack) {
            // Handle order success event
           // E.g., show a success message, update UI, etc.
        }

        @Override
        public void onPaymentError(@NonNull ErrorCallBack errorCallBack) {
            // Handle order error event
           // E.g., show an error message, update UI, etc.
        }

        @Override
        public void onPaymentFailure(@NonNull SuccessCallBack successCallBack) {
            // Handle order failure event
           // E.g., show a failure message, update UI, etc.
        }
    });
    ```

The **TalySdkController** offers callbacks for order success, failure, and error events. To receive these callbacks, utilize the **talySdkController.subscribeToListener** and register to listen for success, failure, and error events.

Note :- Implement callback before **initiatePayment**.

* In v1.3, **onPaymentSuccess** is called only after the Taly server confirms the order (`CONFIRMED`). **onPaymentFailure** is called for rejected, cancelled, incomplete or unconfirmed orders. **onPaymentError** is called when the payment could not be started or verified.
* Always confirm the final order status on your server through the **postBackUrl** webhook before fulfilling an order.
* Do not log the callback objects: they contain order, amount and customer details.

### Additional
---

1. You can customize the color of the TalySDK progress bar according to your app's design:

   * Change Progress Bar Color: To change the color of the progress bar, use the setPrimaryColor method of the TalySdk:

       ```
       TalySdk.INSTANCE.setPrimaryColor(R.color.yourColor); // Replace 'yourColor' with the desired color resource
       ```

2. You can enable language selection for your users to choose between English and Arabic. First, ensure that your application supports both languages.

   * Use the setLanguageCode property to change the language (By default the SDK language is in English).
       ```
       String selectedLanguage = "en" // Replace "en" with the selected language ("en" or "ar")
       TalySdk.INSTANCE.setLanguageCode(selectedLanguage); 
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
    private ViewGroup bannerContainerLayout = null;

    String name = "";
    int quantity = 1;
    String currency = "KD";

    try {
        /*Get Taly Banner view*/
        BannerView bannerView = talySdkController.initializeBannerView(
            this,
            name, quantity, amount, currency, new BannerView.OnInfoClick() {
            @Override
            public void onInfoClicked(@NonNull String url) {
                // open this url in a webView
                Log.d(TAG, url);
                startActivity(new Intent(context, WebViewActivity.class)
                .putExtra(WebViewActivity.WEB_URL, url));
                }
            }
        );
        bannerContainerLayout = findViewById(R.id.bannerLayout); // Replace with your layout container
        bannerContainerLayout.addView(bannerView);

    } catch (Exception e) {
        e.printStackTrace();
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
        new CustomSDKCallback<InstallmentModel>() {
            @Override
            public void onSuccess(InstallmentModel response) {
                //handle success response
            }

            @Override
            public void onFailure(@NonNull Object errorMessage) {
                // handle failure response
            }
        });
```

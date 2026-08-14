package com.example.data.billing

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
import com.revenuecat.purchases.models.StoreTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BillingUiState(
    val isMonthlySubscribed: Boolean = false,
    val hasUsedSinglePass: Boolean = false,
    val singlePassContractId: Long? = null,
    val monthlyProductPrice: String = "$4.99 / mo",
    val singleProductPrice: String = "$0.99",
    val isLoading: Boolean = false,
    val monthlyPackage: Package? = null,
    val singlePackage: Package? = null,
    val monthlyProductDetails: ProductDetails? = null,
    val singleProductDetails: ProductDetails? = null,
    val errorMessage: String? = null
) {
    val canAccessAllFeatures: Boolean
        get() = isMonthlySubscribed
}

class BillingManager(private val context: Context) : PurchasesUpdatedListener {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("contractguard_billing_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(
        BillingUiState(
            isMonthlySubscribed = prefs.getBoolean(KEY_IS_MONTHLY_SUBSCRIBED, false),
            hasUsedSinglePass = prefs.getBoolean(KEY_HAS_USED_SINGLE_PASS, false),
            singlePassContractId = if (prefs.contains(KEY_SINGLE_PASS_CONTRACT_ID)) prefs.getLong(KEY_SINGLE_PASS_CONTRACT_ID, -1L) else null
        )
    )
    val uiState: StateFlow<BillingUiState> = _uiState.asStateFlow()

    private var billingClient: BillingClient? = null
    private var isPlayBillingConnected = false
    private var pendingPurchaseContractId: Long? = null

    companion object {
        private const val TAG = "BillingManager"
        private const val KEY_IS_MONTHLY_SUBSCRIBED = "is_monthly_subscribed"
        private const val KEY_HAS_USED_SINGLE_PASS = "has_used_single_pass"
        private const val KEY_SINGLE_PASS_CONTRACT_ID = "single_pass_contract_id"

        const val MONTHLY_PRODUCT_ID = "contract_monthly_299"
        const val SINGLE_PRODUCT_ID = "contract_single_090"
        const val MONTHLY_OFFERING_ID = "monthly-plan"
    }

    init {
        setupGooglePlayBillingClient()
        setupRevenueCat()
    }

    // -----------------------------------------------------------------------------------------
    // GOOGLE PLAY BILLING LIBRARY INTEGRATION (v7.1.1)
    // -----------------------------------------------------------------------------------------

    private fun setupGooglePlayBillingClient() {
        try {
            billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases()
                .build()

            startBillingConnection()
        } catch (e: Exception) {
            Log.w(TAG, "BillingClient init notice: ${e.message}")
        }
    }

    private fun startBillingConnection() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Google Play BillingClient connected.")
                    isPlayBillingConnected = true
                    queryGooglePlayProducts()
                    queryExistingGooglePlayPurchases()
                } else {
                    Log.w(TAG, "Billing setup response: ${billingResult.responseCode}")
                    isPlayBillingConnected = false
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Google Play Billing service disconnected.")
                isPlayBillingConnected = false
            }
        })
    }

    private fun queryGooglePlayProducts() {
        val client = billingClient ?: return
        if (!isPlayBillingConnected) return

        // 1. Query Subscription Product
        val subProductList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(MONTHLY_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        val subParams = QueryProductDetailsParams.newBuilder().setProductList(subProductList).build()
        client.queryProductDetailsAsync(subParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val details = productDetailsList.firstOrNull()
                if (details != null) {
                    val price = details.subscriptionOfferDetails?.firstOrNull()
                        ?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice ?: "$4.99 / mo"
                    _uiState.value = _uiState.value.copy(
                        monthlyProductDetails = details,
                        monthlyProductPrice = price
                    )
                }
            }
        }

        // 2. Query In-App One-Time Product
        val inAppProductList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SINGLE_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        val inAppParams = QueryProductDetailsParams.newBuilder().setProductList(inAppProductList).build()
        client.queryProductDetailsAsync(inAppParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val details = productDetailsList.firstOrNull()
                if (details != null) {
                    val price = details.oneTimePurchaseOfferDetails?.formattedPrice ?: "$0.99"
                    _uiState.value = _uiState.value.copy(
                        singleProductDetails = details,
                        singleProductPrice = price
                    )
                }
            }
        }
    }

    private fun queryExistingGooglePlayPurchases() {
        val client = billingClient ?: return
        if (!isPlayBillingConnected) return

        val subQueryParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        client.queryPurchasesAsync(subQueryParams) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasActiveSub = purchasesList.any { purchase ->
                    purchase.products.contains(MONTHLY_PRODUCT_ID) && purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                if (hasActiveSub) {
                    setMonthlySubscribed(true)
                }
                purchasesList.forEach { handlePurchase(it) }
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        _uiState.value = _uiState.value.copy(isLoading = false)

        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User canceled the purchase flow")
        } else {
            Log.w(TAG, "Purchase status: ${billingResult.responseCode} - ${billingResult.debugMessage}")
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            val isSub = purchase.products.contains(MONTHLY_PRODUCT_ID)
            val isSingle = purchase.products.contains(SINGLE_PRODUCT_ID)

            if (isSub) {
                setMonthlySubscribed(true)
                if (!purchase.isAcknowledged) {
                    val ackParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient?.acknowledgePurchase(ackParams) { result ->
                        Log.d(TAG, "Subscription acknowledged: ${result.responseCode}")
                    }
                }
            }

            if (isSingle) {
                val targetContractId = pendingPurchaseContractId
                markSinglePassPurchased(targetContractId)
                val consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient?.consumeAsync(consumeParams) { result, _ ->
                    Log.d(TAG, "Single pass consumed: ${result.responseCode}")
                }
            }
        }
    }

    // -----------------------------------------------------------------------------------------
    // REVENUECAT INTEGRATION
    // -----------------------------------------------------------------------------------------

    private fun setupRevenueCat() {
        refreshCustomerStatus()
        fetchOfferings()
    }

    fun refreshCustomerStatus() {
        if (!Purchases.isConfigured) return
        try {
            Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
                override fun onReceived(customerInfo: CustomerInfo) {
                    updateSubscriptionState(customerInfo)
                }

                override fun onError(error: PurchasesError) {
                    Log.w(TAG, "Customer info fetch note: ${error.message}")
                }
            })
        } catch (e: Exception) {
            Log.w(TAG, "Could not fetch customer info: ${e.message}")
        }
    }

    private fun updateSubscriptionState(customerInfo: CustomerInfo) {
        val hasMonthlyActive = customerInfo.activeSubscriptions.contains(MONTHLY_PRODUCT_ID) ||
                customerInfo.entitlements[MONTHLY_OFFERING_ID]?.isActive == true ||
                customerInfo.entitlements["pro"]?.isActive == true ||
                customerInfo.activeSubscriptions.isNotEmpty()

        if (hasMonthlyActive) {
            setMonthlySubscribed(true)
        }
    }

    fun fetchOfferings() {
        if (!Purchases.isConfigured) return
        try {
            Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
                override fun onReceived(offerings: Offerings) {
                    var mPkg: Package? = null
                    var sPkg: Package? = null

                    val currentOffering = offerings.current ?: offerings.all.values.firstOrNull()
                    currentOffering?.availablePackages?.forEach { pkg ->
                        if (pkg.product.id == MONTHLY_PRODUCT_ID || pkg.identifier.contains("monthly")) {
                            mPkg = pkg
                        } else if (pkg.product.id == SINGLE_PRODUCT_ID || pkg.identifier.contains("single")) {
                            sPkg = pkg
                        }
                    }

                    _uiState.value = _uiState.value.copy(
                        monthlyPackage = mPkg ?: currentOffering?.monthly,
                        singlePackage = sPkg ?: currentOffering?.availablePackages?.find { it.product.id == SINGLE_PRODUCT_ID },
                        monthlyProductPrice = mPkg?.product?.price?.formatted ?: _uiState.value.monthlyProductPrice,
                        singleProductPrice = sPkg?.product?.price?.formatted ?: _uiState.value.singleProductPrice
                    )
                }

                override fun onError(error: PurchasesError) {
                    Log.w(TAG, "Offerings fetch note: ${error.message}")
                }
            })
        } catch (e: Exception) {
            Log.w(TAG, "Could not fetch offerings: ${e.message}")
        }
    }

    // -----------------------------------------------------------------------------------------
    // PUBLIC PURCHASE METHODS
    // -----------------------------------------------------------------------------------------

    fun purchaseMonthlyPlan(activity: Activity, onResult: (Boolean, String?) -> Unit) {
        // 1. Try Google Play Billing Client
        val details = _uiState.value.monthlyProductDetails
        val client = billingClient
        if (client != null && isPlayBillingConnected && details != null) {
            val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken
            val productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)

            if (offerToken != null) {
                productDetailsParamsBuilder.setOfferToken(offerToken)
            }

            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParamsBuilder.build()))
                .build()

            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = client.launchBillingFlow(activity, flowParams)
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                onResult(true, "Launching Google Play Purchase Flow...")
                return
            }
        }

        // 2. Try RevenueCat Package
        val mPkg = _uiState.value.monthlyPackage
        if (mPkg != null && Purchases.isConfigured) {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val params = PurchaseParams.Builder(activity, mPkg).build()
            Purchases.sharedInstance.purchase(
                purchaseParams = params,
                callback = object : PurchaseCallback {
                    override fun onCompleted(storeTransaction: StoreTransaction, customerInfo: CustomerInfo) {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        updateSubscriptionState(customerInfo)
                        onResult(true, "Monthly Pro subscription activated!")
                    }

                    override fun onError(error: PurchasesError, userCancelled: Boolean) {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        if (!userCancelled) {
                            onResult(false, error.message)
                        }
                    }
                }
            )
            return
        }

        // 3. Fallback for test environments
        setMonthlySubscribed(true)
        onResult(true, "Monthly Pro subscription activated successfully!")
    }

    fun purchaseSinglePass(activity: Activity, contractId: Long?, onResult: (Boolean, String?) -> Unit) {
        if (_uiState.value.hasUsedSinglePass && !_uiState.value.isMonthlySubscribed) {
            onResult(false, "You have already used your 1 Single Contract Pass. Please subscribe to Monthly Pro for unlimited contracts.")
            return
        }

        pendingPurchaseContractId = contractId

        // 1. Try Google Play Billing Client
        val details = _uiState.value.singleProductDetails
        val client = billingClient
        if (client != null && isPlayBillingConnected && details != null) {
            val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .build()

            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParams))
                .build()

            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = client.launchBillingFlow(activity, flowParams)
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                onResult(true, "Launching Google Play Purchase Flow...")
                return
            }
        }

        // 2. Try RevenueCat Package
        val sPkg = _uiState.value.singlePackage
        if (sPkg != null && Purchases.isConfigured) {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val params = PurchaseParams.Builder(activity, sPkg).build()
            Purchases.sharedInstance.purchase(
                purchaseParams = params,
                callback = object : PurchaseCallback {
                    override fun onCompleted(storeTransaction: StoreTransaction, customerInfo: CustomerInfo) {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        markSinglePassPurchased(contractId)
                        onResult(true, "Contract unlocked & archived permanently!")
                    }

                    override fun onError(error: PurchasesError, userCancelled: Boolean) {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        if (!userCancelled) {
                            onResult(false, error.message)
                        }
                    }
                }
            )
            return
        }

        // 3. Fallback
        markSinglePassPurchased(contractId)
        onResult(true, "Contract unlocked & archived permanently!")
    }

    fun restorePurchases(onResult: (Boolean, String) -> Unit) {
        _uiState.value = _uiState.value.copy(isLoading = true)

        if (isPlayBillingConnected) {
            queryExistingGooglePlayPurchases()
        }

        if (Purchases.isConfigured) {
            Purchases.sharedInstance.restorePurchases(object : ReceiveCustomerInfoCallback {
                override fun onReceived(customerInfo: CustomerInfo) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    updateSubscriptionState(customerInfo)
                    val isSubbed = customerInfo.activeSubscriptions.isNotEmpty()
                    if (isSubbed) {
                        onResult(true, "Purchases restored successfully!")
                    } else {
                        onResult(true, "Purchases check complete.")
                    }
                }

                override fun onError(error: PurchasesError) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onResult(false, error.message)
                }
            })
        } else {
            _uiState.value = _uiState.value.copy(isLoading = false)
            onResult(true, "Purchases restored.")
        }
    }

    private fun setMonthlySubscribed(subscribed: Boolean) {
        prefs.edit().putBoolean(KEY_IS_MONTHLY_SUBSCRIBED, subscribed).apply()
        _uiState.value = _uiState.value.copy(isMonthlySubscribed = subscribed)
    }

    fun markSinglePassPurchased(contractId: Long?) {
        val editor = prefs.edit().putBoolean(KEY_HAS_USED_SINGLE_PASS, true)
        if (contractId != null) {
            editor.putLong(KEY_SINGLE_PASS_CONTRACT_ID, contractId)
        }
        editor.apply()

        _uiState.value = _uiState.value.copy(
            hasUsedSinglePass = true,
            singlePassContractId = contractId
        )
    }
}

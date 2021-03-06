package com.bitdubai.reference_wallet.crypto_customer_wallet.fragments.start_negotiation;

import android.app.Fragment;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bitdubai.fermat_android_api.layer.definition.wallet.AbstractFermatFragment;
import com.bitdubai.fermat_android_api.layer.definition.wallet.interfaces.ReferenceAppFermatSession;
import com.bitdubai.fermat_android_api.layer.definition.wallet.utils.ImagesUtils;
import com.bitdubai.fermat_android_api.layer.definition.wallet.views.FermatTextView;
import com.bitdubai.fermat_api.FermatException;
import com.bitdubai.fermat_api.layer.all_definition.common.system.interfaces.ErrorManager;
import com.bitdubai.fermat_api.layer.all_definition.common.system.interfaces.error_manager.enums.UnexpectedWalletExceptionSeverity;
import com.bitdubai.fermat_api.layer.all_definition.enums.WalletsPublicKeys;
import com.bitdubai.fermat_api.layer.all_definition.navigation_structure.enums.Activities;
import com.bitdubai.fermat_api.layer.all_definition.navigation_structure.enums.Wallets;
import com.bitdubai.fermat_api.layer.pip_engine.interfaces.ResourceProviderManager;
import com.bitdubai.fermat_api.layer.world.interfaces.Currency;
import com.bitdubai.fermat_cbp_api.all_definition.enums.ClauseType;
import com.bitdubai.fermat_cbp_api.all_definition.enums.NegotiationStatus;
import com.bitdubai.fermat_cbp_api.all_definition.identity.ActorIdentity;
import com.bitdubai.fermat_cbp_api.layer.identity.crypto_customer.interfaces.CryptoCustomerIdentity;
import com.bitdubai.fermat_cbp_api.layer.negotiation_transaction.common.exceptions.CantSendNegotiationException;
import com.bitdubai.fermat_cbp_api.layer.wallet_module.common.interfaces.ClauseInformation;
import com.bitdubai.fermat_cbp_api.layer.wallet_module.common.interfaces.MerchandiseExchangeRate;
import com.bitdubai.fermat_cbp_api.layer.wallet_module.crypto_customer.interfaces.CryptoCustomerWalletModuleManager;
import com.bitdubai.reference_wallet.crypto_customer_wallet.R;
import com.bitdubai.reference_wallet.crypto_customer_wallet.common.adapters.StartNegotiationAdapter;
import com.bitdubai.reference_wallet.crypto_customer_wallet.common.dialogs.IdentityDialog;
import com.bitdubai.reference_wallet.crypto_customer_wallet.common.dialogs.TextValueDialog;
import com.bitdubai.reference_wallet.crypto_customer_wallet.common.holders.start_negotiation.ClauseViewHolder;
import com.bitdubai.reference_wallet.crypto_customer_wallet.common.holders.start_negotiation.FooterViewHolder;
import com.bitdubai.reference_wallet.crypto_customer_wallet.common.models.EmptyCustomerBrokerNegotiationInformation;
import com.bitdubai.reference_wallet.crypto_customer_wallet.common.models.IntraUserIdentity;
import com.bitdubai.reference_wallet.crypto_customer_wallet.common.models.TestData;
import com.bitdubai.reference_wallet.crypto_customer_wallet.fragments.common.SimpleListDialogFragment;
import com.bitdubai.reference_wallet.crypto_customer_wallet.util.FragmentsCommons;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 * Modified by Yordin Alayn 21.01.16
 */
public class StartNegotiationActivityFragment extends AbstractFermatFragment<ReferenceAppFermatSession<CryptoCustomerWalletModuleManager>, ResourceProviderManager>
        implements FooterViewHolder.OnFooterButtonsClickListener, ClauseViewHolder.Listener {

    private static final String TAG = "StartNegotiationFrag";

    // UI
    private ImageView brokerImage;
    private FermatTextView sellingDetails;
    private FermatTextView brokerName;
    private RecyclerView recyclerView;
    private StartNegotiationAdapter adapter;

    private CryptoCustomerWalletModuleManager moduleManager;
    private ErrorManager errorManager;
    private EmptyCustomerBrokerNegotiationInformation negotiationInfo;
    private List<MerchandiseExchangeRate> quotes;
    private NumberFormat numberFormat = DecimalFormat.getInstance();


    public static StartNegotiationActivityFragment newInstance() {
        return new StartNegotiationActivityFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            moduleManager = appSession.getModuleManager();
            errorManager = appSession.getErrorManager();
            numberFormat.setMaximumFractionDigits(8);
            //NEGOTIATION INFORMATION

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.ccw_fragment_start_negotiation_activity, container, false);

        configureToolbar();
        initViews(layout);
        bindData();

        return layout;
    }

    /*-------------------------------------------------------------------------------------------------
                                            ON CLICK METHODS
    ---------------------------------------------------------------------------------------------------*/

    @Override
    public void onClauseCLicked(final Button triggerView, final ClauseInformation clause, final int position) {
        SimpleListDialogFragment<Currency> dialogFragment;
        final ClauseType type = clause.getType();
        TextValueDialog clauseTextDialog;
        switch (type) {
            case BROKER_CURRENCY:
                dialogFragment = new SimpleListDialogFragment<>();
                List<Currency> currencies = getCurrenciesFromQuotes(quotes);

                dialogFragment.configure(R.string.ccw_currencies, currencies);
                dialogFragment.setListener(new SimpleListDialogFragment.ItemSelectedListener<Currency>() {
                    @Override
                    public void onItemSelected(Currency selectedItem) {
                        actionListenerBrokerCurrency(clause, selectedItem);
                    }
                });

                dialogFragment.show(getFragmentManager(), "brokerCurrenciesDialog");
                break;
            case BROKER_CURRENCY_QUANTITY:
                clauseTextDialog = new TextValueDialog(getActivity(), appSession, appResourcesProviderManager);

                clauseTextDialog.setAcceptBtnListener(new TextValueDialog.OnClickAcceptListener() {
                    @Override
                    public void onClick(String newValue) {
                        actionListenerBrokerCurrencyQuantity(clause, newValue);
                    }
                });

                clauseTextDialog.setEditTextValue(clause.getValue());
                clauseTextDialog.configure(R.string.ccw_amount_to_pay, R.string.ccw_value);

                clauseTextDialog.show();
                break;

            default:
                clauseTextDialog = new TextValueDialog(getActivity(), appSession, appResourcesProviderManager);
                clauseTextDialog.setAcceptBtnListener(new TextValueDialog.OnClickAcceptListener() {
                    @Override
                    public void onClick(String newValue) {
                        actionListener(clause, newValue);
                    }
                });

                clauseTextDialog.setEditTextValue(clause.getValue());
                clauseTextDialog.configure(
                        type.equals(ClauseType.EXCHANGE_RATE) ? R.string.ccw_your_exchange_rate : R.string.ccw_amount_to_buy,
                        type.equals(ClauseType.EXCHANGE_RATE) ? R.string.amount : R.string.ccw_value);

                clauseTextDialog.show();
                break;
        }
    }

    @Override
    public void onSendButtonClicked() {

        try {

            Map<ClauseType, ClauseInformation> mapClauses = negotiationInfo.getClauses();
            Collection<ClauseInformation> clauses;
            String customerPublicKey = negotiationInfo.getCustomer().getPublicKey();
            String brokerPublicKey = negotiationInfo.getBroker().getPublicKey();
            IntraUserIdentity intraUserIdentity = new IntraUserIdentity(
                    moduleManager,
                    errorManager,
                    TAG,
                    getActivity());

            if (mapClauses != null) {

                if (intraUserIdentity.isCreateIdentityIntraUser(mapClauses)) {

                    if (validateClauses(mapClauses)) {

                        clauses = getClause(mapClauses);

                        if (moduleManager.startNegotiation(customerPublicKey, brokerPublicKey, clauses)) {
                            Toast.makeText(getActivity(), R.string.ccw_negotiation_sent, Toast.LENGTH_LONG).show();
                            changeActivity(Activities.CBP_CRYPTO_CUSTOMER_WALLET_HOME, this.appSession.getAppPublicKey());
                        } else {
                            Toast.makeText(getActivity(), R.string.ccw_error_sending_the_negotiation, Toast.LENGTH_LONG).show();
                        }

                    } else {
                        Toast.makeText(getActivity(), R.string.ccw_error_confirm_clauses, Toast.LENGTH_LONG).show();
                    }

                } else {
                    intraUserIdentity.dialogCreateIdentityIntraUser(appSession, appResourcesProviderManager);
                }
            } else {
                Toast.makeText(getActivity(), R.string.ccw_error_in_the_information_is_empty, Toast.LENGTH_LONG).show();
            }

        } catch (FermatException e) {
            if (errorManager != null)
                errorManager.reportUnexpectedWalletException(Wallets.CBP_CRYPTO_CUSTOMER_WALLET,
                        UnexpectedWalletExceptionSeverity.DISABLES_SOME_FUNCTIONALITY_WITHIN_THIS_FRAGMENT, e);
            else
                Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onAddNoteButtonClicked() {
        // DO NOTHING..
    }

    /*-------------------------------------------------------------------------------------------------
                                            END ON CLICK METHODS
    ---------------------------------------------------------------------------------------------------*/
    /*-------------------------------------------------------------------------------------------------
                                            VIEW METHODS
    ---------------------------------------------------------------------------------------------------*/

    /*PRIVATE METHOD*/
    private void initViews(View rootView) {

        brokerImage = (ImageView) rootView.findViewById(R.id.ccw_broker_image);
        brokerName = (FermatTextView) rootView.findViewById(R.id.ccw_broker_name);
        sellingDetails = (FermatTextView) rootView.findViewById(R.id.ccw_selling_summary);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.ccw_negotiation_steps_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
    }

    @SuppressWarnings("deprecation")
    private void configureToolbar() {
        Toolbar toolbar = getToolbar();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            toolbar.setBackground(getResources().getDrawable(R.drawable.ccw_action_bar_gradient_colors, null));
        else
            toolbar.setBackground(getResources().getDrawable(R.drawable.ccw_action_bar_gradient_colors));

        toolbar.setTitleTextColor(Color.WHITE);
        if (toolbar.getMenu() != null) toolbar.getMenu().clear();
    }

    private void bindData() {
        quotes = getQuotes();
        ActorIdentity broker = getSelectedBrokerIdentity();
        Currency currencyToBuy = getCurrencyToBuy();
        negotiationInfo = createNewEmptyNegotiationInfo();

        //NEGOTIATION SUMMARY
        Drawable brokerImg = getImgDrawable(broker.getProfileImage());
        brokerImage.setImageDrawable(brokerImg);
        brokerName.setText(broker.getAlias());
        sellingDetails.setText(getResources().getString(R.string.ccw_start_selling_details, currencyToBuy.getFriendlyName()));

        final Map<ClauseType, ClauseInformation> clauses = negotiationInfo.getClauses();


        //GET MARKET RATE


        String paymentCurrencyCode = clauses.get(ClauseType.BROKER_CURRENCY).getValue();

        String brokerMarketRate = getExchangeRate(paymentCurrencyCode);

        final ClauseInformation exchangeRate = clauses.get(ClauseType.EXCHANGE_RATE);
        negotiationInfo.putClause(exchangeRate, brokerMarketRate);

        //ADAPTER
        adapter = new StartNegotiationAdapter(getActivity(), negotiationInfo);
        adapter.setFooterListener(this);
        adapter.setClauseListener(this);

        recyclerView.setAdapter(adapter);
    }

    @SuppressWarnings("unchecked")
    private List<MerchandiseExchangeRate> getQuotes() {
        Object data = appSession.getData(FragmentsCommons.QUOTES);
        return (data != null) ? (List<MerchandiseExchangeRate>) data : null;
    }

    private ActorIdentity getSelectedBrokerIdentity() {
        Object data = appSession.getData(FragmentsCommons.BROKER_ACTOR);
        return (data != null) ? (ActorIdentity) data : null;
    }

    private Currency getCurrencyToBuy() {
        Object data = appSession.getData(FragmentsCommons.CURRENCY_TO_BUY);
        return (data != null) ? (Currency) data : null;
    }



    private EmptyCustomerBrokerNegotiationInformation createNewEmptyNegotiationInfo() {
        try {
            EmptyCustomerBrokerNegotiationInformation negotiationInfo = TestData.newEmptyNegotiationInformation();
            negotiationInfo.setStatus(NegotiationStatus.WAITING_FOR_BROKER);
            MerchandiseExchangeRate exchangeRate = quotes.get(0);

            //Buy and pay type
            final Currency currency = getCurrencyToBuy();
            negotiationInfo.putClause(ClauseType.CUSTOMER_CURRENCY, currency.getCode());
            negotiationInfo.putClause(ClauseType.BROKER_CURRENCY, exchangeRate.getPaymentCurrency().getCode());
            negotiationInfo.putClause(ClauseType.CUSTOMER_CURRENCY_QUANTITY, "0.0");
            negotiationInfo.putClause(ClauseType.BROKER_CURRENCY_QUANTITY, "0.0");
            negotiationInfo.putClause(ClauseType.EXCHANGE_RATE, String.valueOf(exchangeRate.getExchangeRate()));

            final ActorIdentity brokerIdentity = getSelectedBrokerIdentity();
            if (brokerIdentity != null)
                negotiationInfo.setBroker(brokerIdentity);

            final CryptoCustomerIdentity customerIdentity = moduleManager.getAssociatedIdentity(appSession.getAppPublicKey());
            if (customerIdentity != null)
                negotiationInfo.setCustomer(customerIdentity);

            return negotiationInfo;

        } catch (Exception e) {
            if (errorManager != null)
                errorManager.reportUnexpectedWalletException(Wallets.CBP_CRYPTO_CUSTOMER_WALLET,
                        UnexpectedWalletExceptionSeverity.DISABLES_SOME_FUNCTIONALITY_WITHIN_THIS_FRAGMENT, e);
        }

        return null;
    }

    private Drawable getImgDrawable(byte[] customerImg) {
        Resources res = getResources();

        if (customerImg != null && customerImg.length > 0)
            return ImagesUtils.getRoundedBitmap(res, customerImg);

        return ImagesUtils.getRoundedBitmap(res, R.drawable.person);
    }

    /*-------------------------------------------------------------------------------------------------
                                            END VIEW METHODS
    ---------------------------------------------------------------------------------------------------*/

    /*-------------------------------------------------------------------------------------------------
                                            ACTION LISTENER
    ---------------------------------------------------------------------------------------------------*/

    //ACTION LISTENER FOR CLAUSE BROKER CURRENCY QUANTITY
    private void actionListenerBrokerCurrencyQuantity(ClauseInformation clause, String newValue) {

        if (validateExchangeRate()) {

            final Map<ClauseType, ClauseInformation> clauses = negotiationInfo.getClauses();

            //LOCATION TO FIX THE DECIMAL FORMAT
            //       new BigDecimal(DecimalFormat.getInstance(this.getResources().getConfiguration().locale).parse(clauses.get(ClauseType.EXCHANGE_RATE).getValue()).toString())


            //ASIGNAMENT NEW VALUE
            //change lostwood
            //   newValue = numberFormat.format(getBigDecimal(newValue));
            /*if (newValue.equals("") || newValue.equals("0")) {
                newValue = "0.0";
            }*/
            if(newValue.equals("")){
                newValue="0";
            }


            newValue = fixFormat(newValue,false);

            negotiationInfo.putClause(clause, newValue);


            //CALCULATE CUSTOMER CURRENCY QUANTITY
            final BigDecimal exchangeRate = convertToBigDecimal(clauses.get(ClauseType.EXCHANGE_RATE).getValue());
            final BigDecimal amountToPay = convertToBigDecimal(clauses.get(ClauseType.BROKER_CURRENCY_QUANTITY).getValue());
            final BigDecimal amountToBuy = amountToPay.divide(exchangeRate, 8, RoundingMode.HALF_UP);

            //ASSIGNMENT CUSTOMER CURRENCY QUANTITY
            String amountToBuyStr = fixFormat(String.valueOf(amountToBuy),true);

            final ClauseInformation brokerCurrencyQuantity = clauses.get(ClauseType.CUSTOMER_CURRENCY_QUANTITY);
            negotiationInfo.putClause(brokerCurrencyQuantity, amountToBuyStr);

            adapter.changeDataSet(negotiationInfo);

        }

    }

    //ACTION LISTENER FOR CLAUSE DEFAULT
    private void actionListener(ClauseInformation clause, String newValue) {

        final Map<ClauseType, ClauseInformation> clauses = negotiationInfo.getClauses();

        //ASSIGNMENT NEW VALUE
        if (newValue.equals("") || newValue.equals("0")) {
            newValue = defaultValue();
        }
        newValue = fixFormat(newValue,false);

        negotiationInfo.putClause(clause, newValue);

        //CALCULATE BROKER CURRENCY CHANGE LOSTWOOD
        final BigDecimal exchangeRate = convertToBigDecimal(clauses.get(ClauseType.EXCHANGE_RATE).getValue());
        final BigDecimal amountToBuy = convertToBigDecimal(clauses.get(ClauseType.CUSTOMER_CURRENCY_QUANTITY).getValue());
        final BigDecimal amountToPay = amountToBuy.multiply(exchangeRate);

        //ASIGNAMENT BROKER CURRENCY
        final String amountToPayStr = fixFormat(String.valueOf(amountToPay),true);
        final ClauseInformation brokerCurrencyQuantityClause = clauses.get(ClauseType.BROKER_CURRENCY_QUANTITY);
        negotiationInfo.putClause(brokerCurrencyQuantityClause, amountToPayStr);

        adapter.changeDataSet(negotiationInfo);
    }

    //ACTION LISTENER FOR CLAUSE BROKER CURRENCY
    private void actionListenerBrokerCurrency(ClauseInformation clause, Currency selectedItem) {

        final Map<ClauseType, ClauseInformation> clauses = negotiationInfo.getClauses();

        String payment = selectedItem.getCode();

        //ASIGNAMENT NEW VALUE
        negotiationInfo.putClause(clause, payment);

        //GET MARKET RATE
        BigDecimal exchangeRate = convertToBigDecimal(String.valueOf(getExchangeRate(payment)));


        //CALCULATE NEW PAY
        ClauseInformation currencyToBuyInfo = clauses.get(ClauseType.CUSTOMER_CURRENCY_QUANTITY);


        final BigDecimal amountToBuy = convertToBigDecimal(currencyToBuyInfo.getValue());
        final BigDecimal amountToPay = amountToBuy.multiply(exchangeRate);

        //ASINAMENT NEW EXCHANGE RATE
        final String amountToExchangeRateStr = fixFormat(String.valueOf(exchangeRate),true);
        final ClauseInformation exchangeRateClause = clauses.get(ClauseType.EXCHANGE_RATE);
        negotiationInfo.putClause(exchangeRateClause, amountToExchangeRateStr);

        //ASIGNAMENT NEW PAY
        // final String amountToPayStr = numberFormat.format(amountToPay);
        final String amountToPayStr = fixFormat(String.valueOf(amountToPay),true);
        final ClauseInformation brokerCurrencyQuantityClause = clauses.get(ClauseType.BROKER_CURRENCY_QUANTITY);
        negotiationInfo.putClause(brokerCurrencyQuantityClause, amountToPayStr);

        adapter.changeDataSet(negotiationInfo);


    }
    /*-------------------------------------------------------------------------------------------------
                                                END ACTION LISTENER
    ---------------------------------------------------------------------------------------------------*/
    /*-------------------------------------------------------------------------------------------------
                                                VALIDATE OF DATE
    --------------------------------------------------------------------------------------------------*/

    //VALIDATE CLAUSE
    private Boolean validateClauses(Map<ClauseType, ClauseInformation> clauses) {

        if (clauses != null) {

            final BigDecimal exchangeRate = convertToBigDecimal(clauses.get(ClauseType.EXCHANGE_RATE).getValue());

            final BigDecimal amountToBuy = convertToBigDecimal(clauses.get(ClauseType.CUSTOMER_CURRENCY_QUANTITY).getValue());

            final BigDecimal amountToPay = convertToBigDecimal(clauses.get(ClauseType.BROKER_CURRENCY_QUANTITY).getValue());

            if (exchangeRate.compareTo(BigDecimal.ZERO) <= 0) {
                Toast.makeText(getActivity(), "The exchange must be greater than zero.", Toast.LENGTH_LONG).show();
                return false;
            }

            if (amountToBuy.compareTo(BigDecimal.ZERO) <= 0) {
                Toast.makeText(getActivity(), "The  buying must be greater than zero.", Toast.LENGTH_LONG).show();
                return false;
            }

            if (amountToPay.compareTo(BigDecimal.ZERO) <= 0) {
                Toast.makeText(getActivity(), "The  paying must be greater than zero.", Toast.LENGTH_LONG).show();
                return false;
            }

            final String customerCurrencyClauseVal = clauses.get(ClauseType.CUSTOMER_CURRENCY).getValue();
            final String brokerCurrencyClauseVal = clauses.get(ClauseType.BROKER_CURRENCY).getValue();

            if (customerCurrencyClauseVal.equals(brokerCurrencyClauseVal)) {
                Toast.makeText(getActivity(), "The currency to pay is equal to currency buy.", Toast.LENGTH_LONG).show();
                return false;
            }

        } else {
            Toast.makeText(getActivity(), "Error. Information is null.", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    //VALIDATE EXCHANGE RATE NOT IS ZERO
    private boolean validateExchangeRate() {

        final Map<ClauseType, ClauseInformation> clauses = negotiationInfo.getClauses();

        final BigDecimal exchangeRate = convertToBigDecimal(clauses.get(ClauseType.EXCHANGE_RATE).getValue());

        if (exchangeRate.compareTo(BigDecimal.ZERO) <= 0) {
            Toast.makeText(getActivity(), R.string.the_exchange_rate_must_be_greater_than_zero, Toast.LENGTH_LONG).show();
            return false;
        }

        return true;

    }

    /*------------------------------------------ END VALIDATE OF DATE -------------------------------------*/

    /*------------------------------------------ OTHER METHODS ---------------------------------------------*/

    //GET CLAUSE INFORMATION
    private Collection<ClauseInformation> getClause(Map<ClauseType, ClauseInformation> mapClauses) {

        Collection<ClauseInformation> clauses = new ArrayList<>();

        if (mapClauses != null)
            for (Map.Entry<ClauseType, ClauseInformation> clauseInformation : mapClauses.entrySet())
                clauses.add(clauseInformation.getValue());

        return clauses;
    }

    //GET CLAUSE INFORMATION TEST
    @SuppressWarnings("unused")
    private String getClauseTest(Map<ClauseType, ClauseInformation> mapClauses) {

        String clauses = "";
        ClauseInformation information;

        if (mapClauses != null) {
            for (Map.Entry<ClauseType, ClauseInformation> clauseInformation : mapClauses.entrySet()) {
                information = clauseInformation.getValue();
                clauses = information.getType().getCode() + ": " + information.getValue() + ", " + clauses;
            }
        }

        return clauses;
    }

    private List<Currency> getCurrenciesFromQuotes(List<MerchandiseExchangeRate> quotes) {
        Set<Currency> data = new HashSet<>();

        for (MerchandiseExchangeRate exchangeRate : quotes) {
            data.add(exchangeRate.getPaymentCurrency());
        }

        return new ArrayList<>(data);
    }

    private String getExchangeRate(String paymentCurrencyCode) {
        for (MerchandiseExchangeRate exchangeRate : quotes) {
            Currency exchangeRatePaymentCurrency = exchangeRate.getPaymentCurrency();

            if (exchangeRatePaymentCurrency.getCode().equals(paymentCurrencyCode))
                return String.valueOf(exchangeRate.getExchangeRate());
        }

            return defaultValue();

    }

    private String fixFormat(String value,Boolean stringContainADoubleValue) {

        try {
            if (compareLessThan1(value,stringContainADoubleValue)) {
                numberFormat.setMaximumFractionDigits(8);
            } else {
                numberFormat.setMaximumFractionDigits(2);
            }
            if(stringContainADoubleValue){
                return String.valueOf(numberFormat.parse(numberFormat.format(
                        new BigDecimal(value))));
            }else{
                return String.valueOf(numberFormat.parse(numberFormat.format(
                        new BigDecimal(numberFormat.parse(value).toString()))));
            }

        } catch (ParseException e) {
            e.printStackTrace();
            return "0";
        }

    }

    private Boolean compareLessThan1(String value,Boolean StringContainADoubleValue) {
        Boolean lessThan1 = true;

        try {
            Double valueToConvert = StringContainADoubleValue ? Double.valueOf(value) : Double.valueOf(numberFormat.parse(value).doubleValue());
            lessThan1 = BigDecimal.valueOf(valueToConvert).compareTo(BigDecimal.ONE) == -1;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return lessThan1;
    }

    private BigDecimal convertToBigDecimal(String value) {

        BigDecimal conversion = new BigDecimal(0);
        try {
            numberFormat.setMaximumFractionDigits(compareLessThan1(value, true) ? 8 : 2);
            conversion = new BigDecimal(String.valueOf(numberFormat.parse(numberFormat.format(Double.valueOf(value)))));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return conversion;
    }

    private String defaultValue(){
        DecimalFormatSymbols symbols =((DecimalFormat)  numberFormat).getDecimalFormatSymbols();
        return symbols.getDecimalSeparator() == '.' ? "0.0" : "0,0";
    }
}

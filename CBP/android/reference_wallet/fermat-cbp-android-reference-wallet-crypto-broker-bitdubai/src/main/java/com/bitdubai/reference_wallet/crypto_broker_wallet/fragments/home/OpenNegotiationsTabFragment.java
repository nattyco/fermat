package com.bitdubai.reference_wallet.crypto_broker_wallet.fragments.home;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bitdubai.fermat_android_api.engine.FermatApplicationCaller;
import com.bitdubai.fermat_android_api.engine.FermatApplicationSession;
import com.bitdubai.fermat_android_api.layer.definition.wallet.interfaces.ReferenceAppFermatSession;
import com.bitdubai.fermat_android_api.ui.enums.FermatRefreshTypes;
import com.bitdubai.fermat_android_api.ui.expandableRecicler.ExpandableRecyclerAdapter;
import com.bitdubai.fermat_android_api.ui.fragments.FermatWalletExpandableListFragment;
import com.bitdubai.fermat_android_api.ui.interfaces.FermatListItemListeners;
import com.bitdubai.fermat_android_api.ui.util.FermatDividerItemDecoration;
import com.bitdubai.fermat_api.FermatBroadcastReceiver;
import com.bitdubai.fermat_api.FermatException;
import com.bitdubai.fermat_api.FermatIntentFilter;
import com.bitdubai.fermat_api.layer.all_definition.common.system.interfaces.ErrorManager;
import com.bitdubai.fermat_api.layer.all_definition.common.system.interfaces.error_manager.enums.UnexpectedUIExceptionSeverity;
import com.bitdubai.fermat_api.layer.all_definition.common.system.interfaces.error_manager.enums.UnexpectedWalletExceptionSeverity;
import com.bitdubai.fermat_api.layer.all_definition.enums.SubAppsPublicKeys;
import com.bitdubai.fermat_api.layer.all_definition.enums.UISource;
import com.bitdubai.fermat_api.layer.all_definition.navigation_structure.enums.Activities;
import com.bitdubai.fermat_api.layer.all_definition.navigation_structure.enums.Wallets;
import com.bitdubai.fermat_api.layer.osa_android.broadcaster.Broadcaster;
import com.bitdubai.fermat_api.layer.osa_android.broadcaster.BroadcasterType;
import com.bitdubai.fermat_api.layer.osa_android.broadcaster.FermatBundle;
import com.bitdubai.fermat_api.layer.pip_engine.interfaces.ResourceProviderManager;
import com.bitdubai.fermat_cbp_api.layer.wallet_module.common.interfaces.CustomerBrokerNegotiationInformation;
import com.bitdubai.fermat_cbp_api.layer.wallet_module.crypto_broker.CryptoBrokerWalletPreferenceSettings;
import com.bitdubai.fermat_cbp_api.layer.wallet_module.crypto_broker.interfaces.CryptoBrokerWalletModuleManager;
import com.bitdubai.reference_wallet.crypto_broker_wallet.R;
import com.bitdubai.reference_wallet.crypto_broker_wallet.common.adapters.OpenNegotiationsExpandableAdapter;
import com.bitdubai.reference_wallet.crypto_broker_wallet.common.models.GrouperItem;
import com.bitdubai.reference_wallet.crypto_broker_wallet.util.CommonLogger;
import com.bitdubai.reference_wallet.crypto_broker_wallet.util.FragmentsCommons;

import java.util.ArrayList;
import java.util.List;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;
import static com.bitdubai.fermat_cbp_api.all_definition.constants.CBPBroadcasterConstants.CBW_CONTRACT_UPDATE_VIEW;
import static com.bitdubai.fermat_cbp_api.all_definition.constants.CBPBroadcasterConstants.CBW_NEGOTIATION_UPDATE_VIEW;


/**
 * Fragment the show the list of open negotiations waiting for the broker and the customer un the Home activity
 *
 * @author Nelson Ramirez
 * @version 1.0
 * @since 20/10/2015
 */
public class OpenNegotiationsTabFragment extends FermatWalletExpandableListFragment<GrouperItem, ReferenceAppFermatSession<CryptoBrokerWalletModuleManager>, ResourceProviderManager>
        implements FermatListItemListeners<CustomerBrokerNegotiationInformation> {

    // Fermat Managers
    private CryptoBrokerWalletModuleManager moduleManager;
    private ErrorManager errorManager;

    // Data
    private List<GrouperItem<CustomerBrokerNegotiationInformation>> openNegotiationList;

    // Android Stuffs
    private View emptyListViewsContainer;
    private FermatApplicationCaller applicationsHelper;
    private OpenNegotiationBroadcastReceiver broadcastReceiver;


    public static OpenNegotiationsTabFragment newInstance() {
        return new OpenNegotiationsTabFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FermatIntentFilter fermatIntentFilter = new FermatIntentFilter(BroadcasterType.UPDATE_VIEW);
        broadcastReceiver = new OpenNegotiationBroadcastReceiver();
        registerReceiver(fermatIntentFilter, broadcastReceiver);

        try {
            moduleManager = appSession.getModuleManager();
            errorManager = appSession.getErrorManager();

            applicationsHelper = ((FermatApplicationSession) getActivity().getApplicationContext()).getApplicationManager();

            CryptoBrokerWalletPreferenceSettings settings = moduleManager.loadAndGetSettings(appSession.getAppPublicKey());
            if (settings.isWizardStartActivity()) {
                changeStartActivity(Activities.CBP_CRYPTO_BROKER_WALLET_HOME.getCode());

                settings.setIsWizardStartActivity(false);
                moduleManager.persistSettings(appSession.getAppPublicKey(), settings);
            }

        } catch (Exception ex) {
            CommonLogger.exception(TAG, ex.getMessage(), ex);
            if (errorManager != null)
                errorManager.reportUnexpectedWalletException(Wallets.CBP_CRYPTO_BROKER_WALLET,
                        UnexpectedWalletExceptionSeverity.DISABLES_THIS_FRAGMENT, ex);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        onRefresh();
    }

    @Override
    public void onDestroy() {
        if (broadcastReceiver != null)
            unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    @Override
    public void onFragmentFocus() {
        super.onFragmentFocus();
        if (isAttached) onRefresh();
    }

    @Override
    protected boolean hasMenu() {
        return true;
    }

    @Override
    protected void initViews(View layout) {
        super.initViews(layout);

        Activity activity = getActivity();
        configureToolbar();

        RecyclerView.ItemDecoration itemDecoration = new FermatDividerItemDecoration(activity, R.drawable.cbw_divider_shape);
        recyclerView.addItemDecoration(itemDecoration);
        emptyListViewsContainer = layout.findViewById(R.id.empty);
    }

    @SuppressWarnings("deprecation")
    private void configureToolbar() {
        Toolbar toolbar = getToolbar();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            toolbar.setBackground(getResources().getDrawable(R.drawable.cbw_action_bar_gradient_colors, null));
        else
            toolbar.setBackground(getResources().getDrawable(R.drawable.cbw_action_bar_gradient_colors));

        toolbar.setTitleTextColor(Color.WHITE);
        if (toolbar.getMenu() != null) toolbar.getMenu().clear();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            int id = item.getItemId();
            switch (id) {
                case FragmentsCommons.OPEN_BROKER_IDENTITY_APP_OPTION_MENU_ID:
                    applicationsHelper.openFermatApp(SubAppsPublicKeys.CBP_BROKER_IDENTITY.getCode());
                    break;
                case FragmentsCommons.OPEN_CUSTOMER_COMMUNITY_APP_OPTION_MENU_ID:
                    applicationsHelper.openFermatApp(SubAppsPublicKeys.CBP_CUSTOMER_COMMUNITY.getCode());
                    break;
            }

        } catch (Exception e) {
            errorManager.reportUnexpectedUIException(UISource.ACTIVITY, UnexpectedUIExceptionSeverity.UNSTABLE, FermatException.wrapException(e));
            makeText(getActivity(), getResources().getString(R.string.error_opps), LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ExpandableRecyclerAdapter getAdapter() {
        if (adapter == null) {
            adapter = new OpenNegotiationsExpandableAdapter(getActivity(), openNegotiationList);
            adapter.setChildItemFermatEventListeners(this);
        }
        return adapter;
    }

    @Override
    public RecyclerView.LayoutManager getLayoutManager() {
        if (layoutManager == null)
            layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        return layoutManager;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.cbw_fragment_open_negotiations_and_contracts_tab;
    }

    @Override
    protected int getRecyclerLayoutId() {
        return R.id.cbw_open_negotiations_and_contracts_recycler_view;
    }

    @Override
    protected int getSwipeRefreshLayoutId() {
        return R.id.swipe_refresh;
    }

    @Override
    public List<GrouperItem> getMoreDataAsync(FermatRefreshTypes refreshType, int pos) {
        ArrayList<GrouperItem> data = new ArrayList<>();
        String grouperText;

        if (moduleManager != null) {
            GrouperItem<CustomerBrokerNegotiationInformation> grouper;
            List<CustomerBrokerNegotiationInformation> waitingForBroker = new ArrayList<>();
            List<CustomerBrokerNegotiationInformation> waitingForCustomer = new ArrayList<>();

            try {

                waitingForBroker.addAll(moduleManager.getNegotiationsWaitingForBroker(0, 10));
                waitingForCustomer.addAll(moduleManager.getNegotiationsWaitingForCustomer(0, 10));

                if (!waitingForBroker.isEmpty() || !waitingForCustomer.isEmpty()) {
                    grouperText = getActivity().getString(R.string.waiting_for_you);
                    grouper = new GrouperItem<>(grouperText, waitingForBroker, true);
                    data.add(grouper);

                    grouperText = getActivity().getString(R.string.waiting_for_the_customer);
                    grouper = new GrouperItem<>(grouperText, waitingForCustomer, true);
                    data.add(grouper);
                }

            } catch (Exception ex) {
                errorManager.reportUnexpectedWalletException(Wallets.CBP_CRYPTO_BROKER_WALLET,
                        UnexpectedWalletExceptionSeverity.DISABLES_SOME_FUNCTIONALITY_WITHIN_THIS_FRAGMENT, ex);
            }

        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.error_open_negotiations_tab), Toast.LENGTH_SHORT).show();
        }

        return data;
    }

    @Override
    protected boolean recyclerHasFixedSize() {
        return true;
    }

    @Override
    public void onItemClickListener(CustomerBrokerNegotiationInformation data, int position) {
        appSession.setData(FragmentsCommons.NEGOTIATION_DATA, data);
        changeActivity(Activities.CBP_CRYPTO_BROKER_WALLET_OPEN_NEGOTIATION_DETAILS, appSession.getAppPublicKey());
    }

    @Override
    public void onLongItemClickListener(CustomerBrokerNegotiationInformation data, int position) {
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onPostExecute(Object... result) {
        isRefreshing = false;
        if (isAttached) {
            swipeRefreshLayout.setRefreshing(false);
            if (result != null && result.length > 0) {
                openNegotiationList = (ArrayList) result[0];
                if (adapter != null)
                    adapter.changeDataSet(openNegotiationList);
            }
        }
        showOrHideEmptyView();
    }

    @Override
    public void onErrorOccurred(Exception ex) {
        isRefreshing = false;
        if (isAttached) {
            swipeRefreshLayout.setRefreshing(false);
            CommonLogger.exception(TAG, ex.getMessage(), ex);
        }
    }

    private void showOrHideEmptyView() {
        if (openNegotiationList == null || openNegotiationList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyListViewsContainer.setVisibility(View.VISIBLE);
        } else {
            emptyListViewsContainer.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private class OpenNegotiationBroadcastReceiver extends FermatBroadcastReceiver {

        @Override
        public void onReceive(FermatBundle fermatBundle) {
            try {
                String code = fermatBundle.getString(Broadcaster.NOTIFICATION_TYPE);

                switch (code) {
                    case CBW_NEGOTIATION_UPDATE_VIEW:
                        if (isAttached) onRefresh();
                        break;
                    case CBW_CONTRACT_UPDATE_VIEW:
                        if (isAttached) onRefresh();
                        break;
                }

            } catch (ClassCastException e) {
                appSession.getErrorManager().reportUnexpectedWalletException(Wallets.CBP_CRYPTO_BROKER_WALLET,
                        UnexpectedWalletExceptionSeverity.DISABLES_SOME_FUNCTIONALITY_WITHIN_THIS_FRAGMENT, e);
            }
        }
    }
}
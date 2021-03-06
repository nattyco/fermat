package com.bitdubai.sub_app.intra_user_community.fragments;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bitdubai.fermat_android_api.layer.definition.wallet.AbstractFermatFragment;
import com.bitdubai.fermat_android_api.layer.definition.wallet.interfaces.ReferenceAppFermatSession;
import com.bitdubai.fermat_android_api.ui.interfaces.FermatListItemListeners;
import com.bitdubai.fermat_android_api.ui.interfaces.FermatWorkerCallBack;
import com.bitdubai.fermat_android_api.ui.util.FermatWorker;
import com.bitdubai.fermat_api.FermatException;
import com.bitdubai.fermat_api.layer.all_definition.common.system.enums.NetworkStatus;
import com.bitdubai.fermat_api.layer.all_definition.common.system.exceptions.CantGetCommunicationNetworkStatusException;
import com.bitdubai.fermat_api.layer.all_definition.common.system.interfaces.ErrorManager;
import com.bitdubai.fermat_api.layer.all_definition.common.system.interfaces.error_manager.enums.UnexpectedUIExceptionSeverity;
import com.bitdubai.fermat_api.layer.all_definition.enums.UISource;
import com.bitdubai.fermat_api.layer.all_definition.location_system.DeviceLocation;
import com.bitdubai.fermat_api.layer.all_definition.navigation_structure.enums.Activities;
import com.bitdubai.fermat_api.layer.osa_android.location_system.Location;
import com.bitdubai.fermat_api.layer.pip_engine.interfaces.ResourceProviderManager;
import com.bitdubai.fermat_ccp_api.layer.actor.intra_user.interfaces.IntraUserWalletSettings;
import com.bitdubai.fermat_ccp_api.layer.module.intra_user.exceptions.CantGetActiveLoginIdentityException;
import com.bitdubai.fermat_ccp_api.layer.module.intra_user.exceptions.CantGetIntraUsersListException;
import com.bitdubai.fermat_ccp_api.layer.module.intra_user.interfaces.IntraUserInformation;
import com.bitdubai.fermat_ccp_api.layer.module.intra_user.interfaces.IntraUserModuleManager;
import com.bitdubai.fermat_pip_api.layer.external_api.geolocation.interfaces.ExtendedCity;
import com.bitdubai.sub_app.intra_user_community.R;
import com.bitdubai.sub_app.intra_user_community.adapters.AppListAdapter;
import com.bitdubai.sub_app.intra_user_community.adapters.GeolocationAdapter;
import com.bitdubai.sub_app.intra_user_community.common.popups.ErrorConnectingFermatNetworkDialog;
import com.bitdubai.sub_app.intra_user_community.common.popups.ErrorConnectingGPSDialog;
import com.bitdubai.sub_app.intra_user_community.common.popups.GeolocationDialog;
import com.bitdubai.sub_app.intra_user_community.common.popups.SearchAliasDialog;
import com.bitdubai.sub_app.intra_user_community.common.popups.PresentationIntraUserCommunityDialog;
import com.bitdubai.sub_app.intra_user_community.constants.Constants;
import com.bitdubai.sub_app.intra_user_community.interfaces.ErrorConnectingFermatNetwork;
import com.bitdubai.sub_app.intra_user_community.util.CommonLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;

/**
 * Created by Matias Furszyfer on 15/09/15.
 * modified by Jose Manuel De Sousa Dos Santos on 08/12/2015
 */

public class ConnectionsWorldFragment extends AbstractFermatFragment<ReferenceAppFermatSession<IntraUserModuleManager>,ResourceProviderManager>  implements
        AdapterView.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener, FermatListItemListeners<IntraUserInformation>, GeolocationDialog.AdapterCallback , SearchAliasDialog.AdapterCallbackAlias {


    public static final String INTRA_USER_SELECTED = "intra_user";

    private static final int MAX = 12;
    /**
     * MANAGERS
     */
    private static IntraUserModuleManager moduleManager;
    private static ErrorManager errorManager;

    protected final String TAG = "Recycler Base";
    FermatWorker worker;
    IntraUserWalletSettings intraUserWalletSettings = null;
    private ErrorConnectingFermatNetwork errorConnectingFermatNetwork;
    private int offset = 0;
    private int mNotificationsCount = 0;
    private SearchView mSearchView;
    private AppListAdapter adapter;
    private boolean isStartList = false;
    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;
    private SwipeRefreshLayout swipeRefresh;
    private View searchView;
    // flags
    private boolean isRefreshing = false;
    private View rootView;
    private ReferenceAppFermatSession<IntraUserModuleManager> intraUserSubAppSession;
    private String searchName;
    private LinearLayout emptyView;
    private ArrayList<IntraUserInformation> lstIntraUserInformations = new ArrayList<>();

    private android.support.v7.widget.Toolbar toolbar;
    private EditText searchEditText;
    private List<IntraUserInformation> dataSetFiltered;
    private ImageView closeSearch;
    private LinearLayout searchEmptyView;
    private LinearLayout noNetworkView;
    private LinearLayout noFermatNetworkView;
    private Handler handler = new Handler();
    List<IntraUserInformation> userList = new ArrayList<>();

    private Location location;
    private double distance;
    private String alias;

    //flags
    int pastVisiblesItems, visibleItemCount, totalItemCount;



    private ExecutorService _executor;
    /**
     * Create a new instance of this fragment
     *
     * @return InstalledFragment instance object
     */
    public static ConnectionsWorldFragment newInstance() {
        return new ConnectionsWorldFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {

            _executor = Executors.newFixedThreadPool(2);

            setHasOptionsMenu(true);
            // setting up  module

            moduleManager = appSession.getModuleManager();
            errorManager = appSession.getErrorManager();

            intraUserSubAppSession = appSession;

            try {
                intraUserWalletSettings = moduleManager.loadAndGetSettings(intraUserSubAppSession.getAppPublicKey());
            } catch (Exception e) {
                intraUserWalletSettings = null;
            }

            if (intraUserSubAppSession.getAppPublicKey() != null) //the identity not exist yet
            {
                if (intraUserWalletSettings == null) {
                    intraUserWalletSettings = new IntraUserWalletSettings();
                    intraUserWalletSettings.setIsPresentationHelpEnabled(true);
                    intraUserSubAppSession.getModuleManager().persistSettings(intraUserSubAppSession.getAppPublicKey(), intraUserWalletSettings);
                }
            }

            mNotificationsCount = moduleManager.getIntraUsersWaitingYourAcceptanceCount();
            new FetchCountTask().execute();


            //consult net work status


            _executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                NetworkStatus networkStatus = null;
                                try {
                                    networkStatus = getFermatNetworkStatus();
                                } catch (CantGetCommunicationNetworkStatusException e) {
                                    e.printStackTrace();
                                }
                                switch (networkStatus) {
                                    case CONNECTED:
                                        // setUpReferences();
                                        break;
                                    case DISCONNECTED:
                                        showErrorFermatNetworkDialog();
                                        break;
                                }

                            }
                        }, 500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            //getting location and setting device locacion
            location = moduleManager.getLocationManager();

            if(location==null){
              //  showErrorGPS();
                Toast.makeText(getActivity(), "Please, turn ON your GPS", Toast.LENGTH_SHORT);
            }

               // turnGPSOn();


        } catch (Exception ex) {
            CommonLogger.exception(TAG, ex.getMessage(), ex);
            errorManager.reportUnexpectedUIException(UISource.ACTIVITY, UnexpectedUIExceptionSeverity.CRASH, ex);
        }
    }

    /**
     * Fragment Class implementation.
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        try {
            rootView = inflater.inflate(R.layout.fragment_connections_world, container, false);
            toolbar = getToolbar();
            toolbar.setTitle("Crypto wallet users");

            setUpScreen(inflater);
            searchView = inflater.inflate(R.layout.search_edit_text, null);


            //Set up RecyclerView
            setUpReferences();
            showEmpty(true, emptyView);


            //adapter.setFermatListEventListener(this);
            recyclerView = (RecyclerView) rootView.findViewById(R.id.gridView);
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    if (dy > 0) {
                        visibleItemCount = layoutManager.getChildCount();
                        totalItemCount = layoutManager.getItemCount();
                        pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();
                        offset = totalItemCount;
                        final int lastItem = pastVisiblesItems + visibleItemCount;
                        if (lastItem == totalItemCount) {
                            onRefresh();
                        }
                    }
                }
            });



        } catch (Exception ex) {
            errorManager.reportUnexpectedUIException(UISource.ACTIVITY, UnexpectedUIExceptionSeverity.CRASH, FermatException.wrapException(ex));
            Toast.makeText(getActivity().getApplicationContext(), "Oooops! recovering from system error", Toast.LENGTH_SHORT).show();
        }
        return rootView;
    }

    public void setUpReferences() {

        rootView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    worker.shutdownNow();
                    return true;
                }
                return false;
            }
        });
        searchEditText = (EditText) searchView.findViewById(R.id.search);
        closeSearch = (ImageView) searchView.findViewById(R.id.close_search);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.gridView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(getActivity(), 3, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
       // adapter = new AppListAdapter(getActivity(), lstIntraUserInformations);
        adapter = new AppListAdapter(getActivity(), lstIntraUserInformations,  appSession, moduleManager);
        recyclerView.setAdapter(adapter);
        adapter.setFermatListEventListener(this);
        swipeRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe);
        swipeRefresh.setOnRefreshListener(this);
        swipeRefresh.setColorSchemeColors(Color.BLUE, Color.BLUE);
        rootView.setBackgroundColor(Color.parseColor("#000b12"));
        emptyView = (LinearLayout) rootView.findViewById(R.id.empty_view);
        searchEmptyView = (LinearLayout) rootView.findViewById(R.id.search_empty_view);
        noNetworkView = (LinearLayout) rootView.findViewById(R.id.no_connection_view);
        noFermatNetworkView = (LinearLayout) rootView.findViewById(R.id.no_fermat_connection_view);


        if (intraUserWalletSettings.isPresentationHelpEnabled()) {
            showDialogHelp();
        } else {

            if (!isRefreshing) {
                isRefreshing = true;
              /* final ProgressDialog notificationsProgressDialog = new ProgressDialog(getActivity());
                notificationsProgressDialog.setMessage("Loading Crypto Wallet Users...");
                notificationsProgressDialog.setCancelable(false);
                notificationsProgressDialog.show();*/
                //Get Fermat User Cache List First
                worker = new FermatWorker() {
                    @Override
                    protected Object doInBackground() throws Exception {
                        return getSuggestionCache();

                    }
                };
                worker.setContext(getActivity());
                worker.setCallBack(new FermatWorkerCallBack() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void onPostExecute(Object... result) {
                      //notificationsProgressDialog.dismiss();
                        isRefreshing = false;
                        if (swipeRefresh != null)
                            swipeRefresh.setRefreshing(false);
                        if (result != null &&
                                result.length > 0) {
                            if (getActivity() != null && adapter != null) {
                                lstIntraUserInformations = (ArrayList<IntraUserInformation>) result[0];

                                if (lstIntraUserInformations != null) {

                                    if (lstIntraUserInformations.isEmpty()) {
                                        showEmpty(true, emptyView);
                                        showEmpty(false, searchEmptyView);

                                    } else {
                                        adapter.changeDataSet(lstIntraUserInformations);
                                        showEmpty(false, emptyView);
                                        showEmpty(false, searchEmptyView);
                                    }
                                } else {
                                    showEmpty(true, emptyView);
                                    showEmpty(false, searchEmptyView);
                                }

                            }
                        } else {
                            showEmpty(true, emptyView);
                            showEmpty(false, searchEmptyView);

                        }
                        //get Fermat User list
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                adapter.changeDataSet(lstIntraUserInformations);
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        onRefresh();
                                    }
                                }, 800);
                            }
                        });
                    }

                    @Override
                    public void onErrorOccurred(Exception ex) {
                       // notificationsProgressDialog.dismiss();
                        isRefreshing = false;
                        if (swipeRefresh != null)
                            swipeRefresh.setRefreshing(false);
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
                        ex.printStackTrace();

                    }
                });
                worker.execute();
            }
        }


    }

    public void showErrorNetworkDialog() {
        ErrorConnectingFermatNetworkDialog errorConnectingFermatNetworkDialog = new ErrorConnectingFermatNetworkDialog(getActivity(), intraUserSubAppSession, null);
        errorConnectingFermatNetworkDialog.setDescription("You are not connected  \n to the Fermat Network");
        errorConnectingFermatNetworkDialog.setRightButton("Connect", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        errorConnectingFermatNetworkDialog.setLeftButton("Cancel", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        errorConnectingFermatNetworkDialog.show();
    }

    public void showErrorFermatNetworkDialog() {
        final ErrorConnectingFermatNetworkDialog errorConnectingFermatNetworkDialog = new ErrorConnectingFermatNetworkDialog(getActivity(), intraUserSubAppSession, null);
        errorConnectingFermatNetworkDialog.setDescription("The access to the Fermat Network is disabled.");
        errorConnectingFermatNetworkDialog.setRightButton("Enable", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                errorConnectingFermatNetworkDialog.dismiss();
                try {
                    if (getFermatNetworkStatus() == NetworkStatus.DISCONNECTED) {
                        Toast.makeText(getActivity(), "Wait a minute please, trying to reconnect...", Toast.LENGTH_SHORT).show();
                        //getActivity().onBackPressed();
                    }
                } catch (CantGetCommunicationNetworkStatusException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        errorConnectingFermatNetworkDialog.setLeftButton("Cancel", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                errorConnectingFermatNetworkDialog.dismiss();
            }
        });
        errorConnectingFermatNetworkDialog.show();
    }

    public void showErrorGPS() {
        final ErrorConnectingGPSDialog errorConnectingGPS = new ErrorConnectingGPSDialog(getActivity(), intraUserSubAppSession, null);
        errorConnectingGPS.setDescription("Please, turn ON your GPS.");
        errorConnectingGPS.setCloseButton("Close", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                errorConnectingGPS.dismiss();
           /*     try {
                    if (getFermatNetworkStatus() == NetworkStatus.DISCONNECTED) {
                        Toast.makeText(getActivity(), "Wait a minute please, trying to reconnect...", Toast.LENGTH_SHORT).show();
                        //getActivity().onBackPressed();
                    }
                } catch (CantGetCommunicationNetworkStatusException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
            }
        });

        errorConnectingGPS.show();
    }


    @Override
    public void onRefresh() {

        //offset = 0;
        if (!isRefreshing) {
            isRefreshing = true;
            final ProgressDialog notificationsProgressDialog = new ProgressDialog(getActivity());

           /* if (offset>0) {
                notificationsProgressDialog.setMessage("Loading Crypto Wallet Users OnLine");
                notificationsProgressDialog.setCancelable(true);
                notificationsProgressDialog.show();
            }*/
            worker = new FermatWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    return getMoreData(location,distance,alias, offset);
                }
            };
            worker.setContext(getActivity());
            worker.setCallBack(new FermatWorkerCallBack() {
                @SuppressWarnings("unchecked")
                @Override
                public void onPostExecute(Object... result) {

                    //notificationsProgressDialog.dismiss();

                    isRefreshing = false;
                    if (swipeRefresh != null)
                        swipeRefresh.setRefreshing(false);
                    if (result != null &&
                            result.length > 0) {
                        if (getActivity() != null && adapter != null) {
                            lstIntraUserInformations = (ArrayList<IntraUserInformation>) result[0];

                            if (lstIntraUserInformations.isEmpty()) {
                               showEmpty(true, emptyView);
                               showEmpty(false, searchEmptyView);
                            } else {
                               // Toast.makeText(getActivity(), "Not user found.", Toast.LENGTH_SHORT).show();

                                adapter.changeDataSet(lstIntraUserInformations);
                                showEmpty(false, emptyView);
                                showEmpty(false, searchEmptyView);
                            }
                        }
                    } else {
                            showEmpty(true, emptyView);
                            showEmpty(false, searchEmptyView);

                    }
                }

                @Override
                public void onErrorOccurred(Exception ex) {

                   // notificationsProgressDialog.dismiss();

                    isRefreshing = false;
                    if (swipeRefresh != null)
                        swipeRefresh.setRefreshing(false);
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
                    ex.printStackTrace();

                }
            });
            offset=0;
            worker.execute();
        }
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
     /*  inflater.inflate(R.menu.cripto_users_menu, menu);

        try {
            final MenuItem searchItem = menu.findItem(R.id.action_search);
            menu.findItem(R.id.action_help).setVisible(true);
            menu.findItem(R.id.action_search).setVisible(true);
            searchItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            });

        } catch (Exception e) {

        }*/

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            int id = item.getItemId();

            if (id == 3)
                showDialogHelp();

            if (id == 2)
                try {
                    GeolocationDialog geolocationDialog = new GeolocationDialog(getActivity(),appSession, null, this);
                    geolocationDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    Window window = geolocationDialog.getWindow();
                    WindowManager.LayoutParams wlp = window.getAttributes();
                    wlp.gravity = Gravity.TOP;
                    //wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                    window.setAttributes(wlp);
                    //geolocationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    geolocationDialog.show();



                } catch ( Exception e) {
                    e.printStackTrace();
                }

            if (id == 1)
                searchIntraUsers();

        } catch (Exception e) {
            errorManager.reportUnexpectedUIException(UISource.ACTIVITY, UnexpectedUIExceptionSeverity.UNSTABLE, FermatException.wrapException(e));
            makeText(getActivity(), "Oooops! recovering from system error",
                    LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void searchIntraUsers()
    {

        //menu.findItem(R.id.action_help).setVisible(false);
       // menu.findItem(R.id.action_search).setVisible(false);
        toolbar = getToolbar();
        toolbar.setTitle("");
        toolbar.addView(searchView);
        if (closeSearch != null)
            closeSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  //  menu.findItem(R.id.action_help).setVisible(true);
                  //  menu.findItem(R.id.action_search).setVisible(true);
                    toolbar = getToolbar();
                    toolbar.removeView(searchView);
                    toolbar.setTitle("Crypto wallet users");
                    onRefresh();
                }
            });

        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(final CharSequence s, int start, int before, int count) {
                    if (s.length() > 0) {
                        worker = new FermatWorker() {
                            @Override
                            protected Object doInBackground() throws Exception {
                                return getQueryData(s);
                            }
                        };
                        worker.setContext(getActivity());
                        worker.setCallBack(new FermatWorkerCallBack() {
                            @SuppressWarnings("unchecked")
                            @Override
                            public void onPostExecute(Object... result) {
                                isRefreshing = false;
                                if (swipeRefresh != null)
                                    swipeRefresh.setRefreshing(false);
                                if (result != null &&
                                        result.length > 0) {
                                    if (getActivity() != null && adapter != null) {
                                        dataSetFiltered = (ArrayList<IntraUserInformation>) result[0];
                                        adapter.changeDataSet(dataSetFiltered);
                                        if (dataSetFiltered != null) {
                                            if (dataSetFiltered.isEmpty()) {
                                                showEmpty(true, searchEmptyView);
                                                showEmpty(false, emptyView);

                                            } else {
                                                showEmpty(false, searchEmptyView);
                                                showEmpty(false, emptyView);
                                            }
                                        } else {
                                            showEmpty(true, searchEmptyView);
                                            showEmpty(false, emptyView);
                                        }
                                    }
                                } else {
                                    showEmpty(true, searchEmptyView);
                                    showEmpty(false, emptyView);
                                }
                            }

                            @Override
                            public void onErrorOccurred(Exception ex) {
                                isRefreshing = false;
                                if (swipeRefresh != null)
                                    swipeRefresh.setRefreshing(false);
                                showEmpty(true, searchEmptyView);
                                if (getActivity() != null)
                                    Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
                                ex.printStackTrace();

                            }
                        });
                        worker.execute();
                    } else {
                        //  menu.findItem(R.id.action_help).setVisible(true);
                        // menu.findItem(R.id.action_search).setVisible(true);
                        toolbar = getToolbar();
                        toolbar.removeView(searchView);
                        toolbar.setTitle("Crypto wallet users");
                        onRefresh();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });


    }

    }

    private void updateNotificationsBadge(int count) {
        mNotificationsCount = count;
        if(getActivity()!=null) {
            getActivity().invalidateOptionsMenu();
        }else{
            Log.e(TAG, "updateNotificationsBadge activity null, please check this, class" + getClass().getName() + " line: " + new Throwable().getStackTrace()[0].getLineNumber());
        }
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    private synchronized List<IntraUserInformation> getQueryData(final CharSequence charSequence) {
        if (lstIntraUserInformations != null && !lstIntraUserInformations.isEmpty()) {
            if (searchEditText != null && !searchEditText.getText().toString().isEmpty()) {
                //noinspection unchecked
                /*dataSetFiltered = (List<IntraUserInformation>) CollectionUtils.find(dataSet, new org.apache.commons.collections.Predicate() {
                    @Override
                    public boolean evaluate(Object object) {
                        IntraUserInformation intraUserInformation = (IntraUserInformation) object;
                        return intraUserInformation.getName().toLowerCase().contains(charSequence);
                    }
                });*/


                dataSetFiltered = new ArrayList<IntraUserInformation>();
                for (IntraUserInformation intraUser : lstIntraUserInformations) {

                    if(intraUser.getName().toLowerCase().contains(charSequence.toString().toLowerCase()))
                        dataSetFiltered.add(intraUser);

                }


            } else {
                dataSetFiltered = null;
            }
        }
        return dataSetFiltered;
    }


    private synchronized List<IntraUserInformation> getMoreData(Location location, double distance, String alias, int offsetP) {
        List<IntraUserInformation> dataSet = new ArrayList<>();

         try {

             List<IntraUserInformation> userList = moduleManager.getSuggestionsToContact(location, distance,alias,MAX, offsetP);
             if(userList != null)
                dataSet.addAll(userList);
             else {
                 if(lstIntraUserInformations!=null) {
                     dataSet.addAll(lstIntraUserInformations);
                     getActivity().runOnUiThread(new Runnable() {
                         public void run() {
                             Toast.makeText(getActivity(), "Request User List Time Out.", Toast.LENGTH_LONG).show();
                         }
                     });
                 }else{
                     Log.e(TAG,"Request null");
                 }
             }


        } catch (CantGetIntraUsersListException e) {
             e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
             getActivity().runOnUiThread(new Runnable() {
                 public void run() {
                     Toast.makeText(getActivity(), "Request User List Time Out.", Toast.LENGTH_LONG).show();
                 }
             });
        }
        return dataSet;
    }


    private List<IntraUserInformation> getSuggestionCache() {
                List<IntraUserInformation> userCacheList = new ArrayList<>();
                try{

                    userCacheList = moduleManager.getCacheSuggestionsToContact(MAX, offset);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                return userCacheList;
    }

    @Override
    public void onItemClickListener(IntraUserInformation data, int position) {
        try {
            if (moduleManager.getActiveIntraUserIdentity() != null) {
                if (!moduleManager.getActiveIntraUserIdentity().getPublicKey().isEmpty())
                    appSession.setData(INTRA_USER_SELECTED, data);
                changeActivity(Activities.CCP_SUB_APP_INTRA_USER_COMMUNITY_CONNECTION_OTHER_PROFILE.getCode());
            }
        } catch (CantGetActiveLoginIdentityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLongItemClickListener(IntraUserInformation data, int position) {

    }


    private void showDialogHelp() {
        try {
            if (moduleManager.getActiveIntraUserIdentity() != null) {
                if (!moduleManager.getActiveIntraUserIdentity().getPublicKey().isEmpty()) {
                    PresentationIntraUserCommunityDialog presentationIntraUserCommunityDialog = new PresentationIntraUserCommunityDialog(getActivity(),
                            intraUserSubAppSession,
                            null,
                            moduleManager,
                            PresentationIntraUserCommunityDialog.TYPE_PRESENTATION_WITHOUT_IDENTITIES);
                    presentationIntraUserCommunityDialog.show();
                    presentationIntraUserCommunityDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            showCriptoUsersCache();
                            invalidate();
                        }
                    });
                } else {
                    PresentationIntraUserCommunityDialog presentationIntraUserCommunityDialog = new PresentationIntraUserCommunityDialog(getActivity(),
                            intraUserSubAppSession,
                            null,
                            moduleManager,
                            PresentationIntraUserCommunityDialog.TYPE_PRESENTATION);
                    presentationIntraUserCommunityDialog.show();
                    presentationIntraUserCommunityDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            Boolean isBackPressed = (Boolean) intraUserSubAppSession.getData(Constants.PRESENTATION_DIALOG_DISMISS);
                            if (isBackPressed != null) {
                                if (isBackPressed) {
                                    getActivity().finish();
                                }
                            } else {
                                showCriptoUsersCache();

                            }
                        }
                    });
                }
            } else {
                PresentationIntraUserCommunityDialog presentationIntraUserCommunityDialog = new PresentationIntraUserCommunityDialog(getActivity(),
                        intraUserSubAppSession,
                        null,
                        moduleManager,
                        PresentationIntraUserCommunityDialog.TYPE_PRESENTATION);
                presentationIntraUserCommunityDialog.show();
                presentationIntraUserCommunityDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        Boolean isBackPressed = null;

                            isBackPressed = (Boolean) intraUserSubAppSession.getData(Constants.PRESENTATION_DIALOG_DISMISS);

                        if (isBackPressed != null) {
                            if (isBackPressed) {
                                getActivity().onBackPressed();
                            }
                        } else
                            showCriptoUsersCache();
                    }
                });
            }
        } catch (CantGetActiveLoginIdentityException e) {
            e.printStackTrace();
        }
    }

    private void showCriptoUsersCache() {

        IntraUserModuleManager moduleManager = intraUserSubAppSession.getModuleManager();
        if(moduleManager==null){
            getActivity().onBackPressed();
        }else{
            invalidate();
        }
        if(lstIntraUserInformations !=null)
        {
            if (lstIntraUserInformations.isEmpty()) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        showEmpty(true, emptyView);
                        swipeRefresh.post(new Runnable() {
                            @Override
                            public void run() {
                                swipeRefresh.setRefreshing(true);
                                onRefresh();
                            }

                        });
                    }
                });

            } else {

                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        adapter.changeDataSet(lstIntraUserInformations);
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                onRefresh();
                            }
                        }, 500);
                    }
                });

            }
        }
        else {

            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    adapter.changeDataSet(lstIntraUserInformations);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onRefresh();
                        }
                    }, 500);
                }
            });

        }

    }

    public void showEmpty(boolean show, View emptyView) {
        Animation anim = AnimationUtils.loadAnimation(getActivity(),
                show ? android.R.anim.fade_in : android.R.anim.fade_out);
        if (show &&
                (emptyView.getVisibility() == View.GONE || emptyView.getVisibility() == View.INVISIBLE)) {
            emptyView.setAnimation(anim);
            emptyView.setVisibility(View.VISIBLE);
            if (adapter != null)
                adapter.changeDataSet(null);
        } else if (!show && emptyView.getVisibility() == View.VISIBLE) {
            emptyView.setAnimation(anim);
            emptyView.setVisibility(View.GONE);
        }

    }

    private void setUpScreen(LayoutInflater layoutInflater) throws CantGetActiveLoginIdentityException {

    }

    /*
    Sample AsyncTask to fetch the notifications count
    */
    class FetchCountTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            // example count. This is where you'd
            // query your data store for the actual count.
            return mNotificationsCount;
        }

        @Override
        public void onPostExecute(Integer count) {
            updateNotificationsBadge(count);
        }
    }

    @Override
    public void onUpdateViewOnUIThread(String code){
        try
        {
            //update intra user list
            if(code.equals("ACCEPTED_CONEXION"))
              onRefresh();

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }


    @Override
    public void onMethodCallback(ExtendedCity city) {
        location = new DeviceLocation();
        location.setLongitude((double) city.getLongitude());
        location.setLatitude((double) city.getLatitude());
        location.setAccuracy((long) distance);

        onRefresh();
/*
        greenBar = (RelativeLayout) rootView.findViewById(R.id.green_bar_layout);
        closeGreenBar = (ImageView) rootView.findViewById(R.id.close_green_bar);
        greenBarCountry = (TextView) rootView.findViewById(R.id.country_green_bar);
        greenBarCity = (TextView) rootView.findViewById(R.id.city_green_bar);

        greenBarCountry.setText(city.getCountryName());
        greenBarCity.setText(city.getName());

        //greenBar.bringToFront();
        greenBar.setVisibility(View.VISIBLE);

        location=new DeviceLocation();
        location.setLatitude((double) city.getLatitude());
        location.setLongitude((double) city.getLongitude());
        //distance=identity.getAccuracy();
        //location.setAccuracy((long) distance);


        closeGreenBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                greenBar.setVisibility(View.GONE);
                location = null;
                offset=0;
                onRefresh();
            }
        });     */
    }

    @Override
    public void onMethodCallbackAlias(String aliasSearch) {
        alias=aliasSearch;
        onRefresh();
    }

    public void turnGPSOn() {
        try{
            if(!checkGPSFineLocation() || !checkGPSCoarseLocation()){ //if gps is disabled
                if (Build.VERSION.SDK_INT < 23) {
                    if (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this.getActivity(),
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    }
                    if (ActivityCompat.checkSelfPermission(this.getActivity(),Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this.getActivity(),
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                    }
                }
                else{
                    if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        getActivity().requestPermissions(
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    }
                    if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        getActivity().requestPermissions(
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                    }
                }
            }
        }catch (Exception e){
            try{
                Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
                intent.putExtra("enabled", true);
                if (Build.VERSION.SDK_INT < 23) {
                    String provider = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                    if(!provider.contains("gps")){ //if gps is disabled
                        Toast.makeText(getActivity(), "Please, turn on your GPS", Toast.LENGTH_SHORT);
                        Intent gpsOptionsIntent = new Intent(
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(gpsOptionsIntent);
                    }
                }else {
                    String provider = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                    if(!provider.contains("gps")){ //if gps is disabled
                        Toast.makeText(getContext(), "Please, turn on your GPS", Toast.LENGTH_SHORT);
                        Intent gpsOptionsIntent = new Intent(
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(gpsOptionsIntent);
                    }
                }
            }catch(Exception ex){
                if (Build.VERSION.SDK_INT < 23) {
                    Toast.makeText(getActivity(), "Please, turn on your GPS", Toast.LENGTH_SHORT);
                }else{
                    Toast.makeText(getContext(), "Please, turn on your GPS", Toast.LENGTH_SHORT);
                }
            }
        }
    }

    private boolean checkGPSCoarseLocation() {
        String permission = "android.permission.ACCESS_COARSE_LOCATION";
        int res = getActivity().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private boolean checkGPSFineLocation() {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = getActivity().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

}





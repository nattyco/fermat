package com.bitdubai.wallet_platform_api.layer._8_communication;

import com.bitdubai.wallet_platform_api.layer._4_user.DeviceUser;

/**
 * Created by ciencias on 2/12/15.
 */
public interface OnlineConnection {


    public DeviceUser getLocalUser ();

    public DeviceUser getRemoteUser ();

    public void connect() throws CantConnectToUserException;

    public void disconnect();

}

package com.bitdubai.wallet_platform_core.layer._4_user;

import com.bitdubai.wallet_platform_api.Addon;
import com.bitdubai.wallet_platform_api.layer.CantStartLayerException;
import com.bitdubai.wallet_platform_api.layer.PlatformLayer;
import com.bitdubai.wallet_platform_api.layer._4_user.UserSubsystem;
import com.bitdubai.wallet_platform_api.layer._4_user.CantStartSubsystemException;
import com.bitdubai.wallet_platform_core.layer._4_user.device_user.DeviceUserSubsystem;

/**
 * Created by ciencias on 22.01.15.
 */
public class UserLayer implements PlatformLayer {

    Addon addon;

    public Addon getUserManager() {
        return addon;
    }

    public void start() throws CantStartLayerException {

        /**
         * Let's start the Login Subsystem;
         */
        UserSubsystem loginSubsystem = new DeviceUserSubsystem();

        try {
            loginSubsystem.start();
            addon = ((UserSubsystem) loginSubsystem).getAddon();

        } catch (CantStartSubsystemException e) {
            System.err.println("CantStartSubsystemException: " + e.getMessage());

            /**
             * The com.bitdubai.platform cannot start without performing licensing operations, that's why I throw this exception.
             */
            throw new CantStartLayerException();
        }
    }



}

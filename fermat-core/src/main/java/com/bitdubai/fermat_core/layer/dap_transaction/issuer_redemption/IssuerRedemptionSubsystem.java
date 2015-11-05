package com.bitdubai.fermat_core.layer.dap_transaction.issuer_redemption;

import com.bitdubai.fermat_api.FermatException;
import com.bitdubai.fermat_api.Plugin;
import com.bitdubai.fermat_dap_api.layer.dap_transaction.CantStartSubsystemException;
import com.bitdubai.fermat_dap_api.layer.dap_transaction.DAPTransactionSubsystem;
import com.bitdubai.fermat_dap_plugin.layer.digital_asset_transaction.asset_reception.developer.bitdubai.DeveloperBitDubai;

/**
 * Created by Manuel Perez (darkpriestrelative@gmail.com) on 31/10/15.
 */
public class IssuerRedemptionSubsystem implements DAPTransactionSubsystem {

    Plugin plugin;
    @Override
    public void start() throws CantStartSubsystemException {
        try {
            DeveloperBitDubai developerBitDubai = new DeveloperBitDubai();
            plugin = developerBitDubai.getPlugin();
        }
        catch (Exception exception)
        {
            throw new CantStartSubsystemException(FermatException.wrapException(exception),"IssuerRedemptionSubsystem","Unexpected Exception");
        }
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }
}

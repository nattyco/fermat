package com.bitdubai.fermat_bch_api.layer.crypto_network.fermat.interfaces;

import com.bitdubai.fermat_api.layer.all_definition.enums.BlockchainNetworkType;
import com.bitdubai.fermat_api.layer.all_definition.enums.CryptoCurrency;
import com.bitdubai.fermat_bch_api.layer.crypto_network.BlockchainNetworkSelector;

import org.bitcoinj.core.NetworkParameters;

/**
 * Created by rodrigo on 6/22/16.
 */
public interface FermatNetworkConfiguration {

    /**
     * The network parameters of the default networt type selected for the platform.
     */
    NetworkParameters DEFAULT_NETWORK_PARAMETERS = BlockchainNetworkSelector.getNetworkParameter(BlockchainNetworkType.getDefaultBlockchainNetworkType());

    /**
     * The crypto currency of this network
     */
    CryptoCurrency CRYPTO_CURRENCY = CryptoCurrency.FERMAT;

    /**
     * Agent name and version
     */
    String USER_AGENT_NAME = "Fermat Agent";
    String USER_AGENT_VERSION ="2.1.0";

    /**
     * amount of blocks depth to consider transaction IRReversible
     */
    int IRREVERSIBLE_BLOCK_DEPTH = 3;
    int MIN_BROADCAST_CONNECTIONS = 2;

    /**
     * Amount of Timeout minutes for broadcasting transactions
     */
    int TRANSACTION_BROADCAST_TIMEOUT = 15;

    /**
     * The minimun of Satoshis that we can send, to avoid dusty sends.
     * Dusty sends are considered invalid in the network
     */
    long MIN_ALLOWED_SATOSHIS_ON_SEND = 35430;

    /**
     * Fixed fee value for outgoing transactions
     */
    long FIXED_FEE_VALUE = 30000;
}
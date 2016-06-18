package com.bitdubai.fermat_ccp_api.layer.crypto_transaction.unhold.interfaces;

import com.bitdubai.fermat_api.layer.all_definition.enums.BlockchainNetworkType;


/**
 * Created by Franklin Marcano on 21/11/2015.
 */
public interface CryptoUnholdTransaction extends CryptoTransaction {

    /**
     * Returns the status of the transaction as defined by the CashTransactionStatus enum
     * @return      Status of the transaction
     * @see         CryptoTransactionStatus
     */
    CryptoTransactionStatus getStatus();
    void                    setStatus(CryptoTransactionStatus status);

    /**
     * Returns the timestamp when the transaction was received by the Hold Plugin
     * @return      Acknowledge timestamp
     */
    long getTimestampAcknowledged();
    void setTimestampAcknowledged(long timestampAcknowledged);

    /**
     * Returns the timestamp when the transaction was either confirmed or rejected by the Hold Plugin
     * @return      Confirm/Rejected timestamp
     */
    long getTimestampConfirmedRejected();
    void setTimestampConfirmedRejected(long timestampConfirmedRejected);

    BlockchainNetworkType getBlockChainNetworkType();

    void setBlockChainNetworkType(BlockchainNetworkType blockChainNetworkType);
}

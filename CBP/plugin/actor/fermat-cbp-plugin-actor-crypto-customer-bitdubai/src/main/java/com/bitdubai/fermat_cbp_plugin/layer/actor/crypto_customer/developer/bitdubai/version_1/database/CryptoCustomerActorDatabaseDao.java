package com.bitdubai.fermat_cbp_plugin.layer.actor.crypto_customer.developer.bitdubai.version_1.database;

import com.bitdubai.fermat_api.FermatException;
import com.bitdubai.fermat_api.layer.osa_android.database_system.Database;
import com.bitdubai.fermat_api.layer.osa_android.database_system.DatabaseFilterType;
import com.bitdubai.fermat_api.layer.osa_android.database_system.DatabaseTable;
import com.bitdubai.fermat_api.layer.osa_android.database_system.DatabaseTableRecord;
import com.bitdubai.fermat_api.layer.osa_android.database_system.PluginDatabaseSystem;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.CantCreateDatabaseException;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.CantDeleteRecordException;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.CantInsertRecordException;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.CantLoadTableToMemoryException;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.CantOpenDatabaseException;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.CantUpdateRecordException;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.DatabaseNotFoundException;
import com.bitdubai.fermat_api.layer.pip_Identity.developer.exceptions.CantGetUserDeveloperIdentitiesException;
import com.bitdubai.fermat_api.layer.actor_connection.common.enums.ConnectionState;
import com.bitdubai.fermat_cbp_api.all_definition.enums.NegotiationStatus;
import com.bitdubai.fermat_cbp_api.all_definition.identity.ActorIdentity;
import com.bitdubai.fermat_cbp_api.all_definition.negotiation.Clause;
import com.bitdubai.fermat_cbp_api.all_definition.negotiation.CustomerBrokerNegotiation;
import com.bitdubai.fermat_cbp_api.layer.actor.crypto_broker.interfaces.BrokerIdentityWalletRelationship;
import com.bitdubai.fermat_cbp_api.layer.actor.crypto_customer.exceptions.CantClosePurchaseNegotiationException;
import com.bitdubai.fermat_cbp_api.layer.actor.crypto_customer.exceptions.CantCreatePurchaseContractException;
import com.bitdubai.fermat_cbp_api.layer.actor.crypto_customer.exceptions.CantCreatePurchaseNegotiationException;
import com.bitdubai.fermat_cbp_api.layer.actor.crypto_customer.exceptions.CantDeleteCustomerIdentiyWalletRelationshipException;
import com.bitdubai.fermat_cbp_api.layer.actor.crypto_customer.exceptions.CantGetCustomerIdentiyWalletRelationshipException;
import com.bitdubai.fermat_cbp_api.layer.actor.crypto_customer.exceptions.CantGetPurchaseContractException;
import com.bitdubai.fermat_cbp_api.layer.actor.crypto_customer.exceptions.CantGetPurchaseNegotiationException;
import com.bitdubai.fermat_cbp_api.layer.actor.crypto_customer.exceptions.CantUpdatePurchaseNegotiationException;
import com.bitdubai.fermat_cbp_api.layer.actor.crypto_customer.exceptions.CantUpdateStatusPurchaseContractException;
import com.bitdubai.fermat_cbp_api.layer.actor.crypto_customer.interfaces.CryptoCustomerActor;
import com.bitdubai.fermat_cbp_api.layer.actor.crypto_customer.interfaces.CustomerIdentityWalletRelationship;
import com.bitdubai.fermat_cbp_api.layer.contract.customer_broker_purchase.interfaces.CustomerBrokerContractPurchase;
import com.bitdubai.fermat_cbp_api.layer.identity.crypto_customer.interfaces.CryptoCustomerIdentity;
import com.bitdubai.fermat_cbp_plugin.layer.actor.crypto_customer.developer.bitdubai.version_1.exceptions.CantUpdateConnectionRegisterCryptoCustomerActorException;
import com.bitdubai.fermat_cbp_plugin.layer.actor.crypto_customer.developer.bitdubai.version_1.structure.CryptoCustomerIdentityWalletRelationshipImpl;
import com.bitdubai.fermat_cbp_plugin.layer.actor.crypto_customer.developer.bitdubai.version_1.exceptions.CantInitializeCryptoCustomerActorDatabaseException;
import com.bitdubai.fermat_cbp_plugin.layer.actor.crypto_customer.developer.bitdubai.version_1.exceptions.CantRegisterCryptoCustomerActorException;
import com.bitdubai.fermat_cbp_plugin.layer.actor.crypto_customer.developer.bitdubai.version_1.exceptions.CantRegisterCryptoCustomerIdentityWalletRelationshipException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by Yordin Alayn on 15.11.15.
 */
public class CryptoCustomerActorDatabaseDao {
    private PluginDatabaseSystem pluginDatabaseSystem;

    private UUID pluginId;

    public CryptoCustomerActorDatabaseDao(PluginDatabaseSystem pluginDatabaseSystem, UUID pluginId) {
        this.pluginDatabaseSystem = pluginDatabaseSystem;
        this.pluginId = pluginId;
    }

    Database database;

    /*INITIALIZE DATABASE*/
    public void initialize() throws CantInitializeCryptoCustomerActorDatabaseException {
        try {
            database = this.pluginDatabaseSystem.openDatabase(this.pluginId, this.pluginId.toString());
        } catch (DatabaseNotFoundException e) {
            try {
                CryptoCustomerActorDatabaseFactory databaseFactory = new CryptoCustomerActorDatabaseFactory(pluginDatabaseSystem);
                database = databaseFactory.createDatabase(pluginId, pluginId.toString());
            } catch (CantCreateDatabaseException f) {
                throw new CantInitializeCryptoCustomerActorDatabaseException(CantCreateDatabaseException.DEFAULT_MESSAGE, f, "", "There is a problem and i cannot create the database.");
            } catch (Exception z) {
                throw new CantInitializeCryptoCustomerActorDatabaseException(CantOpenDatabaseException.DEFAULT_MESSAGE, z, "", "Generic Exception.");
            }
        } catch (CantOpenDatabaseException e) {
            throw new CantInitializeCryptoCustomerActorDatabaseException(CantOpenDatabaseException.DEFAULT_MESSAGE, e, "", "Exception not handled by the plugin, there is a problem and i cannot open the database.");
        } catch (Exception e) {
            throw new CantInitializeCryptoCustomerActorDatabaseException(CantOpenDatabaseException.DEFAULT_MESSAGE, e, "", "Generic Exception.");
        }
    }

    //### ACTOR ###
    //CREATE ACTOR
    public void createRegisterCryptoCustomerActor(String actorLoggedInPublicKey, String actorPublicKey, String actorName, byte[] actorPhoto, ConnectionState connectionState) throws CantRegisterCryptoCustomerActorException{
        try {
            UUID actorId = UUID.randomUUID();
            Date time = new Date();
            long timestamp = time.getTime();

            if (actorExists(actorPublicKey)){
                this.updateRegisterCryptoCustomerActor(actorLoggedInPublicKey, actorPublicKey, connectionState,timestamp);
            }else{

                DatabaseTable table = this.database.getTable(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_ACTOR_TABLE_NAME);
                DatabaseTableRecord record = table.getEmptyRecord();

                record.setUUIDValue(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_ACTOR_ACTOR_ID_COLUMN_NAME, actorId);
                record.setStringValue(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_ACTOR_PUBLIC_KEY_ACTOR_COLUMN_NAME, actorPublicKey);
                record.setStringValue(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_ACTOR_PUBLIC_KEY_IDENTITY_COLUMN_NAME, actorLoggedInPublicKey);
                record.setStringValue(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_ACTOR_NAME_ACTOR_COLUMN_NAME, actorName);
                record.setStringValue(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_ACTOR_CONNECTION_STATE_COLUMN_NAME, connectionState.getCode());
                record.setLongValue(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_ACTOR_TIMESTAMP_COLUMN_NAME, timestamp);

                table.insertRecord(record);
            }
        } catch (CantInsertRecordException e){
            throw new CantRegisterCryptoCustomerActorException (e.getMessage(), e, "Crypto Customer Actor, Customer Identity Wallet Relationship", "Cant create new Customer Identity Wallet Relationship, insert database problems.");
        } catch (CantUpdateConnectionRegisterCryptoCustomerActorException e){
            throw new CantRegisterCryptoCustomerActorException (e.getMessage(), e, "Crypto Customer Actor, Customer Identity Wallet Relationship", "Cant create new Customer Identity Wallet Relationship, insert database problems.");
        } catch (Exception e) {
            throw new CantRegisterCryptoCustomerActorException (e.getMessage(), FermatException.wrapException(e), "Crypto Customer Actor, Customer Identity Wallet Relationship", "Cant create new Customer Identity Wallet Relationship, unknown failure.");
        }
    }

    //INSERT ACTOR


    //UPDATE ACTOR
    private void updateRegisterCryptoCustomerActor(String actorLoggedInPublicKey, String actorPublicKey, ConnectionState connectionState, long timestamp) throws CantUpdateConnectionRegisterCryptoCustomerActorException{
        try {
            DatabaseTable table = this.database.getTable(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_ACTOR_TABLE_NAME);
            if (table == null)
                throw new CantGetUserDeveloperIdentitiesException("Cant get crypto customer actor, table not found.", "Crypto Customer Actor", "");

            table.setStringFilter(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_ACTOR_PUBLIC_KEY_ACTOR_COLUMN_NAME, actorPublicKey, DatabaseFilterType.EQUAL);
            table.setStringFilter(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_ACTOR_PUBLIC_KEY_IDENTITY_COLUMN_NAME, actorLoggedInPublicKey, DatabaseFilterType.EQUAL);
            table.loadToMemory();

            for (DatabaseTableRecord record : table.getRecords()) {
                record.setStringValue(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_ACTOR_CONNECTION_STATE_COLUMN_NAME, connectionState.getCode());
                record.setLongValue(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_ACTOR_TIMESTAMP_COLUMN_NAME, timestamp);
                table.updateRecord(record);
            }
        } catch (CantUpdateRecordException e){
            throw new CantUpdateConnectionRegisterCryptoCustomerActorException (e.getMessage(), e, "Crypto Customer Actor, Customer Identity Wallet Relationship", "Cant create new Customer Identity Wallet Relationship, insert database problems.");
        } catch (Exception e) {
            throw new CantUpdateConnectionRegisterCryptoCustomerActorException (e.getMessage(), FermatException.wrapException(e), "Crypto Customer Actor, Customer Identity Wallet Relationship", "Cant create new Customer Identity Wallet Relationship, unknown failure.");
        }
    }

    public CryptoCustomerActor getRegisterCryptoCustomerActor(String actorLoggedInPublicKey, String actorPublicKey) throws CantRegisterCryptoCustomerActorException {
        return null;
    }

    //### RELATIONSHIP ###
    //CREATE RELATIONSHIP
    public CustomerIdentityWalletRelationship createRegisterCustomerIdentityWalletRelationship(String walletPublicKey, String identityPublicKey) throws CantRegisterCryptoCustomerIdentityWalletRelationshipException {
        try {
            if (relationshipExists(walletPublicKey, identityPublicKey))
                throw new CantRegisterCryptoCustomerIdentityWalletRelationshipException("Cant create new Customer Identity Wallet Relationship, It exists.", "Crypto Customer Actor, Customer Identity Wallet Relationship", "Cant create new Customer Identity Wallet Relationship, Relationship exists.");

            UUID relationshipId = UUID.randomUUID();
            DatabaseTable table = this.database.getTable(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_IDENTITY_WALLET_RELATIONSHIP_TABLE_NAME);
            DatabaseTableRecord record = table.getEmptyRecord();
            record.setUUIDValue(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_IDENTITY_WALLET_RELATIONSHIP_RELATIONSHIP_ID_COLUMN_NAME, relationshipId);
            record.setStringValue(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_IDENTITY_WALLET_RELATIONSHIP_PUBLIC_KEY_WALLET_COLUMN_NAME, walletPublicKey);
            record.setStringValue(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_IDENTITY_WALLET_RELATIONSHIP_PUBLIC_KEY_IDENTITY_COLUMN_NAME, identityPublicKey);
            table.insertRecord(record);

            return new CryptoCustomerIdentityWalletRelationshipImpl(relationshipId, walletPublicKey, identityPublicKey);
        } catch (CantInsertRecordException e) {
            throw new CantRegisterCryptoCustomerIdentityWalletRelationshipException(e.getMessage(), e, "Crypto Customer Actor, Customer Identity Wallet Relationship", "Cant create new Customer Identity Wallet Relationship, insert database problems.");
        } catch (Exception e) {
            throw new CantRegisterCryptoCustomerIdentityWalletRelationshipException(e.getMessage(), FermatException.wrapException(e), "Crypto Customer Actor, Customer Identity Wallet Relationship", "Cant create new Customer Identity Wallet Relationship, unknown failure.");
        }
    }

    //UPDATE RELATIONSHIP
    public CustomerIdentityWalletRelationship updateRegisterCustomerIdentityWalletRelationship(UUID relationshipId, String walletPublicKey, String identityPublicKey) throws CantRegisterCryptoCustomerIdentityWalletRelationshipException {
        try {
            if (!relationshipExists(relationshipId))
                throw new CantRegisterCryptoCustomerIdentityWalletRelationshipException("Cant Update Customer Identity Wallet Relationship, not exists.", "Crypto Customer Actor, Customer Identity Wallet Relationship", "Cant Update Customer Identity Wallet Relationship, not exists");

            DatabaseTable table = this.database.getTable(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_IDENTITY_WALLET_RELATIONSHIP_TABLE_NAME);
            DatabaseTableRecord record = table.getEmptyRecord();
            table.setUUIDFilter(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_IDENTITY_WALLET_RELATIONSHIP_RELATIONSHIP_ID_COLUMN_NAME, relationshipId, DatabaseFilterType.EQUAL);
            record.setStringValue(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_IDENTITY_WALLET_RELATIONSHIP_PUBLIC_KEY_WALLET_COLUMN_NAME, walletPublicKey);
            record.setStringValue(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_IDENTITY_WALLET_RELATIONSHIP_PUBLIC_KEY_IDENTITY_COLUMN_NAME, identityPublicKey);
            table.updateRecord(record);

            return new CryptoCustomerIdentityWalletRelationshipImpl(relationshipId, walletPublicKey, identityPublicKey);
        } catch (CantUpdateRecordException e) {
            throw new CantRegisterCryptoCustomerIdentityWalletRelationshipException(e.getMessage(), e, "Crypto Customer Actor, Customer Identity Wallet Relationship", "Cant update Customer Identity Wallet Relationship, update database problems.");
        } catch (Exception e) {
            throw new CantRegisterCryptoCustomerIdentityWalletRelationshipException(e.getMessage(), FermatException.wrapException(e), "Crypto Customer Actor, Customer Identity Wallet Relationship", "Cant update Customer Identity Wallet Relationship, unknown failure.");
        }
    }

    //DELETE RELATIONSHIP
    public void deleteRegisterCustomerIdentityWalletRelationship(UUID relationshipId) throws CantRegisterCryptoCustomerIdentityWalletRelationshipException {
        try {
            if (!relationshipExists(relationshipId))
                throw new CantRegisterCryptoCustomerIdentityWalletRelationshipException("Cant Delete Customer Identity Wallet Relationship, not exists.", "Crypto Customer Actor, Customer Identity Wallet Relationship", "Cant Delete Customer Identity Wallet Relationship, not exists");

            DatabaseTable table = this.database.getTable(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_IDENTITY_WALLET_RELATIONSHIP_TABLE_NAME);
            DatabaseTableRecord record = table.getEmptyRecord();
            record.setUUIDValue(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_IDENTITY_WALLET_RELATIONSHIP_RELATIONSHIP_ID_COLUMN_NAME, relationshipId);
            table.deleteRecord(record);

        } catch (CantDeleteRecordException e) {
            throw new CantRegisterCryptoCustomerIdentityWalletRelationshipException(e.getMessage(), e, "Crypto Customer Actor, Customer Identity Wallet Relationship", "Cant delete Customer Identity Wallet Relationship, delete database problems.");
        } catch (Exception e) {
            throw new CantRegisterCryptoCustomerIdentityWalletRelationshipException(e.getMessage(), FermatException.wrapException(e), "Crypto Customer Actor, Customer Identity Wallet Relationship", "Cant delete Customer Identity Wallet Relationship, unknown failure.");
        }
    }

    //GET RELATIONSHIP COLLECTION
    public Collection<CustomerIdentityWalletRelationship> getAllRegisterCustomerIdentityWalletRelationships() throws CantRegisterCryptoCustomerIdentityWalletRelationshipException {
        try {
            DatabaseTable table;
            List<DatabaseTableRecord> record;
            Collection<CustomerIdentityWalletRelationship> getCollections = new ArrayList<CustomerIdentityWalletRelationship>();

            table = this.database.getTable(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_IDENTITY_WALLET_RELATIONSHIP_TABLE_NAME);
            if (table == null) {
                throw new CantGetUserDeveloperIdentitiesException("Cant check if relationship exists", "Crypto Customer Actor", "");
            }
            table.loadToMemory();
            record = table.getRecords();
            if (record.size() == 0)
                throw new CantRegisterCryptoCustomerIdentityWalletRelationshipException("The number of records with a primary key is different thatn one ", null, "The ");
            for (DatabaseTableRecord records : record) {
                getCollections.add(getCustomerIdentityWalletRelationshipFromRecord(records));
            }

            return getCollections;
        } catch (CantLoadTableToMemoryException em) {
            throw new CantRegisterCryptoCustomerIdentityWalletRelationshipException(em.getMessage(), em, "Crypto Customer Actor Identity Wallet Relationship It Already Exists", "Cant load " + CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_IDENTITY_WALLET_RELATIONSHIP_TABLE_NAME + " table in memory.");
        } catch (Exception e) {
            throw new CantRegisterCryptoCustomerIdentityWalletRelationshipException(e.getMessage(), FermatException.wrapException(e), "Crypto Customer Actor Identity Wallet Relationship It Already Exists", "unknown failure.");
        }
    }

    //GET RELATIONSHIP ID
    public CustomerIdentityWalletRelationship getAllRegisterCustomerIdentityWalletRelationships(UUID relationshipId) throws CantRegisterCryptoCustomerIdentityWalletRelationshipException {
        try {
            DatabaseTable table;
            List<DatabaseTableRecord> record;
            CustomerIdentityWalletRelationship getRelationship = null;
            table = this.database.getTable(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_IDENTITY_WALLET_RELATIONSHIP_TABLE_NAME);
            if (table == null) {
                throw new CantGetUserDeveloperIdentitiesException("Cant check if relationship exists", "Crypto Customer Actor", "");
            }
            table.setUUIDFilter(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_IDENTITY_WALLET_RELATIONSHIP_RELATIONSHIP_ID_COLUMN_NAME, relationshipId, DatabaseFilterType.EQUAL);
            table.loadToMemory();
            record = table.getRecords();
            if (record.size() == 0)
                throw new CantRegisterCryptoCustomerIdentityWalletRelationshipException("The number of records with a primary key is different thatn one ", null, "The id is: " + relationshipId.toString(), "");

            for (DatabaseTableRecord records : record) {
                getRelationship = getCustomerIdentityWalletRelationshipFromRecord(records);
            }

            return getRelationship;
        } catch (CantLoadTableToMemoryException em) {
            throw new CantRegisterCryptoCustomerIdentityWalletRelationshipException(em.getMessage(), em, "Crypto Customer Actor Identity Wallet Relationship It Already Exists", "Cant load " + CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_IDENTITY_WALLET_RELATIONSHIP_TABLE_NAME + " table in memory.");
        } catch (Exception e) {
            throw new CantRegisterCryptoCustomerIdentityWalletRelationshipException(e.getMessage(), FermatException.wrapException(e), "Crypto Customer Actor Identity Wallet Relationship It Already Exists", "unknown failure.");
        }
    }

    //GET RELATIONSHIP IDENTITY
    public CustomerIdentityWalletRelationship getAllRegisterCustomerIdentityWalletRelationshipsByIdentity(String identityPublicKey) throws CantRegisterCryptoCustomerIdentityWalletRelationshipException {
        try {
            DatabaseTable table;
            List<DatabaseTableRecord> record;
            CustomerIdentityWalletRelationship getRelationship = null;
            table = this.database.getTable(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_IDENTITY_WALLET_RELATIONSHIP_TABLE_NAME);
            if (table == null) {
                throw new CantGetUserDeveloperIdentitiesException("Cant check if relationship exists", "Crypto Customer Actor", "");
            }
            table.setStringFilter(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_IDENTITY_WALLET_RELATIONSHIP_PUBLIC_KEY_IDENTITY_COLUMN_NAME, identityPublicKey, DatabaseFilterType.EQUAL);
            table.loadToMemory();
            record = table.getRecords();
            if (record.size() == 0)
                throw new CantRegisterCryptoCustomerIdentityWalletRelationshipException("The number of records with a primary key is different thatn one ", null, "The id is: " + identityPublicKey.toString(), "");

            for (DatabaseTableRecord records : record) {
                getRelationship = getCustomerIdentityWalletRelationshipFromRecord(records);
            }

            return getRelationship;
        } catch (CantLoadTableToMemoryException em) {
            throw new CantRegisterCryptoCustomerIdentityWalletRelationshipException(em.getMessage(), em, "Crypto Customer Actor Identity Wallet Relationship It Already Exists", "Cant load " + CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_IDENTITY_WALLET_RELATIONSHIP_TABLE_NAME + " table in memory.");
        } catch (Exception e) {
            throw new CantRegisterCryptoCustomerIdentityWalletRelationshipException(e.getMessage(), FermatException.wrapException(e), "Crypto Customer Actor Identity Wallet Relationship It Already Exists", "unknown failure.");
        }
    }

    //GET RELATIONSHIP WALLET
    public CustomerIdentityWalletRelationship getAllRegisterCustomerIdentityWalletRelationshipsByWallet(String walletPublicKey) throws CantRegisterCryptoCustomerIdentityWalletRelationshipException {
        try {
            DatabaseTable table;
            List<DatabaseTableRecord> record;
            CustomerIdentityWalletRelationship getRelationship = null;
            table = this.database.getTable(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_IDENTITY_WALLET_RELATIONSHIP_TABLE_NAME);
            if (table == null) {
                throw new CantGetUserDeveloperIdentitiesException("Cant check if relationship exists", "Crypto Customer Actor", "");
            }
            table.setStringFilter(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_IDENTITY_WALLET_RELATIONSHIP_PUBLIC_KEY_WALLET_COLUMN_NAME, walletPublicKey, DatabaseFilterType.EQUAL);
            table.loadToMemory();
            record = table.getRecords();
            if (record.size() == 0)
                throw new CantRegisterCryptoCustomerIdentityWalletRelationshipException("The number of records with a primary key is different thatn one ", null, "The id is: " + walletPublicKey.toString(), "");

            for (DatabaseTableRecord records : record) {
                getRelationship = getCustomerIdentityWalletRelationshipFromRecord(records);
            }

            return getRelationship;
        } catch (CantLoadTableToMemoryException em) {
            throw new CantRegisterCryptoCustomerIdentityWalletRelationshipException(em.getMessage(), em, "Crypto Customer Actor Identity Wallet Relationship It Already Exists", "Cant load " + CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_IDENTITY_WALLET_RELATIONSHIP_TABLE_NAME + " table in memory.");
        } catch (Exception e) {
            throw new CantRegisterCryptoCustomerIdentityWalletRelationshipException(e.getMessage(), FermatException.wrapException(e), "Crypto Customer Actor Identity Wallet Relationship It Already Exists", "unknown failure.");
        }
    }

    //OTHERS
    private boolean relationshipExists(String walletPublicKey, String identityPublicKey) throws CantRegisterCryptoCustomerIdentityWalletRelationshipException {
        try {
            DatabaseTable table;
            table = this.database.getTable(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_IDENTITY_WALLET_RELATIONSHIP_TABLE_NAME);
            if (table == null) {
                throw new CantGetUserDeveloperIdentitiesException("Cant check if relationship table exists", "Crypto Customer Actor", "");
            }
            table.setStringFilter(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_IDENTITY_WALLET_RELATIONSHIP_PUBLIC_KEY_WALLET_COLUMN_NAME, walletPublicKey, DatabaseFilterType.EQUAL);
            table.setStringFilter(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_IDENTITY_WALLET_RELATIONSHIP_PUBLIC_KEY_WALLET_COLUMN_NAME, identityPublicKey, DatabaseFilterType.EQUAL);
            table.loadToMemory();
            return table.getRecords().size() > 0;
        } catch (CantLoadTableToMemoryException em) {
            throw new CantRegisterCryptoCustomerIdentityWalletRelationshipException(em.getMessage(), em, "Crypto Customer Actor Identity Wallet Relationship It Already Exists", "Cant load " + CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_IDENTITY_WALLET_RELATIONSHIP_TABLE_NAME + " table in memory.");
        } catch (Exception e) {
            throw new CantRegisterCryptoCustomerIdentityWalletRelationshipException(e.getMessage(), FermatException.wrapException(e), "Crypto Customer Actor Identity Wallet Relationship It Already Exists", "unknown failure.");
        }
    }

    private boolean relationshipExists(UUID relationshipId) throws CantRegisterCryptoCustomerIdentityWalletRelationshipException {
        try {
            DatabaseTable table;
            table = this.database.getTable(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_IDENTITY_WALLET_RELATIONSHIP_TABLE_NAME);
            if (table == null) {
                throw new CantGetUserDeveloperIdentitiesException("Cant check if relationship tablet exists", "Crypto Customer Actor", "");
            }
            table.setUUIDFilter(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_IDENTITY_WALLET_RELATIONSHIP_RELATIONSHIP_ID_COLUMN_NAME, relationshipId, DatabaseFilterType.EQUAL);
            table.loadToMemory();
            return table.getRecords().size() > 0;
        } catch (CantLoadTableToMemoryException em) {
            throw new CantRegisterCryptoCustomerIdentityWalletRelationshipException(em.getMessage(), em, "Crypto Customer Actor Identity Wallet Relationship It Already Exists", "Cant load " + CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_IDENTITY_WALLET_RELATIONSHIP_TABLE_NAME + " table in memory.");
        } catch (Exception e) {
            throw new CantRegisterCryptoCustomerIdentityWalletRelationshipException(e.getMessage(), FermatException.wrapException(e), "Crypto Customer Actor Identity Wallet Relationship It Already Exists", "unknown failure.");
        }
    }

    private boolean actorExists(String actorPublicKey) throws CantRegisterCryptoCustomerActorException{
        try {
            DatabaseTable table;
            table = this.database.getTable (CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_ACTOR_TABLE_NAME);
            if (table == null) {
                throw new CantGetUserDeveloperIdentitiesException("Cant check if actor table exists", "Crypto Customer Actor", "");
            }
            table.setStringFilter(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_ACTOR_PUBLIC_KEY_ACTOR_COLUMN_NAME, actorPublicKey, DatabaseFilterType.EQUAL);
            table.loadToMemory();
            return table.getRecords ().size () > 0;
        } catch (CantLoadTableToMemoryException em) {
            throw new CantRegisterCryptoCustomerActorException (em.getMessage(), em, "Crypto Customer Actor It Already Exists", "Cant load " + CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_ACTOR_TABLE_NAME + " table in memory.");
        } catch (Exception e) {
            throw new CantRegisterCryptoCustomerActorException (e.getMessage(), FermatException.wrapException(e), "Crypto Customer Actor It Already Exists", "unknown failure.");
        }
    }

    private CustomerIdentityWalletRelationship getCustomerIdentityWalletRelationshipFromRecord(DatabaseTableRecord record){

        UUID relationshipId = record.getUUIDValue(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_IDENTITY_WALLET_RELATIONSHIP_RELATIONSHIP_ID_COLUMN_NAME);
        String walletPublicKey = record.getStringValue(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_IDENTITY_WALLET_RELATIONSHIP_PUBLIC_KEY_WALLET_COLUMN_NAME);
        String identityPublicKey = record.getStringValue(CryptoCustomerActorDatabaseConstants.CRYPTO_CUSTOMER_IDENTITY_WALLET_RELATIONSHIP_PUBLIC_KEY_WALLET_COLUMN_NAME);

        return new CryptoCustomerIdentityWalletRelationshipImpl(relationshipId, walletPublicKey, identityPublicKey);
    }
}

package com.bitdubai.fermat_dmp_plugin.layer.middleware.wallet_factory.developer.bitdubai.version_1;

import com.bitdubai.fermat_api.CantStartPluginException;
import com.bitdubai.fermat_api.FermatException;
import com.bitdubai.fermat_api.Plugin;
import com.bitdubai.fermat_api.Service;
import com.bitdubai.fermat_api.layer.all_definition.crypto.asymmetric.interfaces.PublicKey;
import com.bitdubai.fermat_api.layer.all_definition.developer.DatabaseManagerForDevelopers;
import com.bitdubai.fermat_api.layer.all_definition.developer.DeveloperDatabase;
import com.bitdubai.fermat_api.layer.all_definition.developer.DeveloperDatabaseTable;
import com.bitdubai.fermat_api.layer.all_definition.developer.DeveloperDatabaseTableRecord;
import com.bitdubai.fermat_api.layer.all_definition.developer.DeveloperObjectFactory;
import com.bitdubai.fermat_api.layer.all_definition.developer.LogManagerForDevelopers;
import com.bitdubai.fermat_api.layer.all_definition.enums.Plugins;
import com.bitdubai.fermat_api.layer.all_definition.enums.ServiceStatus;
import com.bitdubai.fermat_api.layer.all_definition.enums.WalletCategory;
import com.bitdubai.fermat_api.layer.all_definition.enums.WalletType;
import com.bitdubai.fermat_api.layer.all_definition.resources_structure.Skin;
import com.bitdubai.fermat_api.layer.all_definition.resources_structure.enums.ScreenSize;
import com.bitdubai.fermat_api.layer.all_definition.util.Version;
import com.bitdubai.fermat_api.layer.dmp_identity.designer.exceptions.CantSingMessageException;
import com.bitdubai.fermat_api.layer.dmp_identity.designer.interfaces.DesignerIdentity;
import com.bitdubai.fermat_api.layer.dmp_identity.translator.interfaces.TranslatorIdentity;
import com.bitdubai.fermat_api.layer.dmp_middleware.wallet_factory.enums.FactoryProjectType;
import com.bitdubai.fermat_api.layer.dmp_middleware.wallet_factory.enums.WalletFactoryProjectState;
import com.bitdubai.fermat_api.layer.dmp_middleware.wallet_factory.exceptions.CantChangeProjectStateException;
import com.bitdubai.fermat_api.layer.dmp_middleware.wallet_factory.exceptions.CantCreateWalletFactoryProjectException;
import com.bitdubai.fermat_api.layer.dmp_middleware.wallet_factory.exceptions.CantDeleteWalletFactoryProjectException;
import com.bitdubai.fermat_api.layer.dmp_middleware.wallet_factory.exceptions.CantExportWalletFactoryProjectException;
import com.bitdubai.fermat_api.layer.dmp_middleware.wallet_factory.exceptions.CantGetWalletFactoryProjectException;
import com.bitdubai.fermat_api.layer.dmp_middleware.wallet_factory.exceptions.CantImportWalletFactoryProjectException;
import com.bitdubai.fermat_api.layer.dmp_middleware.wallet_factory.exceptions.CantSaveWalletFactoryProyect;
import com.bitdubai.fermat_api.layer.dmp_middleware.wallet_factory.interfaces.WalletFactoryProject;
import com.bitdubai.fermat_api.layer.dmp_middleware.wallet_factory.interfaces.WalletFactoryProjectManager;
import com.bitdubai.fermat_api.layer.dmp_network_service.wallet_store.interfaces.Language;
import com.bitdubai.fermat_api.layer.osa_android.database_system.Database;
import com.bitdubai.fermat_api.layer.osa_android.database_system.DealsWithPluginDatabaseSystem;
import com.bitdubai.fermat_api.layer.osa_android.database_system.PluginDatabaseSystem;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.CantCreateDatabaseException;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.CantOpenDatabaseException;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.DatabaseNotFoundException;
import com.bitdubai.fermat_api.layer.osa_android.file_system.DealsWithPluginFileSystem;
import com.bitdubai.fermat_api.layer.osa_android.file_system.PluginFileSystem;
import com.bitdubai.fermat_api.layer.osa_android.logger_system.DealsWithLogger;
import com.bitdubai.fermat_api.layer.osa_android.logger_system.LogLevel;
import com.bitdubai.fermat_api.layer.osa_android.logger_system.LogManager;
import com.bitdubai.fermat_dmp_plugin.layer.middleware.wallet_factory.developer.bitdubai.version_1.database.WalletFactoryMiddlewareDatabaseConstants;
import com.bitdubai.fermat_dmp_plugin.layer.middleware.wallet_factory.developer.bitdubai.version_1.database.WalletFactoryMiddlewareDatabaseFactory;
import com.bitdubai.fermat_dmp_plugin.layer.middleware.wallet_factory.developer.bitdubai.version_1.database.WalletFactoryMiddlewareDeveloperDatabaseFactory;
import com.bitdubai.fermat_dmp_plugin.layer.middleware.wallet_factory.developer.bitdubai.version_1.structure.WalletFactoryProjectMiddlewareManager;
import com.bitdubai.fermat_pip_api.layer.pip_platform_service.error_manager.DealsWithErrors;
import com.bitdubai.fermat_pip_api.layer.pip_platform_service.error_manager.ErrorManager;
import com.bitdubai.fermat_pip_api.layer.pip_platform_service.error_manager.UnexpectedPluginExceptionSeverity;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class WalletFactoryProjectMiddlewarePluginRoot implements  DatabaseManagerForDevelopers, DealsWithErrors, DealsWithLogger, DealsWithPluginDatabaseSystem, DealsWithPluginFileSystem, LogManagerForDevelopers, Plugin, Service, WalletFactoryProjectManager {
    WalletFactoryProjectMiddlewareManager walletFactoryProjectMiddlewareManager;

    /**
     * DealsWithErrors Interface member variables.
     */
    ErrorManager errorManager;

    /**
     * DealsWithLogger interface member variable
     */
    LogManager logManager;

    static Map<String, LogLevel> newLoggingLevel = new HashMap<>();

    /**
     * DealsWithPluginFileSystem Interface member variables.
     */
    PluginDatabaseSystem pluginDatabaseSystem;

    /**
     * DealsWithPluginDatabaseSystem Interface member variables.
     */
    PluginFileSystem pluginFileSystem;

    /**
     * Plugin Interface member variables.
     */
    UUID pluginId;

    /**
     * Service Interface member variables.
     */
    ServiceStatus serviceStatus = ServiceStatus.CREATED;


    /**
     * WalletFactoryProjectMiddlewareManager Interfaces member variables.
     */

    @Override
    public void start() throws CantStartPluginException {
        // I created the WalletFactoryProjectMiddlewareManager object
        walletFactoryProjectMiddlewareManager = new WalletFactoryProjectMiddlewareManager(this.pluginId, pluginDatabaseSystem, pluginFileSystem);

        // I will create the database
        try {
            Database database = pluginDatabaseSystem.openDatabase(pluginId, WalletFactoryMiddlewareDatabaseConstants.DATABASE_NAME);
            database.closeDatabase();
        } catch (CantOpenDatabaseException | DatabaseNotFoundException e) {
            try {
                WalletFactoryMiddlewareDatabaseFactory databaseFactory = new WalletFactoryMiddlewareDatabaseFactory(this.pluginDatabaseSystem);
                databaseFactory.createDatabase(this.pluginId, WalletFactoryMiddlewareDatabaseConstants.DATABASE_NAME);
            } catch (CantCreateDatabaseException cantCreateDatabaseException) {
                errorManager.reportUnexpectedPluginException(Plugins.BITDUBAI_WALLET_FACTORY_MIDDLEWARE, UnexpectedPluginExceptionSeverity.DISABLES_THIS_PLUGIN, cantCreateDatabaseException);
                throw new CantStartPluginException();
            } catch (Exception exception) {
                throw new CantStartPluginException("Cannot start WalletFactoryMiddleware plugin.", FermatException.wrapException(exception), null, null);
            }
        }

        //Start Test
        //test();

        this.serviceStatus = ServiceStatus.STARTED;
    }
    @Override
    public void pause(){
        this.serviceStatus = ServiceStatus.PAUSED;
    }

    @Override
    public void resume(){
        this.serviceStatus = ServiceStatus.STARTED;
    }

    @Override
    public void stop(){
        this.serviceStatus = ServiceStatus.STOPPED;
    }

    @Override
    public ServiceStatus getStatus() {
        return this.serviceStatus;
    }


    /**
     * DatabaseManagerForDevelopers Interface Implementation
     */
    /**
     * DatabaseManagerForDevelopers interface implementation
     * Returns the list of databases implemented on this plug in.
     */
    @Override
    public List<DeveloperDatabase> getDatabaseList(DeveloperObjectFactory developerObjectFactory) {
        WalletFactoryMiddlewareDeveloperDatabaseFactory dbFactory = new WalletFactoryMiddlewareDeveloperDatabaseFactory(pluginDatabaseSystem, pluginId);
        return dbFactory.getDatabaseList(developerObjectFactory);
    }

    /**
     * returns the list of tables for the given database
     *
     * @param developerObjectFactory
     * @param developerDatabase
     * @return
     */
    @Override
    public List<DeveloperDatabaseTable> getDatabaseTableList(DeveloperObjectFactory developerObjectFactory, DeveloperDatabase developerDatabase) {
        WalletFactoryMiddlewareDeveloperDatabaseFactory dbFactory = new WalletFactoryMiddlewareDeveloperDatabaseFactory(pluginDatabaseSystem, pluginId);
        return dbFactory.getDatabaseTableList(developerObjectFactory);
    }

    /**
     * returns the list of records for the passed table
     *
     * @param developerObjectFactory
     * @param developerDatabase
     * @param developerDatabaseTable
     * @return
     */
    @Override
    public List<DeveloperDatabaseTableRecord> getDatabaseTableContent(DeveloperObjectFactory developerObjectFactory, DeveloperDatabase developerDatabase, DeveloperDatabaseTable developerDatabaseTable) {
        WalletFactoryMiddlewareDeveloperDatabaseFactory dbFactory = new WalletFactoryMiddlewareDeveloperDatabaseFactory(pluginDatabaseSystem, pluginId);
        List<DeveloperDatabaseTableRecord> developerDatabaseTableRecordList = null;
        try {
            dbFactory.initializeDatabase();
            developerDatabaseTableRecordList = dbFactory.getDatabaseTableContent(developerObjectFactory, developerDatabaseTable);
        } catch (Exception e) {
            System.out.println("******* Error trying to get database table list for plugin Wallet Factory");
        }
        return developerDatabaseTableRecordList;
    }

    /**
     * DealsWithLogger Interface implementation.
     */

    @Override
    public void setLogManager(LogManager logManager) {
        this.logManager = logManager;
    }

    /**
     * LogManagerForDevelopers Interface implementation.
     */

    @Override
    public List<String> getClassesFullPath() {
        List<String> returnedClasses = new ArrayList<>();
        returnedClasses.add("com.bitdubai.fermat_dmp_plugin.layer.middleware.wallet_factory.developer.bitdubai.version_1.WalletFactoryProjectMiddlewarePluginRoot");

        /**
         * I return the values.
         */
        return returnedClasses;
    }


    @Override
    public void setLoggingLevelPerClass(Map<String, LogLevel> newLoggingLevel) {
        /**
         * I will check the current values and update the LogLevel in those which is different
         */

        for (Map.Entry<String, LogLevel> pluginPair : newLoggingLevel.entrySet()) {
            /**
             * if this path already exists in the Root.bewLoggingLevel I'll update the value, else, I will put as new
             */
            if (WalletFactoryProjectMiddlewarePluginRoot.newLoggingLevel.containsKey(pluginPair.getKey())) {
                WalletFactoryProjectMiddlewarePluginRoot.newLoggingLevel.remove(pluginPair.getKey());
                WalletFactoryProjectMiddlewarePluginRoot.newLoggingLevel.put(pluginPair.getKey(), pluginPair.getValue());
            } else {
                WalletFactoryProjectMiddlewarePluginRoot.newLoggingLevel.put(pluginPair.getKey(), pluginPair.getValue());
            }
        }

    }


    /**
     * DealWithErrors Interface implementation.
     */
    @Override
    public void setErrorManager(ErrorManager errorManager) {
        this.errorManager = errorManager;
    }

    /**
     * DealWithPluginDatabaseSystem Interface implementation.
     */
    @Override
    public void setPluginDatabaseSystem(PluginDatabaseSystem pluginDatabaseSystem) {
        this.pluginDatabaseSystem = pluginDatabaseSystem;
    }


    /**
     * DealWithPluginFileSystem Interface implementation.
     */
    @Override
    public void setPluginFileSystem(PluginFileSystem pluginFileSystem) {
        this.pluginFileSystem = pluginFileSystem;
    }

    /**
     * Plugin methods implementation.
     */
    @Override
    public void setId(UUID pluginId) {
        this.pluginId = pluginId;
    }

    @Override
    public WalletFactoryProject getWalletFactoryProjectByPublicKey(String publicKey) throws CantGetWalletFactoryProjectException {
        return walletFactoryProjectMiddlewareManager.getWalletFactoryProject(publicKey);
    }

    @Override
    public List<WalletFactoryProject> getWalletFactoryProjectByState(WalletFactoryProjectState walletFactoryProjectState) throws CantGetWalletFactoryProjectException {
        return walletFactoryProjectMiddlewareManager.getWalletFactoryProjectsByState(walletFactoryProjectState);
    }

    @Override
    public List<WalletFactoryProject> getAllWalletFactoryProjects() throws CantGetWalletFactoryProjectException {
        return walletFactoryProjectMiddlewareManager.getAllFactoryProjects();
    }

    /**
     * Creates and (almost) empty project and persists it.
     * @return
     * @throws CantCreateWalletFactoryProjectException
     */
    @Override
    public WalletFactoryProject createEmptyWalletFactoryProject() throws CantCreateWalletFactoryProjectException {
        return walletFactoryProjectMiddlewareManager.getNewWalletFactoryProject();
    }

    /**
     * Persists in disk and database all changes in the project
     * @param walletFactoryProject
     * @throws CantSaveWalletFactoryProyect
     */
    @Override
    public void saveWalletFactoryProjectChanges(WalletFactoryProject walletFactoryProject) throws CantSaveWalletFactoryProyect {
        try {
            walletFactoryProjectMiddlewareManager.saveWalletFactoryProject(walletFactoryProject);
        } catch (Exception exception) {
            throw new CantSaveWalletFactoryProyect(CantSaveWalletFactoryProyect.DEFAULT_MESSAGE, exception, "there was an error saving the Project information.", null);
        }
    }

    @Override
    public void deleteWalletProjectFactory(WalletFactoryProject walletFactoryProject) throws CantDeleteWalletFactoryProjectException {

    }

    @Override
    public void uploadWalletFactoryProjectToRepository(WalletFactoryProject walletFactoryProject) throws CantSaveWalletFactoryProyect {
        walletFactoryProjectMiddlewareManager.uploadWalletFactoryProjectToRepository((walletFactoryProject));
    }

    @Override
    public void exportProjectToRepository(WalletFactoryProject walletFactoryProject, String githubRepository, String userName, String password) throws CantExportWalletFactoryProjectException {
        try {
            walletFactoryProjectMiddlewareManager.exportProjectToRepository(walletFactoryProject, githubRepository, userName, password);
        } catch (Exception e) {
            throw new CantExportWalletFactoryProjectException(CantExportWalletFactoryProjectException.DEFAULT_MESSAGE, e, null, null);
        }
    }

    @Override
    public void markProkectAsPublished(WalletFactoryProject walletFactoryProject) throws CantChangeProjectStateException {
        walletFactoryProject.setProjectState(WalletFactoryProjectState.PUBLISHED);
        try {
            this.saveWalletFactoryProjectChanges(walletFactoryProject);
        } catch (Exception e) {
            throw new CantChangeProjectStateException(CantChangeProjectStateException.DEFAULT_MESSAGE, e, null, null);
        }
    }

    private void test(){
        try {
            WalletFactoryProject walletFactoryProject = createEmptyWalletFactoryProject();
            walletFactoryProject.setName("Mi primer project");
            walletFactoryProject.setWalletCategory(WalletCategory.BRANDED_REFERENCE_WALLET);
            walletFactoryProject.setDescription("Wallet Factory de prueba cargado desde el Middleware a modo de ejemplo");
            walletFactoryProject.setProjectState(WalletFactoryProjectState.CLOSED);
            walletFactoryProject.setFactoryProjectType(FactoryProjectType.WALLET);
            walletFactoryProject.setCreationTimestamp(new Timestamp(System.currentTimeMillis()));
            walletFactoryProject.setSize(300);
            walletFactoryProject.setProjectPublickKey("newPublicKey");
            walletFactoryProject.setWalletType(WalletType.REFERENCE);
            Skin skin = new Skin();
            skin.setId(UUID.randomUUID());
            skin.setName("SkinTest");
            skin.setScreenSize(ScreenSize.MEDIUM);
            skin.setDesigner(new DesignerIdentity() {
                @Override
                public String getAlias() {
                    return "diseñador";
                }

                @Override
                public String getPublicKey() {
                    return "nula";
                }

                @Override
                public String createMessageSignature(String mensage) throws CantSingMessageException {
                    return "";
                }
            });
            skin.setSize(100);
            skin.setVersion(new Version("1.0.0"));
            walletFactoryProject.setDefaultSkin(skin);

            com.bitdubai.fermat_api.layer.all_definition.resources_structure.Language language = new com.bitdubai.fermat_api.layer.all_definition.resources_structure.Language();
            language.setName("TestLanguage");
            language.setId(UUID.randomUUID());
            language.setVersion(new Version(1, 0, 0));
            language.setSize(100);
            language.setTranslator(new TranslatorIdentity() {
                @Override
                public String getAlias() {
                    return "Translator";
                }

                @Override
                public String getPublicKey() {
                    return "nula";
                }

                @Override
                public String createMessageSignature(String mensage) throws com.bitdubai.fermat_api.layer.dmp_identity.translator.exceptions.CantSingMessageException {
                    return "";
                }
            });
            walletFactoryProject.setDefaultLanguage(language);
            this.saveWalletFactoryProjectChanges(walletFactoryProject);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

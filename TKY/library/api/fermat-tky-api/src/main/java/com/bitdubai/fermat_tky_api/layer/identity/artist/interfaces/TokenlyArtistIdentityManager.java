package com.bitdubai.fermat_tky_api.layer.identity.artist.interfaces;

import com.bitdubai.fermat_api.layer.all_definition.common.system.interfaces.FermatManager;
import com.bitdubai.fermat_tky_api.all_definitions.enums.ArtistAcceptConnectionsType;
import com.bitdubai.fermat_tky_api.all_definitions.enums.ExposureLevel;
import com.bitdubai.fermat_tky_api.all_definitions.enums.ExternalPlatform;
import com.bitdubai.fermat_tky_api.all_definitions.exceptions.CantHideIdentityException;
import com.bitdubai.fermat_tky_api.all_definitions.exceptions.IdentityNotFoundException;
import com.bitdubai.fermat_tky_api.layer.identity.artist.exceptions.ArtistIdentityAlreadyExistsException;
import com.bitdubai.fermat_tky_api.layer.identity.artist.exceptions.CantCreateArtistIdentityException;
import com.bitdubai.fermat_tky_api.layer.identity.artist.exceptions.CantGetArtistIdentityException;
import com.bitdubai.fermat_tky_api.layer.identity.artist.exceptions.CantListArtistIdentitiesException;
import com.bitdubai.fermat_tky_api.layer.identity.artist.exceptions.CantUpdateArtistIdentityException;

import java.util.List;
import java.util.UUID;

/**
 * Created by Manuel Perez (darkpriestrelative@gmail.com) on 09/03/16.
 */
public interface TokenlyArtistIdentityManager extends FermatManager{

    /**
     * Through the method <code>listIdentitiesFromCurrentDeviceUser</code> we can get all the artist
     * identities linked to the current logged device user.
     * @return
     * @throws CantListArtistIdentitiesException
     */
    List<Artist> listIdentitiesFromCurrentDeviceUser() throws CantListArtistIdentitiesException;

    /**
     *
     * @param alias
     * @param profileImage
     * @param externalUserName
     * @param externalAccessToken
     * @param externalPlatform
     * @param exposureLevel
     * @param artistAcceptConnectionsType
     * @return
     * @throws CantCreateArtistIdentityException
     * @throws ArtistIdentityAlreadyExistsException
     */
    Artist createArtistIdentity(
            String alias, byte[] profileImage,
            String externalUserName, String externalAccessToken, ExternalPlatform externalPlatform,
            ExposureLevel exposureLevel, ArtistAcceptConnectionsType artistAcceptConnectionsType) throws
            CantCreateArtistIdentityException,
            ArtistIdentityAlreadyExistsException;

    /**
     *
     * @param alias
     * @param id
     * @param profileImage
     * @param externalUserName
     * @param externalAccessToken
     * @param externalPlatform
     * @param exposureLevel
     * @param artistAcceptConnectionsType
     * @throws CantUpdateArtistIdentityException
     */
    void updateArtistIdentity(
            String alias, UUID id, byte[] profileImage,
            String externalUserName, String externalAccessToken, ExternalPlatform externalPlatform,
            ExposureLevel exposureLevel, ArtistAcceptConnectionsType artistAcceptConnectionsType) throws
            CantUpdateArtistIdentityException;

    /**
     * This method returns a Artist identity
     * @param publicKey
     * @return
     * @throws CantGetArtistIdentityException
     * @throws IdentityNotFoundException
     */
    Artist getArtistIdentity(UUID publicKey) throws
            CantGetArtistIdentityException,
            IdentityNotFoundException;

    /**
     * The method <code>hideIdentity</code> is used to publish a Artist identity
     * @param publicKey
     *
     * @throws CantHideIdentityException
     * @throws IdentityNotFoundException
     */
    void hideIdentity(String publicKey) throws
            CantHideIdentityException,
            IdentityNotFoundException;

}
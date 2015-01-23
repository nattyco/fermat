package com.bitdubai.smartwallet.platform.layer._3_user;

import java.util.UUID;

/**
 * Created by ciencias on 22.01.15.
 */
public interface UserManager {

    public User getLoggedInUser();

    public User createUser() throws CantCreateUserException ;

    public void loadUser(UUID id)  throws CantLoadUserException ;

}

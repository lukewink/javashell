package com.lwink.sshterm.server.api;

public interface CredentialAuthenticator
{
  /**
   * Service used to authenticate credentials.
   * 
   * @param user The user attempting to connect.
   * @param password The user's password.
   * @return true if the credentials are authenticated.
   */
  public boolean checkCredentials(String user, String password);
}

package com.lwink.javashell.server.api;

/**
 * Used to authenticate user connections.
 */
@FunctionalInterface
public interface Authenticator
{
	boolean authenticate(String user, String password);
}

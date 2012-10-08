package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2009 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.security.cert.X509Certificate;

/**
 * @version $Id$
 */
public abstract class AbstractKeychain {
    private static Logger log = Logger.getLogger(AbstractKeychain.class);

    /**
     * @param host Hostname
     * @return the password fetched from the keychain or null if it was not found
     */
    public String find(final Host host) {
        if(log.isInfoEnabled()) {
            log.info(String.format("Fetching password from keychain for %s", host));
        }
        if(StringUtils.isEmpty(host.getHostname())) {
            log.warn("No hostname given");
            return null;
        }
        Credentials credentials = host.getCredentials();
        if(StringUtils.isEmpty(credentials.getUsername())) {
            log.warn("No username given");
            return null;
        }
        String p;
        if(credentials.isPublicKeyAuthentication()) {
            p = this.getPassword(host.getHostname(), credentials.getIdentity().getAbbreviatedPath());
            if(null == p) {
                // Interoperability with OpenSSH (ssh, ssh-agent, ssh-add)
                p = this.getPassword("SSH", credentials.getIdentity().getAbsolute());
            }
            if(null == p) {
                // Backward compatibility
                p = this.getPassword("SSHKeychain", credentials.getIdentity().getAbbreviatedPath());
            }
        }
        else {
            p = this.getPassword(host.getProtocol().getScheme().name(), host.getPort(),
                    host.getHostname(), credentials.getUsername());
        }
        if(null == p) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Password not found in keychain for %s", host));
            }
        }
        return p;
    }

    /**
     * Adds the password to the login keychain
     *
     * @param host Hostname
     * @see ch.cyberduck.core.Host#getCredentials()
     */
    public void save(final Host host) {
        if(StringUtils.isEmpty(host.getHostname())) {
            log.warn("No hostname given");
            return;
        }
        final Credentials credentials = host.getCredentials();
        if(!credentials.isUseKeychain()) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Skip writing credentials for host %s", host.getHostname()));
            }
            return;
        }
        if(StringUtils.isEmpty(credentials.getUsername())) {
            log.warn(String.format("No username in credentials for host %s", host.getHostname()));
            return;
        }
        if(StringUtils.isEmpty(credentials.getPassword())) {
            log.warn(String.format("No password in credentials for host %s", host.getHostname()));
            return;
        }
        if(credentials.isAnonymousLogin()) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Do not write anonymous credentials for host %s", host.getHostname()));
            }
            return;
        }
        if(log.isInfoEnabled()) {
            log.info(String.format("Add password for host %s", host));
        }
        if(credentials.isPublicKeyAuthentication()) {
            this.addPassword(host.getHostname(), credentials.getIdentity().getAbbreviatedPath(),
                    credentials.getPassword());
        }
        else {
            this.addPassword(host.getProtocol().getScheme().name(), host.getPort(),
                    host.getHostname(), credentials.getUsername(), credentials.getPassword());
        }
    }

    /**
     * @param protocol    Protocol scheme
     * @param port        Port
     * @param serviceName Hostname
     * @param user        Credentials
     * @return Password if found or null otherwise
     */
    public abstract String getPassword(String protocol, int port, String serviceName, String user);

    /**
     * @param serviceName Hostname
     * @param user        Credentials
     * @return Password if found or null otherwise
     */
    public abstract String getPassword(String serviceName, String user);

    /**
     * @param serviceName Hostname
     * @param user        Credentials
     * @param password    Password to save for service
     */
    public abstract void addPassword(String serviceName, String user, String password);

    /**
     * @param protocol    Scheme
     * @param port        Port
     * @param serviceName Hostname
     * @param user        Credentials
     * @param password    Password to save for service
     */
    public abstract void addPassword(String protocol, int port, String serviceName, String user, String password);

    /**
     * @param hostname Hostname
     * @param certs    Certificate chain
     * @return True if trusted in Keychain
     */
    public abstract boolean isTrusted(String hostname, X509Certificate[] certs);

    /**
     * @param certificates X.509 certificates
     * @return False if display is not possible
     */
    public abstract boolean displayCertificates(X509Certificate[] certificates);

    /**
     * Prompt user for client certificate
     *
     * @param issuers  Distinguished names
     * @param hostname Client hostname
     * @param prompt   Display in certificate choose prompt
     * @return Null if no certificate selected
     */
    public X509Certificate chooseCertificate(String[] issuers, String hostname, String prompt) {
        return null;
    }
}

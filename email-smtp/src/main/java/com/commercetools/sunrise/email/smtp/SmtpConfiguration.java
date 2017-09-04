package com.commercetools.sunrise.email.smtp;

import javax.annotation.Nonnull;
import java.util.concurrent.Executor;

/**
 * Instances of this class specify how to connect to an SMTP server with a username and password.
 *
 * @see SmtpAuthEmailSender#SmtpAuthEmailSender(SmtpConfiguration, Executor, int)
 */
public class SmtpConfiguration {

    /**
     * How to secure the connection to an SMTP server.
     */
    public enum TransportSecurity {

        /**
         * This mode shall only be used for automatic tests. A plain-text connection will be used to communicate to the
         * server.
         */
        None,

        /**
         * The connection will be established using SSL/TLS right from the start. If this mode is used, the SMTP client
         * needs to be configured to connect to the dedicated port at which the server listens for SSL/TLS connections.
         */
        SSL_TLS,

        /**
         * A plain-text connection will be established and be switched to TLS. SMTP servers supporting this mode listen
         * on a single port only instead of opening a second secured port like in the case of {@link #SSL_TLS}.
         * An SMTP client using this transport security should require the switch to TLS and
         * should not continue if the plain-text connection cannot be switched to TLS.
         */
        STARTTLS

    }

    @Nonnull
    private final String host;

    private final int port;

    @Nonnull
    private final TransportSecurity transportSecurity;

    @Nonnull
    private final String username;

    @Nonnull
    private final String password;

    /**
     * Create a configuration that specifies how to connect to an SMTP server with the given username and password.
     *
     * @param host              the name of the host running the SMTP server to use
     * @param port              the port of the SMTP server to use
     * @param transportSecurity how to secure the SMTP connection
     * @param username          the username to use for authenticating with the SMTP server
     * @param password          the password to authenticate with
     */
    public SmtpConfiguration(@Nonnull final String host, final int port,
                             @Nonnull final TransportSecurity transportSecurity,
                             @Nonnull final String username, @Nonnull final String password) {
        this.host = host;
        this.port = port;
        this.transportSecurity = transportSecurity;
        this.username = username;
        this.password = password;
    }

    /**
     * @return the name of the host running the SMTP server to use
     */
    public String getHost() {
        return host;
    }

    /**
     * @return the port of the SMTP server to use
     */
    public int getPort() {
        return port;
    }

    /**
     * @return how to secure the SMTP connection
     */
    public TransportSecurity getTransportSecurity() {
        return transportSecurity;
    }

    /**
     * @return the username to use for authenticating with the SMTP server
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return the password to authenticate with
     */
    public String getPassword() {
        return password;
    }
}

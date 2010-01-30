package org.iterx.sora.io;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Uri {

    private static final Pattern PATTERN = Pattern.compile("^([a-zA-Z][^:]*)?:?((//((([^@/:#]*)@)?([^:/#]*):?(\\d*)?)?)?/?([^\\?#]*)?(\\?([^#]*))?)?#?(.*)?$");

    private final String uri;

    private final String scheme;
    private final String schemeSpecific;
    private final String fragment;

    private final String authority;
    private final String userInfo;
    private final String host;
    private final int port;

    private final String path;
    private final String query;

    public Uri(final String uri) {
        final Matcher matcher = PATTERN.matcher(uri);
        if(matcher.matches())
        {
            this.scheme = matcher.group(1);
            this.schemeSpecific = matcher.group(2);
            this.fragment = matcher.group(12);
            this.authority = matcher.group(4);
            this.userInfo = matcher.group(6);
            this.host = matcher.group(7);
            this.port = toPort(matcher.group(8));
            this.path = matcher.group(9);
            this.query = matcher.group(11);
            this.uri = uri;
            return;
        }
        throw new IllegalArgumentException("Invalid uri '" + uri + "'");
    }

    public String getUri() {
        return uri;
    }

    public String getSchemeSpecific() {
        return schemeSpecific;
    }

    public String getScheme() {
        return scheme;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getAuthority() {
        return authority;
    }

    public String getPath() {
        return path;
    }

    public String getQuery() {
        return query;
    }

    public String getFragment() {
        return fragment;
    }

    public boolean isAbsolute() {
        throw new UnsupportedOperationException();
    }

    public boolean isOpaque() {
        throw new UnsupportedOperationException();
    }

    public Uri resolve(final Uri uri) {
        throw new UnsupportedOperationException();
    }

    public Uri relativise(final Uri uri)  {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return uri;
    }

    private static int toPort(final String port) {
        return (port != null && port.length() != 0)? Integer.parseInt(port) : -1;
    }
}

package org.iterx.sora.io.connector.session.http;

import org.iterx.sora.io.Uri;
import org.iterx.sora.io.connector.Connector;
import org.iterx.sora.io.connector.endpoint.AcceptorEndpoint;
import org.iterx.sora.io.connector.endpoint.ConnectorEndpoint;
import org.iterx.sora.io.connector.session.AbstractSession;
import org.iterx.sora.io.connector.session.Channel;
import org.iterx.sora.io.connector.session.Session;
import org.iterx.sora.io.connector.session.http.message.HttpMessage;

import java.nio.ByteBuffer;

public final class HttpSession<R extends HttpMessage, W extends HttpMessage> extends AbstractSession<HttpChannel<R, W>, R, W> {

    private final SessionCallback<? super HttpSession<R, W>> sessionCallback;
    private final DelegateSession delegateSession;

    public HttpSession(final Connector connector,
                       final SessionCallback<? super HttpSession<R, W>> sessionCallback,
                       final ConnectorEndpoint connectorEndpoint) {
        this.sessionCallback = sessionCallback;
        this.delegateSession = new DelegateSession(connector, newDelegateConnectorEndpoint(connectorEndpoint));
    }

    public HttpSession(final Connector connector,
                       final SessionCallback<? super HttpSession<R, W>> sessionCallback,
                       final AcceptorEndpoint acceptorEndpoint) {
        this.sessionCallback = sessionCallback;
        this.delegateSession = new DelegateSession(connector, newDelegateAcceptorConnector(acceptorEndpoint));
    }

    public HttpChannel<R, W> newChannel(final Channel.ChannelCallback<? super HttpChannel<R, W>, R, W> channelCallback) {
        assertState(State.OPENED);
        return new HttpChannel<R, W>(delegateSession, channelCallback);
    }

    @Override
    protected State onOpening() {
        delegateSession.open();
        return null;
    }

    @Override
    protected State onOpen() {
        sessionCallback.onOpen(this);
        return super.onOpen();
    }

    @Override
    protected State onClosing() {
        delegateSession.close();
        return null;
    }

    @Override
    protected State onClose() {
        sessionCallback.onClose(this);
        return super.onClose();
    }

    @Override
    protected State onAbort(final Throwable throwable) {
        sessionCallback.onAbort(this, throwable);
        return super.onAbort(throwable);
    }

    private void doAccept(){
        sessionCallback.onAccept(this);
    }

    private ConnectorEndpoint newDelegateConnectorEndpoint(final ConnectorEndpoint connectorEndpoint) {
        final Uri uri = connectorEndpoint.getUri();
        return new ConnectorEndpoint(new Uri("tcp://" + uri.getHost() + ":" + uri.getPort()));
    }

    private AcceptorEndpoint newDelegateAcceptorConnector(final AcceptorEndpoint acceptorEndpoint) {
        final Uri uri = acceptorEndpoint.getUri();
        return new AcceptorEndpoint(new Uri("tcp://" + uri.getHost() + ":" + uri.getPort()));
    }

    private class DelegateSession implements Session<Channel<ByteBuffer, ByteBuffer>, ByteBuffer, ByteBuffer>,
                                             SessionCallback<Session<?, ByteBuffer, ByteBuffer>> {

        private final Session<?, ByteBuffer, ByteBuffer> session;

        private DelegateSession(final Connector connector,
                                final ConnectorEndpoint connectorEndpoint) {
            session = connector.<Session<Channel<ByteBuffer, ByteBuffer>, ByteBuffer, ByteBuffer>>newSession(this, connectorEndpoint);
        }

        private DelegateSession(final Connector connector,
                                final AcceptorEndpoint acceptorEndpoint) {
            session = connector.<Session<Channel<ByteBuffer, ByteBuffer>, ByteBuffer, ByteBuffer>>newSession(this, acceptorEndpoint);
        }

        public Session<Channel<ByteBuffer, ByteBuffer>, ByteBuffer, ByteBuffer> open() {
            session.open();
            return this;
        }

        public Channel<ByteBuffer, ByteBuffer> newChannel(final Channel.ChannelCallback<? super Channel<ByteBuffer, ByteBuffer>, ByteBuffer, ByteBuffer> channelCallback) {
            return session.newChannel(channelCallback);
        }

        public Session<Channel<ByteBuffer, ByteBuffer>, ByteBuffer, ByteBuffer> close() {
            session.close();
            return this;
        }

        public void onOpen(final Session<?, ByteBuffer, ByteBuffer> session) {
            changeState(State.OPENED);
        }

        public void onAccept(final Session<?, ByteBuffer, ByteBuffer> session) {
            doAccept();
        }

        public void onClose(final Session<?, ByteBuffer, ByteBuffer> session) {
            changeState(isState(State.OPENED)? State.CLOSING : State.CLOSED);
        }

        public void onAbort(final Session<?, ByteBuffer, ByteBuffer> session, final Throwable throwable) {
            changeState(State.ABORTED, throwable);
        }
    }
}

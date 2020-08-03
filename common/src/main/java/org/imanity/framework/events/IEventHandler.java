package org.imanity.framework.events;

import org.imanity.framework.redis.server.ImanityServer;
import org.imanity.framework.redis.server.enums.ServerState;

public interface IEventHandler {

    void onServerStateChanged(ImanityServer server, ServerState oldState, ServerState newState);

}
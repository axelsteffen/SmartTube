package com.liskovsoft.plexapi.server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.liskovsoft.plexapi.network.dto.PlexResource;
import com.liskovsoft.plexapi.network.dto.PlexResourceConnection;

import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PlexServerImplTest {
    private static final Gson GSON = new Gson();
    private static final Type CONNECTIONS_TYPE = new TypeToken<List<PlexResourceConnection>>() {}.getType();

    @Test
    public void pickBaseUrl_prefersLocalHttps() {
        List<PlexResourceConnection> connections = GSON.fromJson("["
                + "{\"protocol\":\"http\",\"uri\":\"http://remote.example:32400\",\"local\":false},"
                + "{\"protocol\":\"https\",\"uri\":\"https://192.168.1.10:32400\",\"local\":true},"
                + "{\"protocol\":\"http\",\"uri\":\"http://192.168.1.10:32400\",\"local\":true}"
                + "]", CONNECTIONS_TYPE);

        assertEquals("https://192.168.1.10:32400", PlexServerImpl.pickBaseUrl(connections));
    }

    @Test
    public void pickBaseUrl_prefersLocalOverRemoteHttps() {
        List<PlexResourceConnection> connections = GSON.fromJson("["
                + "{\"protocol\":\"https\",\"uri\":\"https://remote.example:32400\",\"local\":false},"
                + "{\"protocol\":\"http\",\"uri\":\"http://192.168.1.10:32400\",\"local\":true}"
                + "]", CONNECTIONS_TYPE);

        assertEquals("http://192.168.1.10:32400", PlexServerImpl.pickBaseUrl(connections));
    }

    @Test
    public void pickBaseUrl_returnsNullWhenEmpty() {
        assertNull(PlexServerImpl.pickBaseUrl(null));
        assertNull(PlexServerImpl.pickBaseUrl(Collections.emptyList()));
    }

    @Test
    public void fromResource_skipsNonServer() {
        PlexResource resource = GSON.fromJson(
                "{\"name\":\"Phone\",\"product\":\"Plex for Android\",\"provides\":\"client\","
                        + "\"connections\":[{\"uri\":\"http://1.2.3.4:32400\",\"local\":true}]}",
                PlexResource.class);
        assertNull(PlexServerImpl.fromResource(resource));
    }

    @Test
    public void fromResource_mapsServer() {
        PlexResource resource = GSON.fromJson(
                "{\"name\":\"Home\",\"product\":\"Plex Media Server\",\"provides\":\"server\","
                        + "\"clientIdentifier\":\"srv-1\",\"owned\":true,\"presence\":true,"
                        + "\"accessToken\":\"server-token\","
                        + "\"connections\":["
                        + "{\"protocol\":\"https\",\"uri\":\"https://10.0.0.2:32400\",\"local\":true}"
                        + "]}",
                PlexResource.class);

        PlexServerImpl server = PlexServerImpl.fromResource(resource);
        assertNotNull(server);
        assertEquals("srv-1", server.getClientIdentifier());
        assertEquals("Home", server.getName());
        assertEquals("https://10.0.0.2:32400/", server.getBaseUrl());
        assertEquals("server-token", server.getAccessToken());
        assertTrue(server.isOwned());
        assertTrue(server.isOnline());
    }
}

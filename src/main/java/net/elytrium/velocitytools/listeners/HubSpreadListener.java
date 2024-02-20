/*
 * Copyright (C) 2021 - 2024 Elytrium
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.elytrium.velocitytools.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.elytrium.velocitytools.VelocityTools;
import net.elytrium.velocitytools.handlers.HubSpreadHandler;
import net.kyori.adventure.text.Component;

public final class HubSpreadListener {
    private final HubSpreadHandler spreadHandler;
    private final VelocityTools plugin;
    private boolean connectionDenied;

    public HubSpreadListener(VelocityTools plugin) {
        this.plugin = plugin;
        this.spreadHandler = new HubSpreadHandler(plugin);
    }

    @Subscribe
    public void chooseInitialServerConnection(ServerPreConnectEvent event) {
        if (connectionDenied) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            return;
        }

        RegisteredServer selectedServer = spreadHandler.firstAvailableHub();

        // Check if a lobby server is available
        if (selectedServer != null) {
            // Attempt to connect to the selected lobby server
            event.setResult(ServerPreConnectEvent.ServerResult.allowed(selectedServer));
        } else {
            // No lobby server available, disconnect the player
            connectToNextAvailableServer(event.getPlayer());
        }
    }


    private void connectToNextAvailableServer(Player player) {
        RegisteredServer nextServer = spreadHandler.nextAvailableHub();

        // Check if a next lobby server is available
        if (nextServer != null) {
            // Attempt to connect to the next lobby server
            player.createConnectionRequest(nextServer).fireAndForget();
        } else {
            // No lobby server available, disconnect the player
            VelocityTools.getLogger().info("All lobby servers unavailable. Disconnecting the player.");
            player.disconnect(Component.text("All lobby servers are currently unavailable. Please try again later."));
        }
    }


    @Subscribe
    public void onKickedFromServer(KickedFromServerEvent event) {
        Player player = event.getPlayer();
        connectToNextAvailableServer(player);
    }
}

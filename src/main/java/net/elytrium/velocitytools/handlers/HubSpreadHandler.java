/*
 * Copyright (C) 2021 - 2023 Elytrium
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

package net.elytrium.velocitytools.handlers;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.elytrium.velocitytools.Settings;
import net.elytrium.velocitytools.VelocityTools;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HubSpreadHandler
{
  private final List<RegisteredServer> hubServers = new ArrayList<>();
  
  private final ProxyServer proxyServer;
  
  private final SpreadMethod spreadMethod;
  private int lastUsed = 0;
  
  public HubSpreadHandler(VelocityTools plugin) {
    this.proxyServer = plugin.getServer();
    
    this.spreadMethod = Objects.<SpreadMethod>requireNonNull(SpreadMethod.valueOf(Settings.IMP.TOOLS.HUB_SPREAD.HUB_SPREAD_METHOD), "Specified hub spread method is invalid, the methods are: LOWEST, CYCLE");
    
    this.hubServers.addAll((Collection<? extends RegisteredServer>)Settings.IMP.TOOLS.HUB_SPREAD.SERVERS.stream()
        .map(name -> (RegisteredServer)this.proxyServer.getServer(name).orElse(null))
        .filter(Objects::nonNull)
        .collect(Collectors.toList()));
  }
  
  public RegisteredServer firstAvailableHub() {
    return firstAvailableHub(this.spreadMethod);
  }
  
  public RegisteredServer firstAvailableHub(SpreadMethod spreadMethod) {
    if (spreadMethod == SpreadMethod.LOWEST) {
      return this.hubServers.stream()
        .filter(server -> !server.ping().isCompletedExceptionally())
        .min(Comparator.comparingInt(server -> server.getPlayersConnected().size()))
        .orElse(null);
    }
    
    this.lastUsed++;
    if (this.lastUsed >= this.hubServers.size()) this.lastUsed = 0;
    
    return this.hubServers.get(this.lastUsed);
  }
  
  public boolean isHub(RegisteredServer registeredServer) {
    Objects.requireNonNull(registeredServer); return this.hubServers.stream().anyMatch(registeredServer::equals);
  }
  
  public List<RegisteredServer> getHubServers() {
    return this.hubServers;
  }
  
  public enum SpreadMethod {
    LOWEST,
    CYCLE;
  }
}

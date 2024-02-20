package net.elytrium.velocitytools.handlers;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.elytrium.velocitytools.Settings;
import net.elytrium.velocitytools.VelocityTools;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HubSpreadHandler {
    private final List<RegisteredServer> hubServers = new ArrayList<>();
    private final ProxyServer proxyServer;
    private final BalancingType balancingType;
    private int cycleCounter = 0;

    public HubSpreadHandler(VelocityTools plugin) {
        this.proxyServer = plugin.getServer();
        this.balancingType = BalancingType.valueOf(
                Settings.IMP.TOOLS.HUB_SPREAD.HUB_SPREAD_METHOD.toUpperCase());

        this.hubServers.addAll(Settings.IMP.TOOLS.HUB_SPREAD.SERVERS.stream()
                .map(name -> this.proxyServer.getServer(name).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    public RegisteredServer firstAvailableHub() {
        switch (balancingType) {
            case CYCLE:
                return cycleBalancing();
            case LOWEST:
                return lowestBalancing();
            default:
                throw new IllegalArgumentException("Invalid balancing type");
        }
    }

    public RegisteredServer nextAvailableHub() {
        switch (balancingType) {
            case CYCLE:
                return nextCycleServer();
            case LOWEST:
                return lowestBalancing();
            default:
                throw new IllegalArgumentException("Invalid balancing type");
        }
    }

    private RegisteredServer cycleBalancing() {
        RegisteredServer selectedServer = hubServers.get(cycleCounter);
        cycleCounter = (cycleCounter + 1) % hubServers.size();
        return selectedServer;
    }

    private RegisteredServer nextCycleServer() {
        RegisteredServer selectedServer = hubServers.get(cycleCounter);
        cycleCounter = (cycleCounter + 1) % hubServers.size();
        return selectedServer;
    }

    private RegisteredServer lowestBalancing() {
        return hubServers.stream()
                .min(Comparator.comparingInt(server -> server.getPlayersConnected().size()))
                .orElse(null);
    }

    public enum BalancingType {
        CYCLE,
        LOWEST
    }

    public BalancingType getBalancingType() {
        return balancingType;
    }
}

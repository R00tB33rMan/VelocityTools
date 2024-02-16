package net.elytrium.velocitytools.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.elytrium.velocitytools.Settings;
import net.elytrium.velocitytools.VelocityTools;
import net.kyori.adventure.text.Component;

import java.util.Optional;

public class PingCommand implements SimpleCommand {

    private final ProxyServer server;
    private final String playerNotFound;

    public PingCommand(ProxyServer server) {
        this.server = server;
        playerNotFound = Settings.IMP.COMMANDS.PING_CMD.PLAYER_NOT_FOUND;
    }

    @Override
    public void execute(Invocation invocation) {
        // Get the command source (who executed the command)
        CommandSource source = invocation.source();

        // Check if the command source is a player
        if (source instanceof Player) {
            Player player = (Player) source;
            long ping;

            String[] args = invocation.arguments();
            if (args.length > 0) {
                // Arguments provided, check ping of the specified user
                String targetName = args[0];
                Optional<Player> optionalTarget = server.getPlayer(targetName);
                if (optionalTarget.isPresent()) {
                    Player target = optionalTarget.get();
                    ping = getPlayerPing(target);

                    String pingMessage = Settings.IMP.COMMANDS.PING_CMD.PING_OTHER.replace(
                            "{ping}", String.valueOf(ping)
                    ).replace("{player}", target.getUsername());
                    source.sendMessage(VelocityTools.getSerializer().deserialize(pingMessage));
                } else {
                    String notFoundMessage = playerNotFound.replace("{player}", targetName);
                    source.sendMessage(VelocityTools.getSerializer().deserialize(notFoundMessage));
                }
            } else {
                // No arguments provided, check own ping
                ping = getPlayerPing(player);
                String pingMessage = Settings.IMP.COMMANDS.PING_CMD.PING_SELF.replace("{ping}", String.valueOf(ping));
                source.sendMessage(VelocityTools.getSerializer().deserialize(pingMessage));

            }
        } else {
            // The command was executed by the console or a command block
            source.sendMessage(Component.text("This command can only be executed by a player."));
        }
    }

    // Method to get player ping
    private long getPlayerPing(Player player) {
        // Get the player's ping
        return player.getPing(); // Return -1 if ping is not available
    }
}

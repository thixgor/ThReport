package com.spirit.threport.commands;

import com.spirit.threport.ThReport;
import com.spirit.threport.chat.ReportsChatInterface;
import com.spirit.threport.gui.ReportsGUI;
import com.spirit.threport.managers.LanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Comando para visualizar todas as denúncias
 */
public class ReportsCommand implements CommandExecutor {

    private final ThReport plugin;
    private final LanguageManager lang;
    private final Map<UUID, ReportsChatInterface> activeChatInterfaces;

    /**
     * Construtor
     * @param plugin Instância do plugin
     */
    public ReportsCommand(ThReport plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLanguageManager();
        this.activeChatInterfaces = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Verificar se o comando foi executado por um jogador
        if (!(sender instanceof Player)) {
            sender.sendMessage(lang.getMessage((Player) null, "geral.only_players"));
            return true;
        }

        Player player = (Player) sender;

        // Verificar permissão
        if (!player.hasPermission("threport.reports")) {
            player.sendMessage(lang.getMessage(player, "geral.sem_permissao"));
            return true;
        }

        // Verificar subcomandos para interface de chat
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("view") && args.length >= 2) {
                try {
                    int reportId = Integer.parseInt(args[1]);
                    ReportsChatInterface chatInterface = new ReportsChatInterface(plugin, player);
                    activeChatInterfaces.put(player.getUniqueId(), chatInterface);
                    chatInterface.showReportDetails(reportId);
                    return true;
                } catch (NumberFormatException e) {
                    player.sendMessage(lang.getMessage(player, "geral.prefixo") + "ID de denúncia inválido.");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("accept") && args.length >= 2) {
                try {
                    int reportId = Integer.parseInt(args[1]);
                    if (activeChatInterfaces.containsKey(player.getUniqueId())) {
                        activeChatInterfaces.get(player.getUniqueId()).acceptReport(reportId);
                    } else {
                        ReportsChatInterface chatInterface = new ReportsChatInterface(plugin, player);
                        chatInterface.acceptReport(reportId);
                    }
                    return true;
                } catch (NumberFormatException e) {
                    player.sendMessage(lang.getMessage(player, "geral.prefixo") + "ID de denúncia inválido.");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("reject") && args.length >= 2) {
                try {
                    int reportId = Integer.parseInt(args[1]);
                    if (activeChatInterfaces.containsKey(player.getUniqueId())) {
                        activeChatInterfaces.get(player.getUniqueId()).rejectReport(reportId);
                    } else {
                        ReportsChatInterface chatInterface = new ReportsChatInterface(plugin, player);
                        chatInterface.rejectReport(reportId);
                    }
                    return true;
                } catch (NumberFormatException e) {
                    player.sendMessage(lang.getMessage(player, "geral.prefixo") + "ID de denúncia inválido.");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("teleport") && args.length >= 2) {
                try {
                    int reportId = Integer.parseInt(args[1]);
                    if (activeChatInterfaces.containsKey(player.getUniqueId())) {
                        activeChatInterfaces.get(player.getUniqueId()).teleportToReportedPlayer(reportId);
                    } else {
                        ReportsChatInterface chatInterface = new ReportsChatInterface(plugin, player);
                        chatInterface.teleportToReportedPlayer(reportId);
                    }
                    return true;
                } catch (NumberFormatException e) {
                    player.sendMessage(lang.getMessage(player, "geral.prefixo") + "ID de denúncia inválido.");
                    return true;
                }
            }
        }

        // Verificar o tipo de interface configurado
        if (plugin.getConfigManager().isReportsGuiInterface()) {
            // Abrir GUI de denúncias
            new ReportsGUI(plugin, player).open();
        } else {
            // Usar interface de chat clicável
            ReportsChatInterface chatInterface = new ReportsChatInterface(plugin, player);
            activeChatInterfaces.put(player.getUniqueId(), chatInterface);
            chatInterface.showPendingReports();
        }
        
        return true;
    }
    
    /**
     * Limpa a interface de chat ativa para um jogador
     * @param playerUUID UUID do jogador
     */
    public void clearActiveChatInterface(UUID playerUUID) {
        activeChatInterfaces.remove(playerUUID);
    }
}
package com.spirit.threport.commands;

import com.spirit.threport.ThReport;
import com.spirit.threport.chat.ReportChatInterface;
import com.spirit.threport.gui.ReportTypeGUI;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReportCommand implements CommandExecutor {

    private final ThReport plugin;
    private final Map<UUID, ReportChatInterface> activeChatInterfaces;

    public ReportCommand(ThReport plugin) {
        this.plugin = plugin;
        this.activeChatInterfaces = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Verificar se o comando foi executado por um jogador
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage((Player) null, "geral.only_players"));
            return true;
        }

        Player player = (Player) sender;

        // Verificar subcomandos para interface de chat
        if (args.length >= 1 && args[0].equalsIgnoreCase("cancel")) {
            // Cancelar denúncia via chat
            if (activeChatInterfaces.containsKey(player.getUniqueId())) {
                activeChatInterfaces.get(player.getUniqueId()).cancelReport();
                activeChatInterfaces.remove(player.getUniqueId());
            }
            return true;
        } else if (args.length >= 3 && args[0].equalsIgnoreCase("confirm")) {
            // Confirmar seleção de motivo via chat
            String targetName = args[1];
            String reasonId = args[2];
            
            Player target = Bukkit.getPlayer(targetName);
            if (target == null) {
                player.sendMessage(plugin.getLanguageManager().getMessage(player, "geral.prefixo") + 
                        plugin.getLanguageManager().getMessage(player, "geral.jogador-offline"));
                return true;
            }
            
            ReportChatInterface chatInterface = new ReportChatInterface(plugin, player, target);
            activeChatInterfaces.put(player.getUniqueId(), chatInterface);
            chatInterface.showConfirmation(reasonId);
            return true;
        } else if (args.length >= 3 && args[0].equalsIgnoreCase("submit")) {
            // Submeter denúncia via chat
            String targetName = args[1];
            String reasonId = args[2];
            
            Player target = Bukkit.getPlayer(targetName);
            if (target == null) {
                player.sendMessage(plugin.getLanguageManager().getMessage(player, "geral.prefixo") + 
                        plugin.getLanguageManager().getMessage(player, "geral.jogador-offline"));
                return true;
            }
            
            // Verificar se o jogador está bloqueado de fazer denúncias
            if (plugin.getReportManager().isPlayerBlocked(player.getUniqueId())) {
                player.sendMessage(plugin.getLanguageManager().getMessage(player, "geral.prefixo") + 
                        plugin.getLanguageManager().getMessage(player, "report.bloqueado"));
                return true;
            }

            // Verificar cooldown
            if (plugin.getReportManager().isInCooldown(player.getUniqueId(), target.getUniqueId())) {
                int remaining = plugin.getReportManager().getRemainingCooldown(player.getUniqueId(), target.getUniqueId());
                String cooldownMessage = plugin.getLanguageManager().getMessage(player, "report.cooldown")
                        .replace("{tempo}", String.valueOf(remaining));
                player.sendMessage(plugin.getLanguageManager().getMessage(player, "geral.prefixo") + cooldownMessage);
                return true;
            }
            
            if (activeChatInterfaces.containsKey(player.getUniqueId())) {
                activeChatInterfaces.get(player.getUniqueId()).submitReport(reasonId);
                activeChatInterfaces.remove(player.getUniqueId());
            }
            return true;
        }

        // Comando principal /report <jogador>
        // Verificar se o jogador forneceu um nome
        if (args.length < 1) {
            String comandoCorreto = plugin.getLanguageManager().getMessage(player, "geral.comando-incorreto")
                    .replace("{comando}", "/" + label + " <jogador>");
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "geral.prefixo") + comandoCorreto);
            return true;
        }

        // Verificar se o jogador está tentando reportar a si mesmo
        if (args[0].equalsIgnoreCase(player.getName())) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "geral.prefixo") + 
                    plugin.getLanguageManager().getMessage(player, "report.nao-pode-reportar-si-mesmo"));
            return true;
        }

        // Obter o jogador alvo
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "geral.prefixo") + 
                    plugin.getLanguageManager().getMessage(player, "geral.jogador-offline"));
            return true;
        }

        // Verificar se o jogador está bloqueado de fazer denúncias
        if (plugin.getReportManager().isPlayerBlocked(player.getUniqueId())) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "geral.prefixo") + 
                    plugin.getLanguageManager().getMessage(player, "report.bloqueado"));
            return true;
        }

        // Verificar cooldown
        if (plugin.getReportManager().isInCooldown(player.getUniqueId(), target.getUniqueId())) {
            int remaining = plugin.getReportManager().getRemainingCooldown(player.getUniqueId(), target.getUniqueId());
            String cooldownMessage = plugin.getLanguageManager().getMessage(player, "report.cooldown")
                    .replace("{tempo}", String.valueOf(remaining));
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "geral.prefixo") + cooldownMessage);
            return true;
        }

        // Verificar o tipo de interface configurado
        if (plugin.getConfigManager().isGuiInterface()) {
            // Abrir menu de tipos de denúncia (GUI)
            new ReportTypeGUI(plugin, player, target).open();
            
            // Tocar som ao abrir menu (se habilitado)
            if (plugin.getConfigManager().areSoundsEnabled()) {
                try {
                    player.playSound(player.getLocation(), 
                            Sound.valueOf(plugin.getConfigManager().getOpenMenuSound()), 
                            1.0f, 1.0f);
                } catch (Exception e) {
                    // Ignorar erro se o som não existir
                }
            }
        } else {
            // Usar interface de chat clicável
            ReportChatInterface chatInterface = new ReportChatInterface(plugin, player, target);
            activeChatInterfaces.put(player.getUniqueId(), chatInterface);
            chatInterface.showReportOptions();
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
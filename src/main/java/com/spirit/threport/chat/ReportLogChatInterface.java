package com.spirit.threport.chat;

import com.spirit.threport.ThReport;
import com.spirit.threport.models.Report;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Interface de chat clicável para visualização do histórico de denúncias
 */
public class ReportLogChatInterface {

    private final ThReport plugin;
    private final Player player;

    /**
     * Construtor da interface de chat para histórico de denúncias
     * @param plugin Instância do plugin
     * @param player Jogador que está visualizando
     */
    public ReportLogChatInterface(ThReport plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    /**
     * Mostra o histórico de denúncias de um jogador
     * @param targetName Nome do jogador alvo
     */
    public void showReportHistory(String targetName) {
        // Tentar obter UUID do jogador
        UUID targetUUID = null;
        
        // Primeiro tenta encontrar o jogador online
        Player targetPlayer = plugin.getServer().getPlayerExact(targetName);
        if (targetPlayer != null) {
            targetUUID = targetPlayer.getUniqueId();
        } else {
            // Se não estiver online, busca pelo histórico usando o nome
            List<Report> allReports = plugin.getReportManager().getPendingReports();
            // Adiciona também os reports resolvidos
            for (Report report : plugin.getReportManager().getPlayerReports(null)) {
                if (!allReports.contains(report)) {
                    allReports.add(report);
                }
            }
            
            for (Report report : allReports) {
                if (report.getReportedName().equalsIgnoreCase(targetName)) {
                    targetUUID = report.getReportedUUID();
                    break;
                }
            }
        }
        
        // Se não encontrou o UUID, informa ao jogador
        if (targetUUID == null) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "geral.prefixo") + 
                    ChatColor.RED + "Jogador não encontrado ou sem histórico de denúncias.");
            return;
        }
        
        List<Report> reports = plugin.getReportManager().getPlayerReportHistory(targetUUID);
        
        // Enviar título
        String title = plugin.getLanguageManager().getMessage(player, "staff.historico-titulo")
                .replace("{jogador}", targetName);
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage(ChatColor.YELLOW + title);
        player.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        
        if (reports.isEmpty()) {
            // Não há histórico de denúncias
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getLanguageManager().getMessage(player, "staff.sem-historico")));
        } else {
            // Listar histórico de denúncias
            player.sendMessage(ChatColor.YELLOW + "Total de denúncias: " + ChatColor.WHITE + reports.size());
            player.sendMessage("");
            
            SimpleDateFormat dateFormat = new SimpleDateFormat(plugin.getConfigManager().getDateFormat());
            
            for (Report report : reports) {
                String date = dateFormat.format(new Date(report.getTimestamp()));
                String status = report.isResolved() ? ChatColor.GREEN + "✓" : ChatColor.RED + "✗";
                
                TextComponent reportComponent = new TextComponent(
                        status + " " +
                        ChatColor.WHITE + report.getReason() + 
                        ChatColor.GRAY + " - " + 
                        ChatColor.WHITE + "Reportado por: " + report.getReporterName() + 
                        ChatColor.GRAY + " (" + date + ")");
                
                reportComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                        new ComponentBuilder(ChatColor.YELLOW + "Servidor: " + report.getServer() + "\n" + 
                                ChatColor.YELLOW + (report.isResolved() ? "Resolvido" : "Pendente")).create()));
                
                player.spigot().sendMessage(reportComponent);
            }
        }
        
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        
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
    }
}

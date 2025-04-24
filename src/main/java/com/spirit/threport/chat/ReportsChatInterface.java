package com.spirit.threport.chat;

import com.spirit.threport.ThReport;
import com.spirit.threport.models.Report;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Interface de chat clicável para visualização de denúncias
 */
public class ReportsChatInterface {

    private final ThReport plugin;
    private final Player player;

    /**
     * Construtor da interface de chat para denúncias
     * @param plugin Instância do plugin
     * @param player Jogador que está visualizando
     */
    public ReportsChatInterface(ThReport plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    /**
     * Mostra a lista de denúncias pendentes
     */
    public void showPendingReports() {
        List<Report> pendingReports = plugin.getReportManager().getPendingReports();
        
        // Enviar título
        String title = plugin.getLanguageManager().getMessage(player, "staff.menu-titulo");
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage(ChatColor.YELLOW + title);
        player.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        
        if (pendingReports.isEmpty()) {
            // Não há denúncias pendentes
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getLanguageManager().getMessage(player, "staff.sem-denuncias")));
        } else {
            // Listar denúncias pendentes
            player.sendMessage(ChatColor.YELLOW + "Denúncias pendentes: " + ChatColor.WHITE + pendingReports.size());
            player.sendMessage("");
            
            SimpleDateFormat dateFormat = new SimpleDateFormat(plugin.getConfigManager().getDateFormat());
            
            for (Report report : pendingReports) {
                String date = dateFormat.format(new Date(report.getTimestamp()));
                
                TextComponent reportComponent = new TextComponent(ChatColor.YELLOW + "➤ " + 
                        ChatColor.WHITE + report.getReportedName() + 
                        ChatColor.GRAY + " - " + 
                        ChatColor.WHITE + report.getReason() + 
                        ChatColor.GRAY + " (" + date + ")");
                
                reportComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, 
                        "/reports view " + report.getId()));
                
                reportComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                        new ComponentBuilder(ChatColor.YELLOW + "Clique para ver detalhes").create()));
                
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
    
    /**
     * Mostra os detalhes de uma denúncia específica
     * @param reportId ID da denúncia
     */
    public void showReportDetails(int reportId) {
        Report report = null;
        
        // Buscar a denúncia pelo ID
        for (Report r : plugin.getReportManager().getPendingReports()) {
            if (r.getId() == reportId) {
                report = r;
                break;
            }
        }
        
        if (report == null) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "geral.prefixo") + 
                    ChatColor.RED + "Denúncia não encontrada.");
            return;
        }
        
        // Enviar título
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage(ChatColor.YELLOW + plugin.getLanguageManager().getMessage(player, "staff.opcoes-titulo"));
        player.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        
        // Informações da denúncia
        SimpleDateFormat dateFormat = new SimpleDateFormat(plugin.getConfigManager().getDateFormat());
        String date = dateFormat.format(new Date(report.getTimestamp()));
        
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                plugin.getLanguageManager().getMessage(player, "staff.info-denuncia.jogador")
                .replace("{jogador}", report.getReportedName())));
        
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                plugin.getLanguageManager().getMessage(player, "staff.info-denuncia.motivo")
                .replace("{motivo}", report.getReason())));
        
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                plugin.getLanguageManager().getMessage(player, "staff.info-denuncia.reportado-por")
                .replace("{reporter}", report.getReporterName())));
        
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                plugin.getLanguageManager().getMessage(player, "staff.info-denuncia.servidor")
                .replace("{servidor}", report.getServer())));
        
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                plugin.getLanguageManager().getMessage(player, "staff.info-denuncia.data")
                .replace("{data}", date)));
        
        player.sendMessage("");
        
        // Opções
        // Botão de aceitar
        TextComponent acceptComponent = new TextComponent(ChatColor.GREEN + "➤ " + 
                ChatColor.translateAlternateColorCodes('&', plugin.getLanguageManager().getMessage(player, "staff.opcoes.aceitar")));
        
        acceptComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, 
                "/reports accept " + reportId));
        
        acceptComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new ComponentBuilder(ChatColor.GREEN + "Clique para aceitar a denúncia").create()));
        
        player.spigot().sendMessage(acceptComponent);
        
        // Botão de rejeitar
        TextComponent rejectComponent = new TextComponent(ChatColor.RED + "➤ " + 
                ChatColor.translateAlternateColorCodes('&', plugin.getLanguageManager().getMessage(player, "staff.opcoes.rejeitar")));
        
        rejectComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, 
                "/reports reject " + reportId));
        
        rejectComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new ComponentBuilder(ChatColor.RED + "Clique para rejeitar a denúncia").create()));
        
        player.spigot().sendMessage(rejectComponent);
        
        // Botão de teleportar
        TextComponent teleportComponent = new TextComponent(ChatColor.AQUA + "➤ " + 
                ChatColor.translateAlternateColorCodes('&', plugin.getLanguageManager().getMessage(player, "staff.opcoes.teleportar")));
        
        teleportComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, 
                "/reports teleport " + reportId));
        
        teleportComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new ComponentBuilder(ChatColor.AQUA + "Clique para teleportar até o jogador").create()));
        
        player.spigot().sendMessage(teleportComponent);
        
        // Botão de voltar
        TextComponent backComponent = new TextComponent(ChatColor.GRAY + "➤ " + 
                ChatColor.translateAlternateColorCodes('&', plugin.getLanguageManager().getMessage(player, "staff.botoes.voltar")));
        
        backComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, 
                "/reports"));
        
        backComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new ComponentBuilder(ChatColor.GRAY + "Clique para voltar à lista de denúncias").create()));
        
        player.spigot().sendMessage(backComponent);
        
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        
        // Tocar som ao clicar (se habilitado)
        if (plugin.getConfigManager().areSoundsEnabled()) {
            try {
                player.playSound(player.getLocation(), 
                        Sound.valueOf(plugin.getConfigManager().getClickSound()), 
                        1.0f, 1.0f);
            } catch (Exception e) {
                // Ignorar erro se o som não existir
            }
        }
    }
    
    /**
     * Aceita uma denúncia
     * @param reportId ID da denúncia
     */
    public void acceptReport(int reportId) {
        if (plugin.getReportManager().resolveReport(reportId, player.getUniqueId())) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "geral.prefixo") + 
                    plugin.getLanguageManager().getMessage(player, "staff.resolvido"));
            
            // Tocar som (se habilitado)
            if (plugin.getConfigManager().areSoundsEnabled()) {
                try {
                    player.playSound(player.getLocation(), 
                            Sound.valueOf(plugin.getConfigManager().getReportAcceptedSound()), 
                            1.0f, 1.0f);
                } catch (Exception e) {
                    // Ignorar erro se o som não existir
                }
            }
        } else {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "geral.prefixo") + 
                    ChatColor.RED + "Denúncia não encontrada.");
        }
    }
    
    /**
     * Rejeita uma denúncia
     * @param reportId ID da denúncia
     */
    public void rejectReport(int reportId) {
        if (plugin.getReportManager().deleteReport(reportId)) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "geral.prefixo") + 
                    plugin.getLanguageManager().getMessage(player, "staff.deletado"));
            
            // Tocar som (se habilitado)
            if (plugin.getConfigManager().areSoundsEnabled()) {
                try {
                    player.playSound(player.getLocation(), 
                            Sound.valueOf(plugin.getConfigManager().getReportRejectedSound()), 
                            1.0f, 1.0f);
                } catch (Exception e) {
                    // Ignorar erro se o som não existir
                }
            }
        } else {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "geral.prefixo") + 
                    ChatColor.RED + "Denúncia não encontrada.");
        }
    }
    
    /**
     * Teleporta até o jogador denunciado
     * @param reportId ID da denúncia
     */
    public void teleportToReportedPlayer(int reportId) {
        Report report = null;
        
        // Buscar a denúncia pelo ID
        for (Report r : plugin.getReportManager().getPendingReports()) {
            if (r.getId() == reportId) {
                report = r;
                break;
            }
        }
        
        if (report == null) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "geral.prefixo") + 
                    ChatColor.RED + "Denúncia não encontrada.");
            return;
        }
        
        // Tentar teleportar até o jogador
        Player target = plugin.getServer().getPlayer(report.getReportedUUID());
        
        if (target == null || !target.isOnline()) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "geral.prefixo") + 
                    plugin.getLanguageManager().getMessage(player, "staff.nao-mesmo-servidor"));
            return;
        }
        
        player.teleport(target);
        player.sendMessage(plugin.getLanguageManager().getMessage(player, "geral.prefixo") + 
                plugin.getLanguageManager().getMessage(player, "staff.teleportado"));
        
        // Tocar som (se habilitado)
        if (plugin.getConfigManager().areSoundsEnabled()) {
            try {
                player.playSound(player.getLocation(), 
                        Sound.valueOf(plugin.getConfigManager().getTeleportSound()), 
                        1.0f, 1.0f);
            } catch (Exception e) {
                // Ignorar erro se o som não existir
            }
        }
    }
}

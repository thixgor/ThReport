package com.spirit.threport.listeners;

import com.spirit.threport.ThReport;
import com.spirit.threport.managers.LanguageManager;
import com.spirit.threport.managers.ReportManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener para eventos relacionados aos jogadores
 */
public class PlayerListener implements Listener {

    private final ThReport plugin;
    private final LanguageManager lang;
    private final ReportManager reportManager;

    /**
     * Construtor
     * @param plugin Instância do plugin
     */
    public PlayerListener(ThReport plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLanguageManager();
        this.reportManager = plugin.getReportManager();
    }

    /**
     * Evento disparado quando um jogador entra no servidor
     * @param event Evento de entrada do jogador
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Verificar se o jogador tem permissão para receber notificações
        if (player.hasPermission("threport.notify")) {
            // Verificar se existem denúncias pendentes
            int pendingReports = reportManager.getPendingReportsCount();
            if (pendingReports > 0) {
                // Enviar mensagem informando sobre denúncias pendentes
                player.sendMessage(lang.getMessage(player, "pending_reports", "count", pendingReports));
            }
        }
    }

    /**
     * Evento disparado quando um jogador sai do servidor
     * @param event Evento de saída do jogador
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Limpar qualquer processo de denúncia em andamento
        reportManager.cancelReportProcess(player);
    }
}
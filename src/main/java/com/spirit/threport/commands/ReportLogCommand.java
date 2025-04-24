package com.spirit.threport.commands;

import com.spirit.threport.ThReport;
import com.spirit.threport.chat.ReportLogChatInterface;
import com.spirit.threport.gui.ReportHistoryGUI;
import com.spirit.threport.managers.LanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Comando para visualizar o histórico de denúncias
 */
public class ReportLogCommand implements CommandExecutor {

    private final ThReport plugin;
    private final LanguageManager lang;

    /**
     * Construtor
     * @param plugin Instância do plugin
     */
    public ReportLogCommand(ThReport plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLanguageManager();
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
        if (!player.hasPermission("threport.reportlog")) {
            player.sendMessage(lang.getMessage(player, "geral.sem_permissao"));
            return true;
        }

        // Verificar se foi fornecido um jogador
        if (args.length < 1) {
            player.sendMessage(lang.getMessage(player, "reportlog_usage"));
            return true;
        }

        String targetName = args[0];
        
        // Verificar o tipo de interface configurado
        if (plugin.getConfigManager().isReportLogGuiInterface()) {
            // Abrir GUI de histórico de denúncias
            new ReportHistoryGUI(plugin, player, targetName).open();
        } else {
            // Usar interface de chat clicável
            ReportLogChatInterface chatInterface = new ReportLogChatInterface(plugin, player);
            chatInterface.showReportHistory(targetName);
        }
        
        return true;
    }
}
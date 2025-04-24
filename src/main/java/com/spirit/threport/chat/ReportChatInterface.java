package com.spirit.threport.chat;

import com.spirit.threport.ThReport;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Set;

/**
 * Interface de chat clicável para o sistema de denúncias
 */
public class ReportChatInterface {

    private final ThReport plugin;
    private final Player player;
    private final Player target;

    /**
     * Construtor da interface de chat para denúncias
     * @param plugin Instância do plugin
     * @param player Jogador que está reportando
     * @param target Jogador que está sendo reportado
     */
    public ReportChatInterface(ThReport plugin, Player player, Player target) {
        this.plugin = plugin;
        this.player = player;
        this.target = target;
    }

    /**
     * Mostra a interface de chat com os motivos de denúncia
     */
    public void showReportOptions() {
        // Enviar título
        String title = plugin.getLanguageManager().getMessage(player, "report.menu-titulo")
                .replace("{jogador}", target.getName());
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage(ChatColor.YELLOW + title);
        player.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage(ChatColor.GRAY + "Selecione um motivo para denunciar " + ChatColor.YELLOW + target.getName() + ChatColor.GRAY + ":");
        player.sendMessage("");

        // Obter motivos de denúncia da configuração
        Set<String> reasonIds = plugin.getConfigManager().getReportReasonIds();
        
        if (reasonIds.isEmpty()) {
            // Fallback para opções padrão se não houver motivos configurados
            sendDefaultReportOptions();
        } else {
            // Enviar motivos configurados
            sendConfiguredReportOptions(reasonIds);
        }
        
        // Opção para cancelar
        TextComponent cancelComponent = new TextComponent(ChatColor.RED + "➤ " + ChatColor.DARK_RED + "Cancelar");
        cancelComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/report cancel"));
        cancelComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new ComponentBuilder(ChatColor.RED + "Clique para cancelar a denúncia").create()));
        player.spigot().sendMessage(cancelComponent);
        
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
     * Envia as opções de denúncia padrão (fallback)
     */
    private void sendDefaultReportOptions() {
        sendReportOption("hack", "Hack / Cheats", "Jogador usando hacks, cheats ou modificações ilegais");
        sendReportOption("chat", "Chat Abusivo / Ofensas", "Jogador usando linguagem ofensiva ou abusiva no chat");
        sendReportOption("grief", "Team Grief / Traição", "Jogador prejudicando sua própria equipe ou aliados");
        sendReportOption("bug", "Bug Abuse", "Jogador explorando bugs ou falhas do jogo");
        sendReportOption("cross", "Cross-Team / Trapaça", "Jogador fazendo alianças proibidas com outras equipes");
    }
    
    /**
     * Envia as opções de denúncia configuradas
     * @param reasonIds IDs dos motivos configurados
     */
    private void sendConfiguredReportOptions(Set<String> reasonIds) {
        for (String reasonId : reasonIds) {
            String name = plugin.getConfigManager().getReportReasonName(reasonId);
            String description = plugin.getConfigManager().getReportReasonDescription(reasonId);
            sendReportOption(reasonId, name, description);
        }
    }
    
    /**
     * Envia uma opção de denúncia como componente clicável
     * @param reasonId ID do motivo
     * @param name Nome do motivo
     * @param description Descrição do motivo
     */
    private void sendReportOption(String reasonId, String name, String description) {
        TextComponent component = new TextComponent(ChatColor.GREEN + "➤ " + ChatColor.translateAlternateColorCodes('&', name));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/report confirm " + target.getName() + " " + reasonId));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new ComponentBuilder(ChatColor.GRAY + description + "\n" + ChatColor.YELLOW + "Clique para selecionar").create()));
        player.spigot().sendMessage(component);
    }
    
    /**
     * Mostra a confirmação de denúncia
     * @param reasonId ID do motivo de denúncia
     */
    public void showConfirmation(String reasonId) {
        String reasonName = plugin.getConfigManager().getReportReasonName(reasonId);
        
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage(ChatColor.YELLOW + plugin.getLanguageManager().getMessage(player, "report.confirmacao-titulo"));
        player.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "Informações da Denúncia:");
        player.sendMessage(ChatColor.GRAY + "Jogador: " + ChatColor.WHITE + target.getName());
        player.sendMessage(ChatColor.GRAY + "Motivo: " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', reasonName));
        player.sendMessage("");
        
        // Botão de confirmar
        TextComponent confirmComponent = new TextComponent(ChatColor.GREEN + "➤ " + ChatColor.translateAlternateColorCodes('&', plugin.getLanguageManager().getMessage(player, "report.confirmacao-sim")));
        confirmComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/report submit " + target.getName() + " " + reasonId));
        confirmComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new ComponentBuilder(ChatColor.GREEN + "Clique para confirmar a denúncia").create()));
        player.spigot().sendMessage(confirmComponent);
        
        // Botão de cancelar
        TextComponent cancelComponent = new TextComponent(ChatColor.RED + "➤ " + ChatColor.translateAlternateColorCodes('&', plugin.getLanguageManager().getMessage(player, "report.confirmacao-nao")));
        cancelComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/report cancel"));
        cancelComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new ComponentBuilder(ChatColor.RED + "Clique para cancelar a denúncia").create()));
        player.spigot().sendMessage(cancelComponent);
        
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
     * Processa a submissão de uma denúncia
     * @param reasonId ID do motivo de denúncia
     */
    public void submitReport(String reasonId) {
        String reasonName = plugin.getConfigManager().getReportReasonName(reasonId);
        
        // Criar a denúncia
        plugin.getReportManager().createReport(target, player, reasonName);
        
        // Enviar mensagem de sucesso para o jogador que reportou
        player.sendMessage(plugin.getLanguageManager().getMessage(player, "geral.prefixo") + 
                plugin.getLanguageManager().getMessage(player, "report.sucesso"));
        
        // Tocar som de sucesso (se habilitado)
        if (plugin.getConfigManager().areSoundsEnabled()) {
            try {
                player.playSound(player.getLocation(), 
                        Sound.valueOf(plugin.getConfigManager().getReportSentSound()), 
                        1.0f, 1.0f);
            } catch (Exception e) {
                // Ignorar erro se o som não existir
            }
        }
    }
    
    /**
     * Cancela o processo de denúncia
     */
    public void cancelReport() {
        player.sendMessage(plugin.getLanguageManager().getMessage(player, "geral.prefixo") + 
                plugin.getLanguageManager().getMessage(player, "report.cancelado"));
        
        // Tocar som de cancelamento (se habilitado)
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
}

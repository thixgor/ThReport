package com.spirit.threport.gui;

import com.spirit.threport.ThReport;
import com.spirit.threport.models.Report;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Interface gráfica para opções de gerenciamento de denúncias (staff)
 */
public class ReportOptionsGUI {

    private final ThReport plugin;
    private final Player player;
    private final Report report;
    private Inventory inventory;

    /**
     * Construtor da GUI de opções de denúncia
     * @param plugin Instância do plugin
     * @param player Jogador (staff) que está visualizando
     * @param report Denúncia selecionada
     */
    public ReportOptionsGUI(ThReport plugin, Player player, Report report) {
        this.plugin = plugin;
        this.player = player;
        this.report = report;
        createInventory();
    }

    /**
     * Cria o inventário da GUI
     */
    private void createInventory() {
        String title = plugin.getLanguageManager().getMessage(player, "staff.opcoes-titulo");
        inventory = Bukkit.createInventory(null, 27, title);

        // Preencher com vidros pretos nas bordas
        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, glass);
        }

        // Informações da denúncia
        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(ChatColor.YELLOW + "Informações da Denúncia");
        
        List<String> infoLore = new ArrayList<>();
        infoLore.add(plugin.getLanguageManager().getMessage(player, "staff.info-denuncia.motivo", "motivo", report.getReason()));
        infoLore.add(plugin.getLanguageManager().getMessage(player, "staff.info-denuncia.reportado-por", "reporter", report.getReporterName()));
        infoLore.add(plugin.getLanguageManager().getMessage(player, "staff.info-denuncia.servidor", "servidor", report.getServer()));
        infoLore.add(plugin.getLanguageManager().getMessage(player, "staff.info-denuncia.data", "data", report.getFormattedDate(plugin.getConfigManager().getDateFormat())));
        infoMeta.setLore(infoLore);
        
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(4, infoItem);

        // Botão de aceitar denúncia
        ItemStack acceptItem = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta acceptMeta = acceptItem.getItemMeta();
        acceptMeta.setDisplayName(plugin.getLanguageManager().getMessage(player, "staff.opcoes.aceitar"));
        List<String> acceptLore = new ArrayList<>();
        acceptLore.add(ChatColor.GRAY + "Marca a denúncia como resolvida");
        acceptLore.add(ChatColor.GRAY + "e notifica o jogador que reportou.");
        acceptMeta.setLore(acceptLore);
        acceptItem.setItemMeta(acceptMeta);
        inventory.setItem(11, acceptItem);

        // Botão de rejeitar denúncia
        ItemStack rejectItem = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta rejectMeta = rejectItem.getItemMeta();
        rejectMeta.setDisplayName(plugin.getLanguageManager().getMessage(player, "staff.opcoes.rejeitar"));
        List<String> rejectLore = new ArrayList<>();
        rejectLore.add(ChatColor.GRAY + "Marca a denúncia como resolvida");
        rejectLore.add(ChatColor.GRAY + "e a rejeita sem notificar o jogador.");
        rejectMeta.setLore(rejectLore);
        rejectItem.setItemMeta(rejectMeta);
        inventory.setItem(15, rejectItem);

        // Botão de teleportar para o jogador denunciado
        ItemStack teleportItem = new ItemStack(Material.ENDER_PEARL);
        ItemMeta teleportMeta = teleportItem.getItemMeta();
        teleportMeta.setDisplayName(plugin.getLanguageManager().getMessage(player, "staff.opcoes.teleportar"));
        List<String> teleportLore = new ArrayList<>();
        teleportLore.add(ChatColor.GRAY + "Teleporta você até o jogador denunciado");
        teleportLore.add(ChatColor.GRAY + "se ele estiver online.");
        teleportMeta.setLore(teleportLore);
        teleportItem.setItemMeta(teleportMeta);
        inventory.setItem(13, teleportItem);

        // Botão de voltar
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.YELLOW + "Voltar");
        backItem.setItemMeta(backMeta);
        inventory.setItem(22, backItem);
    }

    /**
     * Abre a GUI para o jogador
     */
    public void open() {
        player.openInventory(inventory);
        plugin.getInventoryListener().registerReportOptionsGUI(player, this);
    }

    /**
     * Processa o clique em um item
     * @param slot Slot clicado
     */
    public void handleClick(int slot) {
        // Fechar inventário
        player.closeInventory();
        
        switch (slot) {
            case 11: // Aceitar denúncia
                acceptReport();
                break;
            case 13: // Teleportar para o jogador
                teleportToPlayer();
                break;
            case 15: // Rejeitar denúncia
                rejectReport();
                break;
            case 22: // Voltar
                new ReportsGUI(plugin, player).open();
                break;
        }
    }

    /**
     * Aceita a denúncia e notifica o jogador que reportou
     */
    private void acceptReport() {
        // Marcar como resolvida
        report.setResolved(true);
        report.setResolvedBy(player.getUniqueId());
        
        // Notificar staff
        player.sendMessage(plugin.getLanguageManager().getMessage(player, "geral.prefixo") + 
                plugin.getLanguageManager().getMessage(player, "report.aceita"));
        
        // Notificar jogador que reportou (se estiver online)
        Player reporter = Bukkit.getPlayer(report.getReporterUUID());
        if (reporter != null && reporter.isOnline()) {
            reporter.sendMessage(plugin.getLanguageManager().getMessage(reporter, "geral.prefixo") + 
                    plugin.getLanguageManager().getMessage(reporter, "report.aceita"));
        }
        
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
        
        // Voltar para a lista de denúncias
        new ReportsGUI(plugin, player).open();
    }

    /**
     * Rejeita a denúncia sem notificar o jogador
     */
    private void rejectReport() {
        // Marcar como resolvida
        report.setResolved(true);
        report.setResolvedBy(player.getUniqueId());
        
        // Notificar staff
        player.sendMessage(plugin.getLanguageManager().getMessage(player, "geral.prefixo") + 
                plugin.getLanguageManager().getMessage(player, "staff.denuncia-rejeitada"));
        
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
        
        // Voltar para a lista de denúncias
        new ReportsGUI(plugin, player).open();
    }

    /**
     * Teleporta o staff para o jogador denunciado
     */
    private void teleportToPlayer() {
        // Verificar se o jogador está online
        Player reported = Bukkit.getPlayer(report.getReportedUUID());
        
        if (reported != null && reported.isOnline()) {
            // Teleportar para o jogador
            player.teleport(reported.getLocation());
            
            // Notificar staff
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "geral.prefixo") + 
                    plugin.getLanguageManager().getMessage(player, "staff.teleportado", "jogador", reported.getName()));
            
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
        } else {
            // Jogador offline
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "geral.prefixo") + 
                    plugin.getLanguageManager().getMessage(player, "staff.jogador-offline"));
            
            // Voltar para o menu de opções
            open();
        }
    }
}
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
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Interface gráfica para visualização do histórico de denúncias de um jogador
 */
public class ReportHistoryGUI {

    private final ThReport plugin;
    private final Player player;
    private final String targetName;
    private final UUID targetUUID;
    private Inventory inventory;
    private List<Report> reports;
    private int page;

    /**
     * Construtor da GUI de histórico
     * @param plugin Instância do plugin
     * @param player Jogador (staff) que está visualizando
     * @param targetName Nome do jogador cujo histórico está sendo visualizado
     */
    public ReportHistoryGUI(ThReport plugin, Player player, String targetName) {
        this.plugin = plugin;
        this.player = player;
        this.targetName = targetName;
        this.targetUUID = Bukkit.getOfflinePlayer(targetName).getUniqueId();
        this.page = 0;
        loadReports();
        createInventory();
    }

    /**
     * Carrega as denúncias do jogador alvo
     */
    private void loadReports() {
        reports = plugin.getReportManager().getPlayerReports(targetUUID);
    }

    /**
     * Cria o inventário da GUI
     */
    private void createInventory() {
        String title = plugin.getLanguageManager().getMessage(player, "staff.historico-titulo")
                .replace("{jogador}", targetName);
        inventory = Bukkit.createInventory(null, 54, title);

        // Preencher com vidros pretos nas bordas
        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, glass);
        }

        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, glass);
        }

        for (int i = 0; i < 6; i++) {
            inventory.setItem(i * 9, glass);
            inventory.setItem(i * 9 + 8, glass);
        }

        // Verificar se há denúncias
        if (reports.isEmpty()) {
            ItemStack noReportsItem = new ItemStack(Material.BARRIER);
            ItemMeta noReportsMeta = noReportsItem.getItemMeta();
            noReportsMeta.setDisplayName(plugin.getLanguageManager().getMessage(player, "staff.sem-historico"));
            noReportsItem.setItemMeta(noReportsMeta);
            inventory.setItem(22, noReportsItem);
        } else {
            // Adicionar denúncias
            int startIndex = page * 28;
            int endIndex = Math.min(startIndex + 28, reports.size());

            for (int i = startIndex; i < endIndex; i++) {
                Report report = reports.get(i);
                int slot = 10 + (i - startIndex) % 7 + ((i - startIndex) / 7) * 9;

                // Criar item para representar a denúncia
                ItemStack reportItem = new ItemStack(Material.PAPER);
                ItemMeta reportMeta = reportItem.getItemMeta();
                
                String status = report.isResolved() ? 
                        plugin.getLanguageManager().getMessage(player, "staff.historico.resolvido") : 
                        plugin.getLanguageManager().getMessage(player, "staff.historico.pendente");
                
                reportMeta.setDisplayName(ChatColor.YELLOW + status);
                
                // Adicionar informações da denúncia
                List<String> lore = new ArrayList<>();
                lore.add(plugin.getLanguageManager().getMessage(player, "staff.info-denuncia.motivo", "motivo", report.getReason()));
                lore.add(plugin.getLanguageManager().getMessage(player, "staff.info-denuncia.reportado-por", "reporter", report.getReporterName()));
                lore.add(plugin.getLanguageManager().getMessage(player, "staff.info-denuncia.servidor", "servidor", report.getServer()));
                lore.add(plugin.getLanguageManager().getMessage(player, "staff.info-denuncia.data", "data", report.getFormattedDate(plugin.getConfigManager().getDateFormat())));
                
                if (report.isResolved() && report.getResolvedBy() != null) {
                    String resolvedByName = Bukkit.getOfflinePlayer(report.getResolvedBy()).getName();
                    if (resolvedByName != null) {
                        lore.add(plugin.getLanguageManager().getMessage(player, "staff.historico.resolvido-por", "staff", resolvedByName));
                    }
                }
                
                reportMeta.setLore(lore);
                reportItem.setItemMeta(reportMeta);
                inventory.setItem(slot, reportItem);
            }

            // Adicionar botões de navegação se necessário
            if (page > 0) {
                ItemStack prevPage = new ItemStack(Material.ARROW);
                ItemMeta prevMeta = prevPage.getItemMeta();
                prevMeta.setDisplayName(ChatColor.YELLOW + "Página Anterior");
                prevPage.setItemMeta(prevMeta);
                inventory.setItem(45, prevPage);
            }

            if (endIndex < reports.size()) {
                ItemStack nextPage = new ItemStack(Material.ARROW);
                ItemMeta nextMeta = nextPage.getItemMeta();
                nextMeta.setDisplayName(ChatColor.YELLOW + "Próxima Página");
                nextPage.setItemMeta(nextMeta);
                inventory.setItem(53, nextPage);
            }
        }

        // Botão de voltar
        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Voltar");
        backButton.setItemMeta(backMeta);
        inventory.setItem(49, backButton);
    }

    /**
     * Abre a GUI para o jogador
     */
    public void open() {
        player.openInventory(inventory);
        plugin.getInventoryListener().registerReportHistoryGUI(player, this);
        
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
     * Processa o clique em um item
     * @param slot Slot clicado
     */
    public void handleClick(int slot) {
        // Verificar se é um botão de navegação
        if (slot == 45 && page > 0) {
            // Página anterior
            page--;
            createInventory();
            open();
            return;
        }

        if (slot == 53 && (page + 1) * 28 < reports.size()) {
            // Próxima página
            page++;
            createInventory();
            open();
            return;
        }

        if (slot == 49) {
            // Voltar para o menu anterior
            player.closeInventory();
            return;
        }

        // Verificar se é uma denúncia (apenas para visualização, sem ações)
        if (reports.isEmpty()) {
            return;
        }

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
}
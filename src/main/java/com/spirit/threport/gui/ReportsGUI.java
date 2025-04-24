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

/**
 * Interface gráfica para visualização de denúncias (staff)
 */
public class ReportsGUI {

    private final ThReport plugin;
    private final Player player;
    private Inventory inventory;
    private List<Report> reports;
    private int page;

    /**
     * Construtor da GUI de denúncias
     * @param plugin Instância do plugin
     * @param player Jogador (staff) que está visualizando
     */
    public ReportsGUI(ThReport plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.page = 0;
        loadReports();
        createInventory();
    }

    /**
     * Carrega as denúncias pendentes
     */
    private void loadReports() {
        reports = plugin.getReportManager().getPendingReports();
    }

    /**
     * Cria o inventário da GUI
     */
    private void createInventory() {
        String title = plugin.getLanguageManager().getMessage(player, "staff.menu-titulo");
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
            noReportsMeta.setDisplayName(plugin.getLanguageManager().getMessage(player, "staff.sem-denuncias"));
            noReportsItem.setItemMeta(noReportsMeta);
            inventory.setItem(22, noReportsItem);
        } else {
            // Adicionar denúncias
            int startIndex = page * 28;
            int endIndex = Math.min(startIndex + 28, reports.size());

            for (int i = startIndex; i < endIndex; i++) {
                Report report = reports.get(i);
                int slot = 10 + (i - startIndex) % 7 + ((i - startIndex) / 7) * 9;

                // Criar item de cabeça do jogador denunciado
                ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
                SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
                skullMeta.setOwner(report.getReportedName());
                skullMeta.setDisplayName(ChatColor.RED + report.getReportedName());

                // Adicionar informações da denúncia
                List<String> lore = new ArrayList<>();
                lore.add(plugin.getLanguageManager().getMessage(player, "staff.info-denuncia.motivo", "motivo", report.getReason()));
                lore.add(plugin.getLanguageManager().getMessage(player, "staff.info-denuncia.reportado-por", "reporter", report.getReporterName()));
                lore.add(plugin.getLanguageManager().getMessage(player, "staff.info-denuncia.servidor", "servidor", report.getServer()));
                lore.add(plugin.getLanguageManager().getMessage(player, "staff.info-denuncia.data", "data", report.getFormattedDate(plugin.getConfigManager().getDateFormat())));
                lore.add("");
                lore.add(ChatColor.YELLOW + "Clique para ver opções");
                skullMeta.setLore(lore);

                skull.setItemMeta(skullMeta);
                inventory.setItem(slot, skull);
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

        // Botão de atualizar
        ItemStack refreshButton = new ItemStack(Material.HOPPER);
        ItemMeta refreshMeta = refreshButton.getItemMeta();
        refreshMeta.setDisplayName(ChatColor.GREEN + "Atualizar Lista");
        refreshButton.setItemMeta(refreshMeta);
        inventory.setItem(49, refreshButton);
    }

    /**
     * Abre a GUI para o jogador
     */
    public void open() {
        player.openInventory(inventory);
        plugin.getInventoryListener().registerReportsGUI(player, this);
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
            // Atualizar lista
            loadReports();
            createInventory();
            open();
            return;
        }

        // Verificar se é uma denúncia
        if (reports.isEmpty()) {
            return;
        }

        // Calcular índice da denúncia
        int row = (slot - 10) / 9;
        int col = (slot - 10) % 9;

        if (row >= 0 && row < 4 && col >= 0 && col < 7) {
            int index = page * 28 + row * 7 + col;

            if (index < reports.size()) {
                Report report = reports.get(index);
                // Abrir menu de opções para esta denúncia
                new ReportOptionsGUI(plugin, player, report).open();

                // Tocar som ao abrir menu (se habilitado)
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
    }
}
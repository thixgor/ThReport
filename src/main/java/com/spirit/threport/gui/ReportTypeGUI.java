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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface gráfica para seleção do tipo de denúncia
 */
public class ReportTypeGUI {

    private final ThReport plugin;
    private final Player player;
    private final Player target;
    private Inventory inventory;
    private final Map<Integer, String> slotToReasonMap = new HashMap<>();

    /**
     * Construtor da GUI de tipos de denúncia
     * @param plugin Instância do plugin
     * @param player Jogador que está reportando
     * @param target Jogador que está sendo reportado
     */
    public ReportTypeGUI(ThReport plugin, Player player, Player target) {
        this.plugin = plugin;
        this.player = player;
        this.target = target;
        createInventory();
    }

    /**
     * Cria o inventário da GUI
     */
    private void createInventory() {
        String title = plugin.getLanguageManager().getMessage(player, "report.menu-titulo")
                .replace("{jogador}", target.getName());
        inventory = Bukkit.createInventory(null, 27, title);

        // Preencher com vidros pretos nas bordas
        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < 27; i++) {
            if (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, glass);
            }
        }

        // Adicionar opções de denúncia a partir da configuração
        Set<String> reasonIds = plugin.getConfigManager().getReportReasonIds();
        if (reasonIds.isEmpty()) {
            // Fallback para opções padrão se não houver motivos configurados
            addDefaultReportOptions();
        } else {
            // Adicionar motivos configurados
            addConfiguredReportOptions(reasonIds);
        }

        // Adicionar botão de fechar
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        closeMeta.setDisplayName(ChatColor.RED + "Fechar");
        closeButton.setItemMeta(closeMeta);
        inventory.setItem(22, closeButton);
    }

    /**
     * Adiciona as opções de denúncia padrão (fallback)
     */
    private void addDefaultReportOptions() {
        addReportOption(10, Material.DIAMOND_SWORD, "hack", "⚔️ Hack / Cheats");
        addReportOption(11, Material.BOOK, "chat", "💬 Chat Abusivo / Ofensas");
        addReportOption(12, Material.TNT, "grief", "🕵️ Team Grief / Traição");
        addReportOption(14, Material.BEDROCK, "bug", "🧱 Bug Abuse");
        addReportOption(15, Material.BOW, "cross", "🏹 Cross-Team / Trapaça");
    }

    /**
     * Adiciona as opções de denúncia configuradas
     * @param reasonIds IDs dos motivos configurados
     */
    private void addConfiguredReportOptions(Set<String> reasonIds) {
        int[] slots = {10, 11, 12, 13, 14, 15, 16};
        int slotIndex = 0;
        
        for (String reasonId : reasonIds) {
            if (slotIndex >= slots.length) break; // Limitar ao número de slots disponíveis
            
            String itemName = plugin.getConfigManager().getReportReasonName(reasonId);
            String itemMaterial = plugin.getConfigManager().getReportReasonItem(reasonId);
            String description = plugin.getConfigManager().getReportReasonDescription(reasonId);
            
            Material material;
            try {
                material = Material.valueOf(itemMaterial);
            } catch (Exception e) {
                // Fallback para PAPER se o material não for válido
                material = Material.PAPER;
            }
            
            // Adicionar o item ao inventário
            int slot = slots[slotIndex++];
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', itemName));
            
            List<String> lore = new ArrayList<>();
            for (String line : description.split("\\n")) {
                lore.add(ChatColor.GRAY + line);
            }
            meta.setLore(lore);
            
            item.setItemMeta(meta);
            inventory.setItem(slot, item);
            
            // Mapear o slot para o ID do motivo
            slotToReasonMap.put(slot, reasonId);
        }
    }

    /**
     * Adiciona uma opção de denúncia ao inventário (método legado)
     * @param slot Slot do inventário
     * @param material Material do item
     * @param type Tipo de denúncia
     * @param nameKey Nome do motivo
     */
    private void addReportOption(int slot, Material material, String type, String nameKey) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', nameKey));
        
        String description = plugin.getLanguageManager().getMessage(player, "report.descricoes." + type);
        List<String> lore = new ArrayList<>();
        for (String line : description.split("\\n")) {
            lore.add(ChatColor.GRAY + line);
        }
        meta.setLore(lore);
        
        item.setItemMeta(meta);
        inventory.setItem(slot, item);
        
        // Mapear o slot para o ID do motivo
        slotToReasonMap.put(slot, type);
    }

    /**
     * Abre a GUI para o jogador
     */
    public void open() {
        player.openInventory(inventory);
        plugin.getInventoryListener().registerReportTypeGUI(player, this);
    }

    /**
     * Processa o clique em um tipo de denúncia
     * @param slot Slot clicado
     */
    public void handleClick(int slot) {
        // Verificar se o slot clicado contém um motivo de denúncia
        String reasonId = slotToReasonMap.get(slot);
        
        if (reasonId != null) {
            // Obter o nome do motivo a partir do ID
            String reasonName = plugin.getConfigManager().getReportReasonName(reasonId);
            
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
            
            // Abrir menu de confirmação
            new ReportConfirmGUI(plugin, player, target, reasonName).open();
        } else if (slot == 22) {
            // Fechar menu
            player.closeInventory();
        }
    }
}
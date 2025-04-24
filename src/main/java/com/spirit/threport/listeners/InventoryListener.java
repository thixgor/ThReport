package com.spirit.threport.listeners;

import com.spirit.threport.ThReport;
import com.spirit.threport.gui.ReportConfirmGUI;
import com.spirit.threport.gui.ReportHistoryGUI;
import com.spirit.threport.gui.ReportOptionsGUI;
import com.spirit.threport.gui.ReportTypeGUI;
import com.spirit.threport.gui.ReportsGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryListener implements Listener {

    private final Map<UUID, ReportTypeGUI> reportTypeGUIs;
    private final Map<UUID, ReportConfirmGUI> reportConfirmGUIs;
    private final Map<UUID, ReportsGUI> reportsGUIs;
    private final Map<UUID, ReportHistoryGUI> reportHistoryGUIs;
    private final Map<UUID, ReportOptionsGUI> reportOptionsGUIs;

    public InventoryListener(ThReport plugin) {
        this.reportTypeGUIs = new HashMap<>();
        this.reportConfirmGUIs = new HashMap<>();
        this.reportsGUIs = new HashMap<>();
        this.reportHistoryGUIs = new HashMap<>();
        this.reportOptionsGUIs = new HashMap<>();
    }

    /**
     * Registra uma GUI de tipo de denúncia para um jogador
     * @param player Jogador
     * @param gui GUI de tipo de denúncia
     */
    public void registerReportTypeGUI(Player player, ReportTypeGUI gui) {
        reportTypeGUIs.put(player.getUniqueId(), gui);
    }

    /**
     * Registra uma GUI de confirmação para um jogador
     * @param player Jogador
     * @param gui GUI de confirmação
     */
    public void registerReportConfirmGUI(Player player, ReportConfirmGUI gui) {
        reportConfirmGUIs.put(player.getUniqueId(), gui);
    }

    /**
     * Registra uma GUI de denúncias para um jogador
     * @param player Jogador
     * @param gui GUI de denúncias
     */
    public void registerReportsGUI(Player player, ReportsGUI gui) {
        reportsGUIs.put(player.getUniqueId(), gui);
    }

    /**
     * Registra uma GUI de histórico para um jogador
     * @param player Jogador
     * @param gui GUI de histórico
     */
    public void registerReportHistoryGUI(Player player, ReportHistoryGUI gui) {
        reportHistoryGUIs.put(player.getUniqueId(), gui);
    }

    /**
     * Registra uma GUI de opções de denúncia para um jogador
     * @param player Jogador
     * @param gui GUI de opções de denúncia
     */
    public void registerReportOptionsGUI(Player player, ReportOptionsGUI gui) {
        reportOptionsGUIs.put(player.getUniqueId(), gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getClickedInventory();

        // Cancelar o evento para evitar que o jogador pegue os itens
        event.setCancelled(true);

        if (inventory == null) {
            return;
        }

        // Verificar se o jogador está em uma GUI de tipo de denúncia
        if (reportTypeGUIs.containsKey(player.getUniqueId())) {
            ReportTypeGUI gui = reportTypeGUIs.get(player.getUniqueId());
            gui.handleClick(event.getSlot());
            return;
        }

        if (reportConfirmGUIs.containsKey(player.getUniqueId())) {
            ReportConfirmGUI gui = reportConfirmGUIs.get(player.getUniqueId());
            int slot = event.getSlot();
            gui.handleClick(slot);
            return;
        }

        if (reportsGUIs.containsKey(player.getUniqueId())) {
            ReportsGUI gui = reportsGUIs.get(player.getUniqueId());
            gui.handleClick(event.getSlot());
            return;
        }

        if (reportHistoryGUIs.containsKey(player.getUniqueId())) {
            ReportHistoryGUI gui = reportHistoryGUIs.get(player.getUniqueId());
            gui.handleClick(event.getSlot());
            return;
        }
        
        if (reportOptionsGUIs.containsKey(player.getUniqueId())) {
            ReportOptionsGUI gui = reportOptionsGUIs.get(player.getUniqueId());
            gui.handleClick(event.getSlot());
            return;
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // Remover o jogador dos mapas quando ele fechar o inventário
        reportTypeGUIs.remove(playerUUID);
        reportConfirmGUIs.remove(playerUUID);
        reportsGUIs.remove(playerUUID);
        reportHistoryGUIs.remove(playerUUID);
        reportOptionsGUIs.remove(playerUUID);
    }
}
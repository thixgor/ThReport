package com.spirit.threport.gui;

import com.spirit.threport.ThReport;
import com.spirit.threport.models.Report;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface gráfica para confirmação de denúncia
 */
public class ReportConfirmGUI {

    private final ThReport plugin;
    private final Player player;
    private final Player target;
    private final String reason;
    private Inventory inventory;

    /**
     * Construtor da GUI de confirmação
     * @param plugin Instância do plugin
     * @param player Jogador que está reportando
     * @param target Jogador que está sendo reportado
     * @param reason Motivo da denúncia
     */
    public ReportConfirmGUI(ThReport plugin, Player player, Player target, String reason) {
        this.plugin = plugin;
        this.player = player;
        this.target = target;
        this.reason = reason;
        createInventory();
    }

    /**
     * Cria o inventário da GUI
     */
    private void createInventory() {
        String title = plugin.getLanguageManager().getMessage(player, "report.confirmacao-titulo");
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
        infoLore.add(ChatColor.GRAY + "Jogador: " + ChatColor.WHITE + target.getName());
        infoLore.add(ChatColor.GRAY + "Motivo: " + ChatColor.WHITE + reason);
        infoMeta.setLore(infoLore);
        
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(13, infoItem);

        // Botão de confirmar (vidro verde)
        ItemStack confirmItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5);
        ItemMeta confirmMeta = confirmItem.getItemMeta();
        confirmMeta.setDisplayName(plugin.getLanguageManager().getMessage(player, "report.confirmacao-sim"));
        confirmItem.setItemMeta(confirmMeta);
        inventory.setItem(11, confirmItem);

        // Botão de cancelar (vidro vermelho)
        ItemStack cancelItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        cancelMeta.setDisplayName(plugin.getLanguageManager().getMessage(player, "report.confirmacao-nao"));
        cancelItem.setItemMeta(cancelMeta);
        inventory.setItem(15, cancelItem);
    }

    /**
     * Abre a GUI para o jogador
     */
    public void open() {
        player.openInventory(inventory);
        plugin.getInventoryListener().registerReportConfirmGUI(player, this);
    }

    /**
     * Processa o clique em um botão
     * @param slot Slot clicado
     */
    public void handleClick(int slot) {
        // Verificar qual botão foi clicado
        if (slot == 11) {

            // Confirmar denúncia
            Report report = plugin.getReportManager().createReport(target, player, reason);
            
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
            
            // Mostrar partículas (se habilitado)
            if (plugin.getConfigManager().areEffectsEnabled()) {
                try {
                    player.playEffect(player.getLocation().add(0, 1, 0), 
                            Effect.valueOf(plugin.getConfigManager().getConfirmParticles()), 
                            0);
                } catch (Exception e) {
                    // Ignorar erro se o efeito não existir
                }
            }
            
            // Fechar o inventário após processar tudo
            player.closeInventory();
        } else if (slot == 15) {
            // Cancelar denúncia
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
            
            // Fechar o inventário
            player.closeInventory();
        }
    }
}
package com.spirit.threport;

import com.spirit.threport.commands.ReportCommand;
import com.spirit.threport.commands.ReportLogCommand;
import com.spirit.threport.commands.ReportsCommand;
import com.spirit.threport.commands.ThReportCommand;
import com.spirit.threport.config.ConfigManager;
import com.spirit.threport.database.DatabaseManager;
import com.spirit.threport.listeners.InventoryListener;
import com.spirit.threport.listeners.PlayerListener;
import com.spirit.threport.managers.LanguageManager;
import com.spirit.threport.managers.ReportManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ThReport extends JavaPlugin {

    private static ThReport instance;
    private ConfigManager configManager;
    private LanguageManager languageManager;
    private ReportManager reportManager;
    private DatabaseManager databaseManager;
    private InventoryListener inventoryListener;

    @Override
    public void onEnable() {
        // Definir instância
        instance = this;
        
        // Criar pasta de idiomas se não existir
        File langFolder = new File(getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }
        
        // Inicializar gerenciadores
        configManager = new ConfigManager(this);
        languageManager = new LanguageManager(this);
        reportManager = new ReportManager(this);
        
        // Inicializar banco de dados
        if (configManager.isUsingMySQL()) {
            databaseManager = new DatabaseManager(this);
            databaseManager.connect();
            databaseManager.createTables();
        }
        
        // Registrar comandos
        getCommand("report").setExecutor(new ReportCommand(this));
        getCommand("reports").setExecutor(new ReportsCommand(this));
        getCommand("reportlog").setExecutor(new ReportLogCommand(this));
        
        // Registrar comando administrativo
        ThReportCommand thReportCommand = new ThReportCommand(this);
        getCommand("threport").setExecutor(thReportCommand);
        getCommand("threport").setTabCompleter(thReportCommand);
        
        // Registrar listeners
        inventoryListener = new InventoryListener(this);
        Bukkit.getPluginManager().registerEvents(inventoryListener, this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        
        // Mensagem de inicialização
        getLogger().info("ThReport v" + getDescription().getVersion() + " foi ativado com sucesso!");
        getLogger().info("Desenvolvido por: spirit");
    }

    @Override
    public void onDisable() {
        // Fechar conexão com banco de dados se estiver usando MySQL
        if (configManager.isUsingMySQL() && databaseManager != null) {
            databaseManager.disconnect();
        }
        
        // Salvar dados pendentes
        reportManager.saveAllReports();
        
        // Mensagem de desativação
        getLogger().info("ThReport v" + getDescription().getVersion() + " foi desativado!");
    }

    /**
     * Obtém a instância do plugin
     * @return Instância do plugin
     */
    public static ThReport getInstance() {
        return instance;
    }

    /**
     * Obtém o gerenciador de configurações
     * @return Gerenciador de configurações
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Obtém o gerenciador de idiomas
     * @return Gerenciador de idiomas
     */
    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    /**
     * Obtém o gerenciador de denúncias
     * @return Gerenciador de denúncias
     */
    public ReportManager getReportManager() {
        return reportManager;
    }

    /**
     * Obtém o gerenciador de banco de dados
     * @return Gerenciador de banco de dados
     */
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    /**
     * Obtém o listener de inventário
     * @return Listener de inventário
     */
    public InventoryListener getInventoryListener() {
        return inventoryListener;
    }
}
package com.spirit.threport.config;

import com.spirit.threport.ThReport;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ConfigManager {

    private final ThReport plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private File configFile;
    private File messagesFile;

    public ConfigManager(ThReport plugin) {
        this.plugin = plugin;
        setupFiles();
    }

    /**
     * Configura os arquivos de configuração
     */
    private void setupFiles() {
        // Configuração principal
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        // Arquivo de mensagens
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        // Carregar configurações
        reloadConfigs();
    }

    /**
     * Recarrega todas as configurações
     */
    public void reloadConfigs() {
        config = YamlConfiguration.loadConfiguration(configFile);
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    /**
     * Salva as configurações
     */
    public void saveConfigs() {
        try {
            config.save(configFile);
            messages.save(messagesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Não foi possível salvar os arquivos de configuração!");
            e.printStackTrace();
        }
    }

    /**
     * Verifica se o MySQL está ativado
     * @return true se MySQL estiver ativado
     */
    public boolean isUsingMySQL() {
        return config.getBoolean("database.use-mysql", false);
    }

    /**
     * Obtém o host do MySQL
     * @return Host do MySQL
     */
    public String getMySQLHost() {
        return config.getString("database.mysql.host", "localhost");
    }

    /**
     * Obtém a porta do MySQL
     * @return Porta do MySQL
     */
    public int getMySQLPort() {
        return config.getInt("database.mysql.port", 3306);
    }

    /**
     * Obtém o nome do banco de dados MySQL
     * @return Nome do banco de dados
     */
    public String getMySQLDatabase() {
        return config.getString("database.mysql.database", "threport");
    }

    /**
     * Obtém o usuário do MySQL
     * @return Usuário do MySQL
     */
    public String getMySQLUsername() {
        return config.getString("database.mysql.username", "root");
    }

    /**
     * Obtém a senha do MySQL
     * @return Senha do MySQL
     */
    public String getMySQLPassword() {
        return config.getString("database.mysql.password", "password");
    }

    /**
     * Obtém o prefixo das tabelas MySQL
     * @return Prefixo das tabelas
     */
    public String getMySQLTablePrefix() {
        return config.getString("database.mysql.table-prefix", "threport_");
    }

    /**
     * Obtém o tempo de cooldown entre reports
     * @return Tempo de cooldown em minutos
     */
    public int getReportCooldown() {
        return config.getInt("settings.cooldown", 5);
    }

    /**
     * Verifica se o sistema de confiabilidade está ativado
     * @return true se o sistema estiver ativado
     */
    public boolean isTrustSystemEnabled() {
        return config.getBoolean("settings.trust-system.enabled", true);
    }

    /**
     * Obtém o número máximo de denúncias falsas permitidas
     * @return Número máximo de denúncias falsas
     */
    public int getMaxFalseReports() {
        return config.getInt("settings.trust-system.max-false-reports", 3);
    }

    /**
     * Obtém o tempo de bloqueio após exceder o limite de denúncias falsas
     * @return Tempo de bloqueio em minutos
     */
    public int getTrustBlockTime() {
        return config.getInt("settings.trust-system.block-time", 60);
    }

    /**
     * Verifica se as notificações para staff estão ativadas
     * @return true se as notificações estiverem ativadas
     */
    public boolean isStaffNotificationEnabled() {
        return config.getBoolean("notifications.staff-notification.enabled", true);
    }

    /**
     * Obtém o tipo de notificação para staff
     * @return Tipo de notificação (CHAT, ACTION_BAR, TITLE)
     */
    public String getStaffNotificationType() {
        return config.getString("notifications.staff-notification.type", "ACTION_BAR");
    }

    /**
     * Verifica se os sons estão ativados
     * @return true se os sons estiverem ativados
     */
    public boolean areSoundsEnabled() {
        return config.getBoolean("notifications.sounds.enabled", true);
    }

    /**
     * Obtém o som ao enviar report
     * @return Nome do som
     */
    public String getReportSentSound() {
        return config.getString("notifications.sounds.report-sent", "LEVEL_UP");
    }

    /**
     * Obtém o som ao clicar em opções
     * @return Nome do som
     */
    public String getClickSound() {
        return config.getString("notifications.sounds.click", "CLICK");
    }

    /**
     * Obtém o som ao abrir menu
     * @return Nome do som
     */
    public String getOpenMenuSound() {
        return config.getString("notifications.sounds.open-menu", "CHEST_OPEN");
    }

    /**
     * Verifica se os efeitos visuais estão ativados
     * @return true se os efeitos estiverem ativados
     */
    public boolean areEffectsEnabled() {
        return config.getBoolean("effects.enabled", true);
    }

    /**
     * Obtém o tipo de partículas ao confirmar report
     * @return Nome do tipo de partículas
     */
    public String getConfirmParticles() {
        return config.getString("effects.confirm-particles", "CRIT");
    }

    /**
     * Verifica se a animação de inventário está ativada
     * @return true se a animação estiver ativada
     */
    public boolean isInventoryAnimationEnabled() {
        return config.getBoolean("effects.inventory-animation", true);
    }

    /**
     * Obtém o idioma padrão
     * @return Código do idioma padrão
     */
    public String getDefaultLanguage() {
        return messages.getString("language.default", "pt_BR");
    }

    /**
     * Verifica se deve usar o idioma do cliente quando disponível
     * @return true se deve usar o idioma do cliente
     */
    public boolean useClientLocale() {
        return messages.getBoolean("language.use-client-locale", true);
    }

    /**
     * Obtém o formato da data nas mensagens
     * @return Formato da data
     */
    public String getDateFormat() {
        return messages.getString("formatting.date-format", "dd/MM/yyyy HH:mm");
    }

    /**
     * Obtém a configuração principal
     * @return Configuração principal
     */
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * Obtém a configuração de mensagens
     * @return Configuração de mensagens
     */
    public FileConfiguration getMessages() {
        return messages;
    }
    
    /**
     * Obtém o som ao aceitar uma denúncia
     * @return Nome do som
     */
    public String getReportAcceptedSound() {
        return config.getString("notifications.sounds.report-accepted", "LEVEL_UP");
    }
    
    /**
     * Obtém o som ao rejeitar uma denúncia
     * @return Nome do som
     */
    public String getReportRejectedSound() {
        return config.getString("notifications.sounds.report-rejected", "NOTE_BASS");
    }
    
    /**
     * Obtém o som ao teleportar para um jogador
     * @return Nome do som
     */
    public String getTeleportSound() {
        return config.getString("notifications.sounds.teleport", "ENDERMAN_TELEPORT");
    }
    
    /**
     * Obtém o tipo de interface para reports (GUI ou CHAT)
     * @return Tipo de interface
     */
    public String getInterfaceType() {
        return config.getString("settings.interface-type", "GUI");
    }
    
    /**
     * Verifica se a interface é do tipo GUI
     * @return true se a interface for GUI
     */
    public boolean isGuiInterface() {
        return getInterfaceType().equalsIgnoreCase("GUI");
    }
    
    /**
     * Obtém o tipo de interface para o comando /reports (GUI ou CHAT)
     * @return Tipo de interface
     */
    public String getReportsInterfaceType() {
        return config.getString("settings.reports-interface-type", "GUI");
    }
    
    /**
     * Verifica se a interface do comando /reports é do tipo GUI
     * @return true se a interface for GUI
     */
    public boolean isReportsGuiInterface() {
        return getReportsInterfaceType().equalsIgnoreCase("GUI");
    }
    
    /**
     * Obtém o tipo de interface para o comando /reportlog (GUI ou CHAT)
     * @return Tipo de interface
     */
    public String getReportLogInterfaceType() {
        return config.getString("settings.reportlog-interface-type", "GUI");
    }
    
    /**
     * Verifica se a interface do comando /reportlog é do tipo GUI
     * @return true se a interface for GUI
     */
    public boolean isReportLogGuiInterface() {
        return getReportLogInterfaceType().equalsIgnoreCase("GUI");
    }
    
    /**
     * Obtém a configuração de motivos de report
     * @return Seção de configuração com os motivos de report
     */
    public ConfigurationSection getReportReasonsSection() {
        return config.getConfigurationSection("report-reasons");
    }
    
    /**
     * Obtém todos os IDs de motivos de report configurados
     * @return Lista de IDs de motivos de report
     */
    public Set<String> getReportReasonIds() {
        ConfigurationSection section = getReportReasonsSection();
        return section != null ? section.getKeys(false) : new HashSet<>();
    }
    
    /**
     * Obtém o nome de um motivo de report
     * @param reasonId ID do motivo
     * @return Nome do motivo
     */
    public String getReportReasonName(String reasonId) {
        return config.getString("report-reasons." + reasonId + ".name", reasonId);
    }
    
    /**
     * Obtém a descrição de um motivo de report
     * @param reasonId ID do motivo
     * @return Descrição do motivo
     */
    public String getReportReasonDescription(String reasonId) {
        return config.getString("report-reasons." + reasonId + ".description", "");
    }
    
    /**
     * Obtém o material do item para um motivo de report
     * @param reasonId ID do motivo
     * @return Material do item
     */
    public String getReportReasonItem(String reasonId) {
        return config.getString("report-reasons." + reasonId + ".item", "PAPER");
    }
}
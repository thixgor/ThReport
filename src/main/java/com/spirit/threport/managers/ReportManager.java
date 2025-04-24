package com.spirit.threport.managers;

import com.spirit.threport.ThReport;
import com.spirit.threport.models.Report;
import com.spirit.threport.utils.BungeeUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ReportManager {

    private final ThReport plugin;
    private final Map<UUID, Long> cooldowns;
    private final List<Report> reports;
    private File reportsFile;
    private FileConfiguration reportsConfig;

    public ReportManager(ThReport plugin) {
        this.plugin = plugin;
        this.cooldowns = new HashMap<>();
        this.reports = new ArrayList<>();
        setupFiles();
        loadReports();
    }

    /**
     * Configura os arquivos de denúncias
     */
    private void setupFiles() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        reportsFile = new File(plugin.getDataFolder(), "reports.yml");
        if (!reportsFile.exists()) {
            try {
                reportsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Não foi possível criar o arquivo reports.yml!");
                e.printStackTrace();
            }
        }

        reportsConfig = YamlConfiguration.loadConfiguration(reportsFile);
    }

    /**
     * Carrega as denúncias do arquivo ou banco de dados
     */
    private void loadReports() {
        reports.clear();

        // Se estiver usando MySQL, carregar do banco de dados
        if (plugin.getConfigManager().isUsingMySQL()) {
            reports.addAll(plugin.getDatabaseManager().getPendingReports());
            return;
        }

        // Caso contrário, carregar do arquivo local
        if (reportsConfig.contains("reports")) {
            for (String key : reportsConfig.getConfigurationSection("reports").getKeys(false)) {
                String path = "reports." + key;
                UUID reportedUUID = UUID.fromString(reportsConfig.getString(path + ".reported-uuid"));
                String reportedName = reportsConfig.getString(path + ".reported-name");
                UUID reporterUUID = UUID.fromString(reportsConfig.getString(path + ".reporter-uuid"));
                String reporterName = reportsConfig.getString(path + ".reporter-name");
                String reason = reportsConfig.getString(path + ".reason");
                String server = reportsConfig.getString(path + ".server");
                long timestamp = reportsConfig.getLong(path + ".timestamp");
                boolean resolved = reportsConfig.getBoolean(path + ".resolved", false);

                Report report = new Report(reportedUUID, reportedName, reporterUUID, reporterName, reason, server, timestamp);
                report.setId(Integer.parseInt(key));
                report.setResolved(resolved);

                if (resolved && reportsConfig.contains(path + ".resolved-by")) {
                    report.setResolvedBy(UUID.fromString(reportsConfig.getString(path + ".resolved-by")));
                }

                reports.add(report);
            }
        }
    }

    /**
     * Obtém todas as denúncias de um jogador específico
     * @param playerUUID UUID do jogador
     * @return Lista de denúncias do jogador
     */
    public List<Report> getPlayerReports(UUID playerUUID) {
        List<Report> playerReports = new ArrayList<>();
        
        // Filtrar denúncias pelo UUID do jogador denunciado
        for (Report report : reports) {
            if (report.getReportedUUID().equals(playerUUID)) {
                playerReports.add(report);
            }
        }
        
        return playerReports;
    }
    
    /**
     * Salva todas as denúncias no arquivo ou banco de dados
     */
    public void saveAllReports() {
        // Se estiver usando MySQL, não precisa salvar no arquivo
        if (plugin.getConfigManager().isUsingMySQL()) {
            return;
        }

        // Limpar configuração atual
        reportsConfig.set("reports", null);

        // Salvar cada denúncia
        for (Report report : reports) {
            String path = "reports." + report.getId();
            reportsConfig.set(path + ".reported-uuid", report.getReportedUUID().toString());
            reportsConfig.set(path + ".reported-name", report.getReportedName());
            reportsConfig.set(path + ".reporter-uuid", report.getReporterUUID().toString());
            reportsConfig.set(path + ".reporter-name", report.getReporterName());
            reportsConfig.set(path + ".reason", report.getReason());
            reportsConfig.set(path + ".server", report.getServer());
            reportsConfig.set(path + ".timestamp", report.getTimestamp());
            reportsConfig.set(path + ".resolved", report.isResolved());

            if (report.isResolved() && report.getResolvedBy() != null) {
                reportsConfig.set(path + ".resolved-by", report.getResolvedBy().toString());
            }
        }

        try {
            reportsConfig.save(reportsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Não foi possível salvar o arquivo reports.yml!");
            e.printStackTrace();
        }
    }

    /**
     * Cria uma nova denúncia
     * @param reported Jogador denunciado
     * @param reporter Jogador que reportou
     * @param reason Motivo da denúncia
     * @return Denúncia criada
     */
    public Report createReport(Player reported, Player reporter, String reason) {
        // Obter servidor atual (para BungeeCord)
        String server = BungeeUtil.getServerName();
        if (server == null || server.isEmpty()) {
            server = "default";
        }

        // Criar denúncia
        Report report = new Report(
                reported.getUniqueId(),
                reported.getName(),
                reporter.getUniqueId(),
                reporter.getName(),
                reason,
                server
        );

        // Salvar denúncia
        if (plugin.getConfigManager().isUsingMySQL()) {
            int id = plugin.getDatabaseManager().saveReport(report);
            report.setId(id);
        } else {
            // Gerar ID único para denúncia local
            int id = reports.isEmpty() ? 1 : reports.get(reports.size() - 1).getId() + 1;
            report.setId(id);
            reports.add(report);
            saveAllReports();
        }

        // Notificar staff online
        notifyStaff(report);

        // Definir cooldown
        setCooldown(reporter.getUniqueId(), reported.getUniqueId());

        return report;
    }

    /**
     * Notifica todos os staffs online sobre uma nova denúncia
     * @param report Denúncia a ser notificada
     */
    private void notifyStaff(Report report) {
        if (!plugin.getConfigManager().isStaffNotificationEnabled()) {
            return;
        }

        String prefix = plugin.getLanguageManager().getMessage((Player) null, "geral.prefixo");
        String message = plugin.getLanguageManager().getMessage((Player) null, "staff.nova-denuncia")
                .replace("{reportado}", report.getReportedName())
                .replace("{reporter}", report.getReporterName())
                .replace("{motivo}", report.getReason())
                .replace("{servidor}", report.getServer());

        // Notificar todos os jogadores com permissão
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("threport.notify")) {
                staff.sendMessage(prefix + message);
            }
        }
    }

    /**
     * Obtém o número de denúncias pendentes
     * @return Número de denúncias pendentes
     */
    public int getPendingReportsCount() {
        int count = 0;
        for (Report report : reports) {
            if (!report.isResolved()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Cancela o processo de denúncia de um jogador
     * @param player Jogador
     */
    public void cancelReportProcess(Player player) {
        // Implementação vazia pois o processo é gerenciado pela GUI
    }


    /**
     * Define um cooldown para um jogador reportar outro
     * @param reporterUUID UUID do jogador que reportou
     * @param reportedUUID UUID do jogador reportado
     */
    public void setCooldown(UUID reporterUUID, UUID reportedUUID) {
        cooldowns.put(getCooldownKey(reporterUUID, reportedUUID), System.currentTimeMillis());
    }

    /**
     * Verifica se um jogador está em cooldown para reportar outro
     * @param reporterUUID UUID do jogador que reportou
     * @param reportedUUID UUID do jogador reportado
     * @return true se o jogador estiver em cooldown
     */
    public boolean isInCooldown(UUID reporterUUID, UUID reportedUUID) {
        // Se estiver usando MySQL, verificar no banco de dados
        if (plugin.getConfigManager().isUsingMySQL()) {
            return plugin.getDatabaseManager().hasReportedRecently(reporterUUID, reportedUUID, plugin.getConfigManager().getReportCooldown());
        }

        // Caso contrário, verificar no mapa local
        UUID key = getCooldownKey(reporterUUID, reportedUUID);
        if (!cooldowns.containsKey(key)) {
            return false;
        }

        long lastReport = cooldowns.get(key);
        long cooldownTime = plugin.getConfigManager().getReportCooldown() * 60 * 1000L; // Converter minutos para milissegundos
        boolean inCooldown = System.currentTimeMillis() - lastReport < cooldownTime;

        // Remover do mapa se não estiver mais em cooldown
        if (!inCooldown) {
            cooldowns.remove(key);
        }

        return inCooldown;
    }

    /**
     * Obtém o tempo restante de cooldown em minutos
     * @param reporterUUID UUID do jogador que reportou
     * @param reportedUUID UUID do jogador reportado
     * @return Tempo restante em minutos
     */
    public int getRemainingCooldown(UUID reporterUUID, UUID reportedUUID) {
        UUID key = getCooldownKey(reporterUUID, reportedUUID);
        if (!cooldowns.containsKey(key)) {
            return 0;
        }

        long lastReport = cooldowns.get(key);
        long cooldownTime = plugin.getConfigManager().getReportCooldown() * 60 * 1000L; // Converter minutos para milissegundos
        long remaining = (lastReport + cooldownTime - System.currentTimeMillis()) / (60 * 1000L); // Converter para minutos

        return (int) Math.max(0, remaining);
    }

    /**
     * Gera uma chave única para o cooldown
     * @param reporterUUID UUID do jogador que reportou
     * @param reportedUUID UUID do jogador reportado
     * @return Chave única
     */
    private UUID getCooldownKey(UUID reporterUUID, UUID reportedUUID) {
        return UUID.nameUUIDFromBytes((reporterUUID.toString() + "-" + reportedUUID.toString()).getBytes());
    }

    /**
     * Obtém todas as denúncias pendentes
     * @return Lista de denúncias pendentes
     */
    public List<Report> getPendingReports() {
        // Se estiver usando MySQL, obter do banco de dados
        if (plugin.getConfigManager().isUsingMySQL()) {
            return plugin.getDatabaseManager().getPendingReports();
        }

        // Caso contrário, filtrar da lista local
        List<Report> pendingReports = new ArrayList<>();
        for (Report report : reports) {
            if (!report.isResolved()) {
                pendingReports.add(report);
            }
        }

        return pendingReports;
    }

    /**
     * Obtém o histórico de denúncias de um jogador
     * @param playerUUID UUID do jogador
     * @return Lista de denúncias do jogador
     */
    public List<Report> getPlayerReportHistory(UUID playerUUID) {
        // Se estiver usando MySQL, obter do banco de dados
        if (plugin.getConfigManager().isUsingMySQL()) {
            return plugin.getDatabaseManager().getPlayerReportHistory(playerUUID);
        }

        // Caso contrário, filtrar da lista local
        List<Report> playerReports = new ArrayList<>();
        for (Report report : reports) {
            if (report.getReportedUUID().equals(playerUUID)) {
                playerReports.add(report);
            }
        }

        return playerReports;
    }

    /**
     * Marca uma denúncia como resolvida
     * @param reportId ID da denúncia
     * @param resolvedBy UUID do staff que resolveu
     * @return true se a operação foi bem-sucedida
     */
    public boolean resolveReport(int reportId, UUID resolvedBy) {
        // Se estiver usando MySQL, resolver no banco de dados
        if (plugin.getConfigManager().isUsingMySQL()) {
            return plugin.getDatabaseManager().resolveReport(reportId, resolvedBy);
        }

        // Caso contrário, resolver na lista local
        for (Report report : reports) {
            if (report.getId() == reportId) {
                report.setResolved(true);
                report.setResolvedBy(resolvedBy);
                saveAllReports();
                return true;
            }
        }

        return false;
    }

    /**
     * Deleta uma denúncia
     * @param reportId ID da denúncia
     * @return true se a operação foi bem-sucedida
     */
    public boolean deleteReport(int reportId) {
        // Se estiver usando MySQL, deletar no banco de dados
        if (plugin.getConfigManager().isUsingMySQL()) {
            return plugin.getDatabaseManager().deleteReport(reportId);
        }

        // Caso contrário, deletar da lista local
        for (int i = 0; i < reports.size(); i++) {
            if (reports.get(i).getId() == reportId) {
                reports.remove(i);
                saveAllReports();
                return true;
            }
        }

        return false;
    }

    /**
     * Verifica se um jogador está bloqueado de fazer denúncias
     * @param playerUUID UUID do jogador
     * @return true se o jogador estiver bloqueado
     */
    public boolean isPlayerBlocked(UUID playerUUID) {
        // Se o sistema de confiabilidade não estiver ativado, ninguém está bloqueado
        if (!plugin.getConfigManager().isTrustSystemEnabled()) {
            return false;
        }

        // Se estiver usando MySQL, verificar no banco de dados
        if (plugin.getConfigManager().isUsingMySQL()) {
            return plugin.getDatabaseManager().isPlayerBlocked(playerUUID);
        }

        // Caso contrário, verificar no arquivo local (não implementado para arquivo local)
        return false;
    }
}
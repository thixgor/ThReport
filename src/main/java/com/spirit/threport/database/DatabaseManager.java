package com.spirit.threport.database;

import com.spirit.threport.ThReport;
import com.spirit.threport.models.Report;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {

    private final ThReport plugin;
    private Connection connection;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final String tablePrefix;

    public DatabaseManager(ThReport plugin) {
        this.plugin = plugin;
        this.host = plugin.getConfigManager().getMySQLHost();
        this.port = plugin.getConfigManager().getMySQLPort();
        this.database = plugin.getConfigManager().getMySQLDatabase();
        this.username = plugin.getConfigManager().getMySQLUsername();
        this.password = plugin.getConfigManager().getMySQLPassword();
        this.tablePrefix = plugin.getConfigManager().getMySQLTablePrefix();
    }

    /**
     * Conecta ao banco de dados MySQL
     */
    public void connect() {
        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }

            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false",
                    username,
                    password
            );

            plugin.getLogger().info("Conexão com MySQL estabelecida com sucesso!");
        } catch (SQLException | ClassNotFoundException e) {
            plugin.getLogger().severe("Falha ao conectar ao MySQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Desconecta do banco de dados MySQL
     */
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Conexão com MySQL fechada com sucesso!");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao fechar conexão com MySQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cria as tabelas necessárias no banco de dados
     */
    public void createTables() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }

            // Tabela de denúncias
            String reportsTable = "CREATE TABLE IF NOT EXISTS " + tablePrefix + "reports ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "reported_uuid VARCHAR(36) NOT NULL,"
                    + "reported_name VARCHAR(16) NOT NULL,"
                    + "reporter_uuid VARCHAR(36) NOT NULL,"
                    + "reporter_name VARCHAR(16) NOT NULL,"
                    + "reason VARCHAR(50) NOT NULL,"
                    + "server VARCHAR(50) NOT NULL,"
                    + "timestamp BIGINT NOT NULL,"
                    + "resolved BOOLEAN DEFAULT FALSE,"
                    + "resolved_by VARCHAR(36),"
                    + "INDEX (reported_uuid),"
                    + "INDEX (reporter_uuid)"
                    + ");";

            // Tabela de confiabilidade (trust)
            String trustTable = "CREATE TABLE IF NOT EXISTS " + tablePrefix + "trust ("
                    + "player_uuid VARCHAR(36) PRIMARY KEY,"
                    + "player_name VARCHAR(16) NOT NULL,"
                    + "false_reports INT DEFAULT 0,"
                    + "valid_reports INT DEFAULT 0,"
                    + "blocked_until BIGINT DEFAULT 0"
                    + ");";

            try (Statement statement = connection.createStatement()) {
                statement.execute(reportsTable);
                statement.execute(trustTable);
                plugin.getLogger().info("Tabelas criadas/verificadas com sucesso!");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao criar tabelas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Salva uma denúncia no banco de dados
     * @param report Denúncia a ser salva
     * @return ID da denúncia salva
     */
    public int saveReport(Report report) {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }

            String query = "INSERT INTO " + tablePrefix + "reports "
                    + "(reported_uuid, reported_name, reporter_uuid, reporter_name, reason, server, timestamp) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, report.getReportedUUID().toString());
                statement.setString(2, report.getReportedName());
                statement.setString(3, report.getReporterUUID().toString());
                statement.setString(4, report.getReporterName());
                statement.setString(5, report.getReason());
                statement.setString(6, report.getServer());
                statement.setLong(7, report.getTimestamp());

                statement.executeUpdate();

                // Obter ID gerado
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao salvar denúncia: " + e.getMessage());
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Obtém todas as denúncias pendentes
     * @return Lista de denúncias pendentes
     */
    public List<Report> getPendingReports() {
        List<Report> reports = new ArrayList<>();

        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }

            String query = "SELECT * FROM " + tablePrefix + "reports WHERE resolved = FALSE ORDER BY timestamp DESC";

            try (PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    Report report = new Report(
                            UUID.fromString(resultSet.getString("reported_uuid")),
                            resultSet.getString("reported_name"),
                            UUID.fromString(resultSet.getString("reporter_uuid")),
                            resultSet.getString("reporter_name"),
                            resultSet.getString("reason"),
                            resultSet.getString("server"),
                            resultSet.getLong("timestamp")
                    );

                    report.setId(resultSet.getInt("id"));
                    report.setResolved(resultSet.getBoolean("resolved"));

                    String resolvedBy = resultSet.getString("resolved_by");
                    if (resolvedBy != null) {
                        report.setResolvedBy(UUID.fromString(resolvedBy));
                    }

                    reports.add(report);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao obter denúncias pendentes: " + e.getMessage());
            e.printStackTrace();
        }

        return reports;
    }

    /**
     * Obtém o histórico de denúncias de um jogador
     * @param playerUUID UUID do jogador
     * @return Lista de denúncias do jogador
     */
    public List<Report> getPlayerReportHistory(UUID playerUUID) {
        List<Report> reports = new ArrayList<>();

        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }

            String query = "SELECT * FROM " + tablePrefix + "reports WHERE reported_uuid = ? ORDER BY timestamp DESC";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, playerUUID.toString());

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        Report report = new Report(
                                UUID.fromString(resultSet.getString("reported_uuid")),
                                resultSet.getString("reported_name"),
                                UUID.fromString(resultSet.getString("reporter_uuid")),
                                resultSet.getString("reporter_name"),
                                resultSet.getString("reason"),
                                resultSet.getString("server"),
                                resultSet.getLong("timestamp")
                        );

                        report.setId(resultSet.getInt("id"));
                        report.setResolved(resultSet.getBoolean("resolved"));

                        String resolvedBy = resultSet.getString("resolved_by");
                        if (resolvedBy != null) {
                            report.setResolvedBy(UUID.fromString(resolvedBy));
                        }

                        reports.add(report);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao obter histórico de denúncias: " + e.getMessage());
            e.printStackTrace();
        }

        return reports;
    }

    /**
     * Marca uma denúncia como resolvida
     * @param reportId ID da denúncia
     * @param resolvedBy UUID do staff que resolveu
     * @return true se a operação foi bem-sucedida
     */
    public boolean resolveReport(int reportId, UUID resolvedBy) {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }

            String query = "UPDATE " + tablePrefix + "reports SET resolved = TRUE, resolved_by = ? WHERE id = ?";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, resolvedBy.toString());
                statement.setInt(2, reportId);

                return statement.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao marcar denúncia como resolvida: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Deleta uma denúncia
     * @param reportId ID da denúncia
     * @return true se a operação foi bem-sucedida
     */
    public boolean deleteReport(int reportId) {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }

            String query = "DELETE FROM " + tablePrefix + "reports WHERE id = ?";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, reportId);

                return statement.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao deletar denúncia: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Verifica se um jogador está bloqueado de fazer denúncias
     * @param playerUUID UUID do jogador
     * @return true se o jogador estiver bloqueado
     */
    public boolean isPlayerBlocked(UUID playerUUID) {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }

            String query = "SELECT blocked_until FROM " + tablePrefix + "trust WHERE player_uuid = ?";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, playerUUID.toString());

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        long blockedUntil = resultSet.getLong("blocked_until");
                        return blockedUntil > System.currentTimeMillis();
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao verificar bloqueio de jogador: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Incrementa o contador de denúncias falsas de um jogador
     * @param playerUUID UUID do jogador
     * @param playerName Nome do jogador
     * @return Número atual de denúncias falsas
     */
    public int incrementFalseReports(UUID playerUUID, String playerName) {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }

            // Verificar se o jogador já existe na tabela
            String checkQuery = "SELECT false_reports FROM " + tablePrefix + "trust WHERE player_uuid = ?";
            try (PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {
                checkStatement.setString(1, playerUUID.toString());

                try (ResultSet resultSet = checkStatement.executeQuery()) {
                    if (resultSet.next()) {
                        // Jogador existe, incrementar contador
                        int falseReports = resultSet.getInt("false_reports") + 1;

                        String updateQuery = "UPDATE " + tablePrefix + "trust SET false_reports = ? WHERE player_uuid = ?";
                        try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                            updateStatement.setInt(1, falseReports);
                            updateStatement.setString(2, playerUUID.toString());
                            updateStatement.executeUpdate();
                        }

                        // Verificar se deve bloquear o jogador
                        if (falseReports >= plugin.getConfigManager().getMaxFalseReports()) {
                            long blockTime = System.currentTimeMillis() + (plugin.getConfigManager().getTrustBlockTime() * 60 * 1000L);
                            String blockQuery = "UPDATE " + tablePrefix + "trust SET blocked_until = ? WHERE player_uuid = ?";
                            try (PreparedStatement blockStatement = connection.prepareStatement(blockQuery)) {
                                blockStatement.setLong(1, blockTime);
                                blockStatement.setString(2, playerUUID.toString());
                                blockStatement.executeUpdate();
                            }
                        }

                        return falseReports;
                    } else {
                        // Jogador não existe, criar registro
                        String insertQuery = "INSERT INTO " + tablePrefix + "trust (player_uuid, player_name, false_reports) VALUES (?, ?, 1)";
                        try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                            insertStatement.setString(1, playerUUID.toString());
                            insertStatement.setString(2, playerName);
                            insertStatement.executeUpdate();
                        }

                        return 1;
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao incrementar denúncias falsas: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Incrementa o contador de denúncias válidas de um jogador
     * @param playerUUID UUID do jogador
     * @param playerName Nome do jogador
     * @return Número atual de denúncias válidas
     */
    public int incrementValidReports(UUID playerUUID, String playerName) {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }

            // Verificar se o jogador já existe na tabela
            String checkQuery = "SELECT valid_reports FROM " + tablePrefix + "trust WHERE player_uuid = ?";
            try (PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {
                checkStatement.setString(1, playerUUID.toString());

                try (ResultSet resultSet = checkStatement.executeQuery()) {
                    if (resultSet.next()) {
                        // Jogador existe, incrementar contador
                        int validReports = resultSet.getInt("valid_reports") + 1;

                        String updateQuery = "UPDATE " + tablePrefix + "trust SET valid_reports = ? WHERE player_uuid = ?";
                        try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                            updateStatement.setInt(1, validReports);
                            updateStatement.setString(2, playerUUID.toString());
                            updateStatement.executeUpdate();
                        }

                        return validReports;
                    } else {
                        // Jogador não existe, criar registro
                        String insertQuery = "INSERT INTO " + tablePrefix + "trust (player_uuid, player_name, valid_reports) VALUES (?, ?, 1)";
                        try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                            insertStatement.setString(1, playerUUID.toString());
                            insertStatement.setString(2, playerName);
                            insertStatement.executeUpdate();
                        }

                        return 1;
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao incrementar denúncias válidas: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Verifica se um jogador reportou outro recentemente
     * @param reporterUUID UUID do jogador que reportou
     * @param reportedUUID UUID do jogador reportado
     * @param cooldownMinutes Tempo de cooldown em minutos
     * @return true se o jogador reportou recentemente
     */
    public boolean hasReportedRecently(UUID reporterUUID, UUID reportedUUID, int cooldownMinutes) {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }

            long cooldownTime = System.currentTimeMillis() - (cooldownMinutes * 60 * 1000L);

            String query = "SELECT COUNT(*) FROM " + tablePrefix + "reports "
                    + "WHERE reporter_uuid = ? AND reported_uuid = ? AND timestamp > ?";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, reporterUUID.toString());
                statement.setString(2, reportedUUID.toString());
                statement.setLong(3, cooldownTime);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao verificar cooldown de denúncia: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Obtém o número de denúncias de um jogador
     * @param playerUUID UUID do jogador
     * @return Número de denúncias
     */
    public int getReportCount(UUID playerUUID) {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }

            String query = "SELECT COUNT(*) FROM " + tablePrefix + "reports WHERE reported_uuid = ?";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, playerUUID.toString());

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao obter contagem de denúncias: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }
}
package com.spirit.threport.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Classe que representa uma denúncia no sistema
 */
public class Report {

    private int id;
    private final UUID reportedUUID;
    private final String reportedName;
    private final UUID reporterUUID;
    private final String reporterName;
    private final String reason;
    private final String server;
    private final long timestamp;
    private boolean resolved;
    private UUID resolvedBy;

    /**
     * Construtor para criar uma nova denúncia
     * 
     * @param reportedUUID UUID do jogador denunciado
     * @param reportedName Nome do jogador denunciado
     * @param reporterUUID UUID do jogador que reportou
     * @param reporterName Nome do jogador que reportou
     * @param reason Motivo da denúncia
     * @param server Servidor onde a denúncia foi feita
     * @param timestamp Data/hora da denúncia
     */
    public Report(UUID reportedUUID, String reportedName, UUID reporterUUID, String reporterName, String reason, String server, long timestamp) {
        this.reportedUUID = reportedUUID;
        this.reportedName = reportedName;
        this.reporterUUID = reporterUUID;
        this.reporterName = reporterName;
        this.reason = reason;
        this.server = server;
        this.timestamp = timestamp;
        this.resolved = false;
    }

    /**
     * Construtor para criar uma nova denúncia com timestamp atual
     * 
     * @param reportedUUID UUID do jogador denunciado
     * @param reportedName Nome do jogador denunciado
     * @param reporterUUID UUID do jogador que reportou
     * @param reporterName Nome do jogador que reportou
     * @param reason Motivo da denúncia
     * @param server Servidor onde a denúncia foi feita
     */
    public Report(UUID reportedUUID, String reportedName, UUID reporterUUID, String reporterName, String reason, String server) {
        this(reportedUUID, reportedName, reporterUUID, reporterName, reason, server, System.currentTimeMillis());
    }

    /**
     * Obtém o ID da denúncia
     * @return ID da denúncia
     */
    public int getId() {
        return id;
    }

    /**
     * Define o ID da denúncia
     * @param id ID da denúncia
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Obtém o UUID do jogador denunciado
     * @return UUID do jogador denunciado
     */
    public UUID getReportedUUID() {
        return reportedUUID;
    }

    /**
     * Obtém o nome do jogador denunciado
     * @return Nome do jogador denunciado
     */
    public String getReportedName() {
        return reportedName;
    }

    /**
     * Obtém o UUID do jogador que reportou
     * @return UUID do jogador que reportou
     */
    public UUID getReporterUUID() {
        return reporterUUID;
    }

    /**
     * Obtém o nome do jogador que reportou
     * @return Nome do jogador que reportou
     */
    public String getReporterName() {
        return reporterName;
    }

    /**
     * Obtém o motivo da denúncia
     * @return Motivo da denúncia
     */
    public String getReason() {
        return reason;
    }

    /**
     * Obtém o servidor onde a denúncia foi feita
     * @return Servidor onde a denúncia foi feita
     */
    public String getServer() {
        return server;
    }

    /**
     * Obtém a data/hora da denúncia
     * @return Data/hora da denúncia em milissegundos
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Verifica se a denúncia foi resolvida
     * @return true se a denúncia foi resolvida
     */
    public boolean isResolved() {
        return resolved;
    }

    /**
     * Define se a denúncia foi resolvida
     * @param resolved true se a denúncia foi resolvida
     */
    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    /**
     * Obtém o UUID do staff que resolveu a denúncia
     * @return UUID do staff que resolveu a denúncia
     */
    public UUID getResolvedBy() {
        return resolvedBy;
    }

    /**
     * Define o UUID do staff que resolveu a denúncia
     * @param resolvedBy UUID do staff que resolveu a denúncia
     */
    public void setResolvedBy(UUID resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    /**
     * Obtém a data formatada da denúncia
     * @param format Formato da data
     * @return Data formatada
     */
    public String getFormattedDate(String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(new Date(timestamp));
    }
}
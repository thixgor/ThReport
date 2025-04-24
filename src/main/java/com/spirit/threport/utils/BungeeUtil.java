package com.spirit.threport.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Utilitário para integração com BungeeCord
 */
public class BungeeUtil {

    private static String serverName = null;
    private static boolean initialized = false;

    /**
     * Inicializa o utilitário BungeeCord
     */
    public static void initialize() {
        if (initialized) {
            return;
        }

        // Registrar canal de mensagens BungeeCord
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(Bukkit.getPluginManager().getPlugin("ThReport"), "BungeeCord");
        
        // Obter nome do servidor
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            out.writeUTF("GetServer");
            
            // Enviar mensagem para o primeiro jogador online (se houver)
            if (!Bukkit.getOnlinePlayers().isEmpty()) {
                Player player = Bukkit.getOnlinePlayers().iterator().next();
                player.sendPluginMessage(Bukkit.getPluginManager().getPlugin("ThReport"), "BungeeCord", b.toByteArray());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        initialized = true;
    }

    /**
     * Define o nome do servidor
     * @param name Nome do servidor
     */
    public static void setServerName(String name) {
        serverName = name;
    }

    /**
     * Obtém o nome do servidor
     * @return Nome do servidor ou "default" se não estiver definido
     */
    public static String getServerName() {
        if (serverName == null) {
            // Tentar obter o nome do servidor de outras formas
            try {
                // Verificar se estamos usando Paper/Spigot com suporte a BungeeCord
                if (Bukkit.getServer().spigot().getConfig().getBoolean("settings.bungeecord")) {
                    return "bungee-" + Bukkit.getPort();
                }
            } catch (Exception ignored) {
                // Ignorar exceções e usar o valor padrão
            }
            
            return "default";
        }
        return serverName;
    }

    /**
     * Envia um jogador para outro servidor na rede BungeeCord
     * @param player Jogador a ser enviado
     * @param server Servidor de destino
     */
    public static void sendToServer(Player player, String server) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            out.writeUTF("Connect");
            out.writeUTF(server);
            player.sendPluginMessage(Bukkit.getPluginManager().getPlugin("ThReport"), "BungeeCord", b.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Verifica se o servidor está rodando em modo BungeeCord
     * @return true se estiver em modo BungeeCord
     */
    public static boolean isBungeeMode() {
        try {
            return Bukkit.getServer().spigot().getConfig().getBoolean("settings.bungeecord");
        } catch (Exception e) {
            return false;
        }
    }
}
package com.spirit.threport.managers;

import com.spirit.threport.ThReport;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {

    private final ThReport plugin;
    private final Map<String, FileConfiguration> languages;
    private String defaultLanguage;

    public LanguageManager(ThReport plugin) {
        this.plugin = plugin;
        this.languages = new HashMap<>();
        loadLanguages();
    }

    /**
     * Carrega todos os arquivos de idioma disponíveis
     */
    private void loadLanguages() {
        // Limpar idiomas carregados anteriormente
        languages.clear();
        
        // Obter idioma padrão das configurações
        defaultLanguage = plugin.getConfigManager().getDefaultLanguage();
        
        // Carregar arquivos de idioma da pasta lang
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }
        
        // Garantir que os arquivos de idioma existam
        saveDefaultLanguageFile("pt_BR.yml");
        saveDefaultLanguageFile("en_US.yml");
        
        // Carregar todos os arquivos .yml na pasta lang
        File[] langFiles = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (langFiles != null) {
            for (File file : langFiles) {
                String langCode = file.getName().replace(".yml", "");
                FileConfiguration langConfig = YamlConfiguration.loadConfiguration(file);
                languages.put(langCode, langConfig);
                plugin.getLogger().info("Idioma carregado: " + langCode);
            }
        }
        
        // Verificar se o idioma padrão foi carregado
        if (!languages.containsKey(defaultLanguage)) {
            plugin.getLogger().warning("Idioma padrão '" + defaultLanguage + "' não encontrado! Usando 'pt_BR' como fallback.");
            defaultLanguage = "pt_BR";
            
            // Se pt_BR também não existir, usar o primeiro idioma disponível
            if (!languages.containsKey(defaultLanguage) && !languages.isEmpty()) {
                defaultLanguage = languages.keySet().iterator().next();
            }
        }
    }
    
    /**
     * Salva um arquivo de idioma do JAR se ele não existir
     * @param fileName Nome do arquivo de idioma
     */
    private void saveDefaultLanguageFile(String fileName) {
        File langFile = new File(plugin.getDataFolder() + File.separator + "lang", fileName);
        if (!langFile.exists()) {
            plugin.saveResource("lang/" + fileName, false);
            plugin.getLogger().info("Arquivo de idioma " + fileName + " foi criado.");
        }
    }
    

    /**
     * Obtém uma mensagem no idioma do jogador
     * @param player Jogador
     * @param path Caminho da mensagem
     * @return Mensagem formatada
     */
    public String getMessage(Player player, String path) {
        String langCode = defaultLanguage;
        
        // Usar idioma do cliente se configurado
        if (player != null && plugin.getConfigManager().useClientLocale()) {
            // Compatibilidade com Spigot 1.8 - não possui método getLocale()
            // Tentar obter o idioma através de reflection ou usar o padrão
            try {
                // Verificar se o método existe (versões mais recentes do Spigot)
                Method localeMethod = player.getClass().getMethod("getLocale");
                if (localeMethod != null) {
                    String playerLocale = (String) localeMethod.invoke(player);
                    if (playerLocale != null && !playerLocale.isEmpty()) {
                        // Converter formato do cliente (en_us) para formato do arquivo (en_US)
                        playerLocale = playerLocale.replace('_', '-');
                        String[] parts = playerLocale.split("-");
                        if (parts.length >= 2) {
                            playerLocale = parts[0].toLowerCase() + "_" + parts[1].toUpperCase();
                            
                            // Verificar se o idioma está disponível
                            if (languages.containsKey(playerLocale)) {
                                langCode = playerLocale;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Método não existe ou falhou, usar idioma padrão
                // Não logar erro para evitar spam no console
            }
        }
        
        return getMessage(langCode, path);
    }

    /**
     * Obtém uma mensagem em um idioma específico
     * @param langCode Código do idioma
     * @param path Caminho da mensagem
     * @return Mensagem formatada
     */
    public String getMessage(String langCode, String path) {
        // Verificar se o idioma existe
        if (!languages.containsKey(langCode)) {
            langCode = defaultLanguage;
        }
        
        FileConfiguration lang = languages.get(langCode);
        String message = lang.getString(path);
        
        // Se a mensagem não existir no idioma, tentar no idioma padrão
        if (message == null && !langCode.equals(defaultLanguage)) {
            lang = languages.get(defaultLanguage);
            message = lang.getString(path);
        }
        
        // Se ainda for nulo, retornar o caminho
        if (message == null) {
            return "&cMensagem não encontrada: " + path;
        }
        
        // Formatar cores
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Obtém uma mensagem com placeholders substituídos
     * @param player Jogador
     * @param path Caminho da mensagem
     * @param placeholders Placeholders para substituir (chave, valor)
     * @return Mensagem formatada com placeholders substituídos
     */
    public String getMessage(Player player, String path, Object... placeholders) {
        String message = getMessage(player, path);
        
        // Substituir placeholders
        if (placeholders != null && placeholders.length > 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                if (i + 1 < placeholders.length) {
                    String placeholder = String.valueOf(placeholders[i]);
                    String value = String.valueOf(placeholders[i + 1]);
                    message = message.replace("{" + placeholder + "}", value);
                }
            }
        }
        
        return message;
    }

    /**
     * Recarrega todos os arquivos de idioma
     */
    public void reloadLanguages() {
        loadLanguages();
    }

    /**
     * Obtém o idioma padrão
     * @return Código do idioma padrão
     */
    public String getDefaultLanguage() {
        return defaultLanguage;
    }
}
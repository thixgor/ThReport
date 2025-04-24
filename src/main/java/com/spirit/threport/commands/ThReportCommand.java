package com.spirit.threport.commands;

import com.spirit.threport.ThReport;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Comando administrativo para gerenciar o plugin ThReport
 */
public class ThReportCommand implements CommandExecutor, TabCompleter {

    private final ThReport plugin;
    private final Map<String, String> helpMessages;

    /**
     * Construtor
     * @param plugin Instância do plugin
     */
    public ThReportCommand(ThReport plugin) {
        this.plugin = plugin;
        this.helpMessages = new HashMap<>();
        setupHelpMessages();
    }

    /**
     * Configura as mensagens de ajuda para cada comando
     */
    private void setupHelpMessages() {
        helpMessages.put("help", "&7Mostra a lista de comandos disponíveis");
        helpMessages.put("reload", "&7Recarrega as configurações do plugin");
        helpMessages.put("mysql", "&7Verifica se a conexão com MySQL está ativa");
        helpMessages.put("mode", "&7Define o tipo de interface (GUI/CHAT) para um comando");
        helpMessages.put("cooldown", "&7Define o tempo de cooldown entre reports");
        helpMessages.put("reasons add", "&7Adiciona uma nova razão de report");
        helpMessages.put("reasons description", "&7Modifica a descrição de uma razão");
        helpMessages.put("reasons remove", "&7Remove uma razão de report");
        helpMessages.put("reasons list", "&7Lista todas as razões de report disponíveis");
        helpMessages.put("reasons item", "&7Define o item associado a uma razão de report");
        helpMessages.put("creditos", "&7Mostra os créditos do plugin");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Verificar permissão
        if (!sender.hasPermission("threport.admin")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage((Player) null, "geral.sem_permissao"));
            return true;
        }

        // Sem argumentos: mostrar ajuda
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        // Processar subcomandos
        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                showHelp(sender);
                break;
            case "reload":
                reloadPlugin(sender);
                break;
            case "mysql":
                checkMySql(sender);
                break;
            case "creditos":
            case "credits":
                showCredits(sender);
                break;
            case "mode":
                if (args.length < 3) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage((Player) null, "geral.prefixo") + 
                            ChatColor.RED + "Uso correto: /threport mode <interface-type|reports-interface-type|reportlog-interface-type> <GUI|CHAT>");
                    return true;
                }
                setInterfaceMode(sender, args[1], args[2]);
                break;
            case "cooldown":
                if (args.length < 2) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage((Player) null, "geral.prefixo") + 
                            ChatColor.RED + "Uso correto: /threport cooldown <tempo em minutos>");
                    return true;
                }
                setCooldown(sender, args[1]);
                break;
            case "reasons":
                if (args.length < 2) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage((Player) null, "geral.prefixo") + 
                            ChatColor.RED + "Uso correto: /threport reasons <add|description|remove|list|item> [argumentos]");
                    return true;
                }
                handleReasonsCommand(sender, args);
                break;
            default:
                sender.sendMessage(plugin.getLanguageManager().getMessage((Player) null, "geral.prefixo") + 
                        ChatColor.RED + "Comando desconhecido. Use /threport help para ver os comandos disponíveis.");
                break;
        }

        return true;
    }

    /**
     * Mostra a ajuda do comando
     * @param sender Remetente do comando
     */
    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ThReport" + ChatColor.GRAY + " - Comandos Administrativos");
        sender.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        
        sender.sendMessage(ChatColor.YELLOW + "/threport help" + ChatColor.GRAY + " - Mostra esta ajuda");
        sender.sendMessage(ChatColor.YELLOW + "/threport reload" + ChatColor.GRAY + " - Recarrega as configurações do plugin");
        sender.sendMessage(ChatColor.YELLOW + "/threport mysql" + ChatColor.GRAY + " - Verifica se a conexão com MySQL está ativa");
        sender.sendMessage(ChatColor.YELLOW + "/threport mode <tipo> <GUI|CHAT>" + ChatColor.GRAY + " - Define o tipo de interface");
        sender.sendMessage(ChatColor.YELLOW + "/threport cooldown <minutos>" + ChatColor.GRAY + " - Define o tempo de cooldown");
        sender.sendMessage(ChatColor.YELLOW + "/threport reasons add <id> <nome>" + ChatColor.GRAY + " - Adiciona uma razão");
        sender.sendMessage(ChatColor.YELLOW + "/threport reasons description <id> <descrição>" + ChatColor.GRAY + " - Define a descrição");
        sender.sendMessage(ChatColor.YELLOW + "/threport reasons remove <id>" + ChatColor.GRAY + " - Remove uma razão");
        sender.sendMessage(ChatColor.YELLOW + "/threport reasons list" + ChatColor.GRAY + " - Lista todas as razões");
        sender.sendMessage(ChatColor.YELLOW + "/threport reasons item <id> <item>" + ChatColor.GRAY + " - Define o item da razão");
        sender.sendMessage(ChatColor.YELLOW + "/threport creditos" + ChatColor.GRAY + " - Mostra os créditos do plugin");
        
        sender.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
    }

    /**
     * Recarrega as configurações do plugin
     * @param sender Remetente do comando
     */
    private void reloadPlugin(CommandSender sender) {
        // Recarregar configurações
        plugin.getConfigManager().reloadConfigs();
        
        sender.sendMessage(plugin.getLanguageManager().getMessage((Player) null, "geral.prefixo") + 
                ChatColor.GREEN + "Configurações recarregadas com sucesso!");
    }

    /**
     * Verifica se a conexão com MySQL está ativa
     * @param sender Remetente do comando
     */
    private void checkMySql(CommandSender sender) {
        boolean mysqlEnabled = plugin.getConfigManager().isUsingMySQL();
        
        if (mysqlEnabled) {
            boolean connected = plugin.getDatabaseManager() != null;
            
            if (connected) {
                sender.sendMessage(plugin.getLanguageManager().getMessage((Player) null, "geral.prefixo") + 
                        ChatColor.GREEN + "MySQL está ativado e conectado.");
            } else {
                sender.sendMessage(plugin.getLanguageManager().getMessage((Player) null, "geral.prefixo") + 
                        ChatColor.YELLOW + "MySQL está ativado, mas não está conectado.");
            }
        } else {
            sender.sendMessage(plugin.getLanguageManager().getMessage((Player) null, "geral.prefixo") + 
                    ChatColor.YELLOW + "MySQL não está ativado. Usando armazenamento local.");
        }
    }

    /**
     * Define o tipo de interface para um comando
     * @param sender Remetente do comando
     * @param interfaceType Tipo de interface a ser modificado
     * @param mode Modo (GUI ou CHAT)
     */
    private void setInterfaceMode(CommandSender sender, String interfaceType, String mode) {
        // Verificar se o tipo de interface é válido
        if (!interfaceType.equals("interface-type") && 
            !interfaceType.equals("reports-interface-type") && 
            !interfaceType.equals("reportlog-interface-type")) {
            
            sender.sendMessage(plugin.getLanguageManager().getMessage((Player) null, "geral.prefixo") + 
                    ChatColor.RED + "Tipo de interface inválido. Use: interface-type, reports-interface-type ou reportlog-interface-type");
            return;
        }
        
        // Verificar se o modo é válido
        mode = mode.toUpperCase();
        if (!mode.equals("GUI") && !mode.equals("CHAT")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage((Player) null, "geral.prefixo") + 
                    ChatColor.RED + "Modo inválido. Use: GUI ou CHAT");
            return;
        }
        
        // Atualizar configuração
        FileConfiguration config = plugin.getConfigManager().getConfig();
        config.set("settings." + interfaceType, mode);
        plugin.getConfigManager().saveConfigs();
        
        sender.sendMessage(plugin.getLanguageManager().getMessage((Player) null, "geral.prefixo") + 
                ChatColor.GREEN + "Tipo de interface " + ChatColor.YELLOW + interfaceType + 
                ChatColor.GREEN + " definido para " + ChatColor.YELLOW + mode);
    }

    /**
     * Define o tempo de cooldown entre reports
     * @param sender Remetente do comando
     * @param cooldownStr Tempo de cooldown em minutos
     */
    private void setCooldown(CommandSender sender, String cooldownStr) {
        int cooldown;
        
        try {
            cooldown = Integer.parseInt(cooldownStr);
            
            if (cooldown < 0) {
                sender.sendMessage(plugin.getLanguageManager().getMessage((Player) null, "geral.prefixo") + 
                        ChatColor.RED + "O tempo de cooldown não pode ser negativo.");
                return;
            }
            
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getLanguageManager().getMessage((Player) null, "geral.prefixo") + 
                    ChatColor.RED + "Tempo de cooldown inválido. Use um número inteiro.");
            return;
        }
        
        // Atualizar configuração
        FileConfiguration config = plugin.getConfigManager().getConfig();
        config.set("settings.cooldown", cooldown);
        plugin.getConfigManager().saveConfigs();
        
        if (cooldown == 0) {
            sender.sendMessage(plugin.getLanguageManager().getMessage((Player) null, "geral.prefixo") + 
                    ChatColor.YELLOW + "Cooldown entre reports foi desativado.");
        } else {
            sender.sendMessage(plugin.getLanguageManager().getMessage((Player) null, "geral.prefixo") + 
                    ChatColor.GREEN + "Tempo de cooldown definido para " + 
                    ChatColor.YELLOW + cooldown + " minuto(s).");
        }
    }

    /**
     * Gerencia os comandos relacionados às razões de report
     * @param sender Remetente do comando
     * @param args Argumentos do comando
     */
    private void handleReasonsCommand(CommandSender sender, String[] args) {
        String subCommand = args[1].toLowerCase();
        
        switch (subCommand) {
            case "list":
                listReasons(sender);
                break;
            case "add":
                if (args.length < 4) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage((Player) null, "geral.prefixo") + 
                            ChatColor.RED + "Uso correto: /threport reasons add <id> <nome>");
                    return;
                }
                addReason(sender, args[2], String.join(" ", Arrays.copyOfRange(args, 3, args.length)));
                break;
            case "description":
                if (args.length < 4) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage((Player) null, "geral.prefixo") + 
                            ChatColor.RED + "Uso correto: /threport reasons description <id> <descrição>");
                    return;
                }
                setReasonDescription(sender, args[2], String.join(" ", Arrays.copyOfRange(args, 3, args.length)));
                break;
            case "remove":
                if (args.length < 3) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage((Player) null, "geral.prefixo") + 
                            ChatColor.RED + "Uso correto: /threport reasons remove <id>");
                    return;
                }
                removeReason(sender, args[2]);
                break;
            case "item":
                if (args.length < 4) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage((Player) null, "geral.prefixo") + 
                            ChatColor.RED + "Uso correto: /threport reasons item <id> <item>");
                    return;
                }
                setReasonItem(sender, args[2], args[3]);
                break;
            default:
                sender.sendMessage(plugin.getLanguageManager().getMessage((Player) null, "geral.prefixo") + 
                        ChatColor.RED + "Subcomando desconhecido. Use /threport help para ver os comandos disponíveis.");
                break;
        }
    }

    /**
     * Lista todas as razões de report
     * @param sender Remetente do comando
     */
    private void listReasons(CommandSender sender) {
        ConfigurationSection reasonsSection = plugin.getConfigManager().getReportReasonsSection();
        
        if (reasonsSection == null || reasonsSection.getKeys(false).isEmpty()) {
            sender.sendMessage(plugin.getLanguageManager().getMessage((Player) null, "geral.prefixo") + 
                    ChatColor.YELLOW + "Não há razões de report configuradas.");
            return;
        }
        
        sender.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ThReport" + ChatColor.GRAY + " - Razões de Report");
        sender.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        
        int id = 1;
        for (String reasonId : reasonsSection.getKeys(false)) {
            String name = reasonsSection.getString(reasonId + ".name", "");
            String description = reasonsSection.getString(reasonId + ".description", "");
            String item = reasonsSection.getString(reasonId + ".item", "PAPER");
            
            sender.sendMessage(ChatColor.YELLOW + "" + id + ". " + ChatColor.WHITE + reasonId + 
                    ChatColor.GRAY + " - " + ChatColor.WHITE + name);
            sender.sendMessage(ChatColor.GRAY + "   Descrição: " + ChatColor.WHITE + description);
            sender.sendMessage(ChatColor.GRAY + "   Item: " + ChatColor.WHITE + item);
            sender.sendMessage("");
            
            id++;
        }
        
        sender.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
    }

    /**
     * Adiciona uma nova razão de report
     * @param sender Remetente do comando
     * @param reasonId ID da razão
     * @param name Nome da razão
     */
    private void addReason(CommandSender sender, String reasonId, String name) {
        // Verificar se o ID já existe
        ConfigurationSection reasonsSection = plugin.getConfigManager().getReportReasonsSection();
        
        if (reasonsSection != null && reasonsSection.contains(reasonId)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage((Player) null, "geral.prefixo") + 
                    ChatColor.RED + "Já existe uma razão com o ID '" + reasonId + "'.");
            return;
        }
        
        // Adicionar nova razão
        FileConfiguration config = plugin.getConfigManager().getConfig();
        config.set("report-reasons." + reasonId + ".name", name);
        config.set("report-reasons." + reasonId + ".description", "Descrição padrão");
        config.set("report-reasons." + reasonId + ".item", "PAPER");
        plugin.getConfigManager().saveConfigs();
        
        sender.sendMessage(plugin.getLanguageManager().getMessage((Player) null, "geral.prefixo") + 
                ChatColor.GREEN + "Razão de report " + ChatColor.YELLOW + reasonId + 
                ChatColor.GREEN + " adicionada com sucesso!");
    }

    /**
     * Define a descrição de uma razão de report
     * @param sender Remetente do comando
     * @param reasonId ID da razão
     * @param description Descrição da razão
     */
    private void setReasonDescription(CommandSender sender, String reasonId, String description) {
        // Verificar se o ID existe
        ConfigurationSection reasonsSection = plugin.getConfigManager().getReportReasonsSection();
        
        if (reasonsSection == null || !reasonsSection.contains(reasonId)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage((Player) null, "geral.prefixo") + 
                    ChatColor.RED + "Não existe uma razão com o ID '" + reasonId + "'.");
            return;
        }
        
        // Atualizar descrição
        FileConfiguration config = plugin.getConfigManager().getConfig();
        config.set("report-reasons." + reasonId + ".description", description);
        plugin.getConfigManager().saveConfigs();
        
        sender.sendMessage(plugin.getLanguageManager().getMessage((Player) null, "geral.prefixo") + 
                ChatColor.GREEN + "Descrição da razão " + ChatColor.YELLOW + reasonId + 
                ChatColor.GREEN + " atualizada com sucesso!");
    }

    /**
     * Remove uma razão de report
     * @param sender Remetente do comando
     * @param reasonId ID da razão
     */
    private void removeReason(CommandSender sender, String reasonId) {
        // Verificar se o ID existe
        ConfigurationSection reasonsSection = plugin.getConfigManager().getReportReasonsSection();
        
        if (reasonsSection == null || !reasonsSection.contains(reasonId)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage((Player) null, "geral.prefixo") + 
                    ChatColor.RED + "Não existe uma razão com o ID '" + reasonId + "'.");
            return;
        }
        
        // Remover razão
        FileConfiguration config = plugin.getConfigManager().getConfig();
        config.set("report-reasons." + reasonId, null);
        plugin.getConfigManager().saveConfigs();
        
        sender.sendMessage(plugin.getLanguageManager().getMessage((Player) null, "geral.prefixo") + 
                ChatColor.GREEN + "Razão de report " + ChatColor.YELLOW + reasonId + 
                ChatColor.GREEN + " removida com sucesso!");
    }

    /**
     * Define o item de uma razão de report
     * @param sender Remetente do comando
     * @param reasonId ID da razão
     * @param itemName Nome do item
     */
    private void setReasonItem(CommandSender sender, String reasonId, String itemName) {
        // Verificar se o ID existe
        ConfigurationSection reasonsSection = plugin.getConfigManager().getReportReasonsSection();
        
        if (reasonsSection == null || !reasonsSection.contains(reasonId)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage((Player) null, "geral.prefixo") + 
                    ChatColor.RED + "Não existe uma razão com o ID '" + reasonId + "'.");
            return;
        }
        
        // Verificar se o item é válido
        try {
            Material.valueOf(itemName.toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(plugin.getLanguageManager().getMessage((Player) null, "geral.prefixo") + 
                    ChatColor.RED + "Item inválido: '" + itemName + "'. Use um material válido do Minecraft.");
            return;
        }
        
        // Atualizar item
        FileConfiguration config = plugin.getConfigManager().getConfig();
        config.set("report-reasons." + reasonId + ".item", itemName.toUpperCase());
        plugin.getConfigManager().saveConfigs();
        
        sender.sendMessage(plugin.getLanguageManager().getMessage((Player) null, "geral.prefixo") + 
                ChatColor.GREEN + "Item da razão " + ChatColor.YELLOW + reasonId + 
                ChatColor.GREEN + " atualizado para " + ChatColor.YELLOW + itemName.toUpperCase() + 
                ChatColor.GREEN + " com sucesso!");
    }

    /**
     * Mostra os créditos do plugin
     * @param sender Remetente do comando
     */
    private void showCredits(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ThReport" + ChatColor.GRAY + " - Créditos");
        sender.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "Plugin desenvolvido por: " + ChatColor.WHITE + "Spirit");
        sender.sendMessage(ChatColor.YELLOW + "Ano de criação: " + ChatColor.WHITE + "2025");
        sender.sendMessage(ChatColor.YELLOW + "Última versão: " + ChatColor.WHITE + "24 de abril de 2025");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.AQUA + "Obrigado por utilizar o ThReport!");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!sender.hasPermission("threport.admin")) {
            return completions;
        }
        
        if (args.length == 1) {
            // Primeiro argumento: subcomandos principais
            String[] subCommands = {"help", "reload", "mysql", "mode", "cooldown", "reasons", "creditos", "credits"};
            return filterCompletions(subCommands, args[0]);
        } else if (args.length == 2) {
            // Segundo argumento: depende do primeiro
            switch (args[0].toLowerCase()) {
                case "mode":
                    String[] interfaceTypes = {"interface-type", "reports-interface-type", "reportlog-interface-type"};
                    return filterCompletions(interfaceTypes, args[1]);
                case "reasons":
                    String[] reasonsCommands = {"add", "description", "remove", "list", "item"};
                    return filterCompletions(reasonsCommands, args[1]);
            }
        } else if (args.length == 3) {
            // Terceiro argumento: depende dos anteriores
            if (args[0].toLowerCase().equals("mode")) {
                String[] modes = {"GUI", "CHAT"};
                return filterCompletions(modes, args[2]);
            } else if (args[0].toLowerCase().equals("reasons")) {
                if (args[1].toLowerCase().equals("remove") || 
                    args[1].toLowerCase().equals("description") || 
                    args[1].toLowerCase().equals("item")) {
                    
                    // Listar IDs de razões existentes
                    ConfigurationSection reasonsSection = plugin.getConfigManager().getReportReasonsSection();
                    if (reasonsSection != null) {
                        return filterCompletions(
                                reasonsSection.getKeys(false).toArray(new String[0]), 
                                args[2]);
                    }
                }
            }
        } else if (args.length == 4) {
            // Quarto argumento: depende dos anteriores
            if (args[0].toLowerCase().equals("reasons") && 
                args[1].toLowerCase().equals("item")) {
                
                // Listar materiais válidos
                return filterCompletions(
                        Arrays.stream(Material.values())
                              .map(Material::name)
                              .toArray(String[]::new), 
                        args[3]);
            }
        }
        
        return completions;
    }
    
    /**
     * Filtra as opções de autocompletar com base no prefixo digitado
     * @param options Opções disponíveis
     * @param prefix Prefixo digitado
     * @return Lista filtrada de opções
     */
    private List<String> filterCompletions(String[] options, String prefix) {
        return Arrays.stream(options)
                .filter(option -> option.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
}

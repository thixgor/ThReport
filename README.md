# <div align="center">ThReport - Sistema Avançado de Denúncias</div>

<div align="center">
  <img src="https://i.imgur.com/1O4X8Jd.png" alt="Logo ThReport" width="200">
</div>

## ✨ Funcionalidades

<div style="background-color: #f8f9fa; padding: 15px; border-radius: 5px; border-left: 4px solid #4285f4;">
  <ul>
    <li>📝 <b>Múltiplos Métodos de Denúncia</b>: Interface gráfica e por chat</li>
    <li>🌍 <b>Suporte a Idiomas</b>: Padrão pt_BR, com sistema fácil de tradução</li>
    <li>💾 <b>Banco de Dados</b>: MySQL para armazenamento persistente</li>
    <li>⚙️ <b>Totalmente Configurável</b>: Motivos de denúncia e mensagens personalizáveis</li>
    <li>🔍 <b>Ferramentas para Moderadores</b>: Visualize e gerencie denúncias</li>
  </ul>
</div>

## 📦 Instalação

1. Baixe a versão mais recente em [Releases](#)
2. Coloque o `ThReport-pre-alpha-rd-20250424.jar` na pasta `plugins` do seu servidor
3. Reinicie o servidor
4. Configure o plugin em `plugins/ThReport/config.yml`

## 🎮 Comandos
| `/report` | Abre o menu de denúncias 
| `/reports` | Visualiza denúncias ativas
| `/reportlog` | Histórico de denúncias 
| `/threport reload` | Recarrega as configurações

## ⚙️ Configurações Principais

O plugin possui 3 arquivos principais:

1. `config.yml` - Configurações gerais:
```yaml
# Idioma padrão (pt_BR, en_US)
default-language: pt_BR

# Motivos de denúncia customizáveis
report-reasons:
  hacking: "Hacking/Cheating"
  spam: "Spam no Chat"
  assedio: "Assédio"
```

2. `messages.yml` - Todas as mensagens do plugin
3. `lang/pt_BR.yml` - Traduções para português brasileiro

## 🌍 Idiomas Suportados

O plugin já vem com:
- Português Brasileiro (pt_BR.yml) - PADRÃO
- Inglês (en_US.yml)

Para adicionar novos idiomas, basta criar um arquivo `lang/novo_idioma.yml` baseado no modelo existente.

## 📜 Licença

Creative Commons - CC BY-NC 4.0 - veja o arquivo [LICENSE](LICENSE) para detalhes.
(Atribuição + Uso Não Comercial)

<div align="center">
  <img src="https://i.imgur.com/jtb0wQH.png" alt="ThReport Icon" width="200">
  <p>Desenvolvido por Spirit</p>
</div>

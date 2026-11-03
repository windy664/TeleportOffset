<div align="center">

<img src="assets/banner.svg" alt="TeleportOffset" width="100%">

# TeleportOffset

**传送偏移与救援** —— 管理和调整玩家传送行为：传送自动偏移、虚空坠落救援、卡地底弹回，双语玩家可开调试。

![Platform](https://img.shields.io/badge/Platform-Spigot%20%2F%20Paper-62bda8?style=flat-square&logo=minecraft&logoColor=white)
![Minecraft](https://img.shields.io/badge/MC-1.20.x-8a9a5b?style=flat-square)
![Java](https://img.shields.io/badge/Java-15%2B-f89820?style=flat-square&logo=openjdk&logoColor=white)

[功能](#-功能) · [命令](#-命令) · [配置](#️-配置) · [安装](#-安装)

</div>

TeleportOffset 是一个 Minecraft 服务器插件，用于管理和调整玩家的传送偏移。
A Minecraft server plugin for managing and adjusting player teleport offsets.

## ✨ 功能

- **传送偏移**：玩家传送时自动按 `teleport-offset`（x/y/z）调整落点
- **虚空救援**：`void.enable` 开启后，玩家坠落到 `y` 阈值以下时执行救援命令（如 `tp %player_name% -18 60 -37`）
- **卡地底弹回**：`PlayerLower` / `PlayerStuck` 监听器处理玩家卡在方块里的情况
- **世界黑名单**：`Disabled-world` 列出的世界不生效
- **调试模式**：`/teleportoffset debug` 实时观察偏移判定

## 📜 命令

| 命令 | 说明 |
|------|------|
| `/teleportoffset reload` | 重载插件配置 |
| `/teleportoffset debug` | 开关调试模式 |

## ⚙️ 配置

```yaml
teleport-offset:
  x: 0
  y: 3
  z: 0
Disabled-world:
  - world
  - spawn
Deviation: 1
times: 2
void:
  enable: true
  y: -40                # 低于此高度触发救援
  worlds:
    - world
  commands:
    - tp %player_name% -18 60 -37
```

## 📦 安装

1. 将 TeleportOffset 插件文件下载到你的服务器的 `plugins` 文件夹。
2. 重启服务器，或使用 `/reload` 命令加载插件。

## 🔨 构建

```bash
mvn package
```

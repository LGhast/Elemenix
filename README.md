# 元质学 Elemenics ⚗️

> 将万物解构为六种元质，再随心所欲地重构它们。
> *Deconstruct everything into six kinds of Elemenix, then reconstruct them as you wish.*

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-green)](https://www.mcmod.cn/class/22852.html)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1.235-blue)](https://neoforged.net/)
[![CurseForge](https://img.shields.io/badge/CurseForge-Elemenics-orange)](https://www.curseforge.com/minecraft/mc-mods/elemenics)
[![MC百科](https://img.shields.io/badge/MC百科-元质学-red)](https://www.mcmod.cn/class/22852.html)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

**元质学（Elemenics）** 是一款以「物质分解与重构」为核心的科技模组。玩家可以将物品转化为 6 种不同的元质，再将元质重构为物品。用六种不同的「货币」限制物品间的转换，创造更平衡的玩法。

**Elemenics** is a tech mod about decomposition and reconstruction: convert items into 6 kinds of Elemenix, then reconstruct the Elemenix back into items.

---

## ✨ 功能特性

### 元质
宇宙间的一切造物，皆由六种元质交织而成：

| 元质 | 内涵     |
|---|--------|
| 🧬 有机质 | 编纺血肉草木 |
| ⛰️ 矿化质 | 构筑大地根基 |
| 💧 流动质 | 流转水气魂灵 |
| ⚙️ 金属质 | 铸就金银钢铁 |
| 🔥 激发质 | 迸发光热能量 |
| 🔮 神秘质 | 蕴藏无尽未知 |

## 📥 下载与安装

| 渠道 | 链接 |
|---|---|
| CurseForge | https://www.curseforge.com/minecraft/mc-mods/elemenics |
| MC百科 | https://www.mcmod.cn/class/22852.html |
| GitHub Releases | https://github.com/LGhast/Elemenix/releases |

**环境要求**
- Minecraft **1.21.1**（仅此版本）
- NeoForge **21.1.235+**
- **前置：Curios（饰品栏 API）**
- 推荐：JEI（查看配方）
- 可选联动：FTB Quests、Jade、Lootr

## 🔗 联动与兼容

- **预设元质构成**：已为原版物品、[农夫乐事](https://www.mcmod.cn/class/3219.html)、[暮色森林](https://www.mcmod.cn/class/61.html) 等多个模组的物品预设；
- **自动计算**：提供基础物品的元质后，模组会根据配方自动推算合成产物的元质组成，支持：
  - 原版：合成、熔炼、切石、锻造
  - 农夫乐事 / 地牢乐事 / 机械动力 / 铁砧工艺 / 新生魔艺 / 灾变 / AE2 及其扩展 / 方块宝可梦 / 天境 / 汇流来世 / 森罗物语 / 魔法食事 / 诡厄巫法 等模组的数十种配方类型（详见 MC 百科页）

## 🌍 本地化

已内置：**English、简体中文、繁體中文、日本語、Français、Русский**。
欢迎通过 Pull Request 贡献更多语言或修正现有翻译。

## 🔨 从源码构建

```bash
# 需要 JDK 21
git clone https://github.com/LGhast/Elemenix.git
cd Elemenix
git checkout 1.21.1
./gradlew build
# 产物位于 build/libs/
```

## 📜 许可证

本项目采用 **MIT 许可证**，详见 [LICENSE](LICENSE) 文件。

## 💬 反馈与开发状态

- 问题反馈：[GitHub Issues](https://github.com/LGhast/Elemenix/issues)
- 本模组目前处于**早期开发阶段**：各物品的元质平衡性仍需根据反馈持续优化；自动计算功能对个别模组配方可能出现异常。
- This mod is currently in the **early development stage**: the elemenic balance of each item still needs ongoing optimization based on feedback; the automatic calculation feature may behave abnormally with certain mod recipes.

---
*Made with ❤️ by [LGhast](https://github.com/LGhast)*

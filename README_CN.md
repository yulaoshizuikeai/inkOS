<div style="text-align: center;">
	<img src="docs/img/inkos.svg" alt="inkOS logo" height="48">
	<h2>inkOS - 专为 E-ink 墨水屏与按键设备打造的纯文本极简 Android 启动器</h2>
    <table>
        <tr>
            <td><img src='fastlane/metadata/android/en-US/images/phoneScreenshots/0.png' height='300' alt=""></td>
            <td><img src='fastlane/metadata/android/en-US/images/phoneScreenshots/1.png' height='300' alt=""></td>
            <td><img src='fastlane/metadata/android/en-US/images/phoneScreenshots/2.png' height='300' alt=""></td>
            <td><img src='fastlane/metadata/android/en-US/images/phoneScreenshots/3.png' height='300' alt=""></td>
        </tr>
    </table>
</div>

[English](README.md) | [简体中文](README_CN.md) | [文档说明](DOCUMENTATION.md)

---

# 📱 兼容性与支持设备 (Android 8.0 - 16)

inkOS 经过专门优化，非常适合墨水屏、备用机以及物理按键/键盘手机：

- 文石 Onyx、汉王、掌阅 iReader、小米多看等墨水屏阅读器/手机
- 多田 Mudita Kompakt
- 钦 Qin F21 Pro / F22 / F22 Pro
- 喵喵机 / 极简按键手机 / Cat S22 Flip
- LightPhone 3

---

# 🚀 二次开发新增核心功能 (Features)

### 🌤️ 国内优先 Weather 天气系统 (全新)
- **零 GPS 权限 & IP 模糊定位**：通过 IP 自动识别所在城市，无需开启定位权限，充分保护个人隐私。
- **国内极速数据源**：优先调用太平洋电脑网 IP 定位与中国天气网 (etouch) 高速 CDN 气象接口，毫秒级响应。
- **E-Ink 高对比度视觉**：大字显示温度、气象描述（晴/多云/雷阵雨/雪等）与清晰图标，完美支持文字岛背景卡片。
- **快捷交互与设置**：主页底部挂件一键切换为 Weather，支持点击立即刷新、摄氏度 (°C) / 华氏度 (°F) 切换及自定义城市重写。

### 🌐 多语言与本地化支持 (全新)
- **全量中文支持**：内置完整的 **简体中文 (`zh-CN`)** 与 **繁體中文 (`zh-TW`)** 800+ 词条翻译。
- **应用内独立语言切换**：在设置 -> 高级设置中支持自由选择 **跟随系统 / English / 简体中文 / 繁體中文**，解决部分墨水屏系统无法修改语言的痛点。

---

### 🎨 核心桌面特性

**主屏幕 (Home Screen)**
- 支持多页主屏与指示点导航。
- 9 种精美时钟样式：默认、翻页 (Flip)、方块 (Boxed)、圆形 (Round)、分栏 (Split)、横向 (Horizontal)、线框 (Box Outline)、表盘指针 (Analog)、堆叠 (Stacked)。
- 双时钟显示，支持自定义时区时差。
- 独立对齐控制：时钟、日期、应用列表、底部挂件各自支持居左/居中/居右对齐。
- 8 种底部挂件类型：天气系统、自定义金句/文本、日历日程、Android 原生小组件、快捷按键、屏幕使用时长、指示点或关闭。
- 媒体播放挂件：播放音乐时自动弹出，支持上/下一曲、暂停与停止。
- 嵌入任意 Android 原生 Widget，支持动态调节高度与左右边距。
- 实时编辑模式 (Edit Mode)：长按或点击元素即可在屏幕上直观调整设置。

**应用抽屉与多源搜索 (App Drawer & Search)**
- 右侧 A-Z 字母快捷定位导航栏。
- 多种排序法则：按字母 A-Z、按使用频率、按最后使用时间。
- 全能多源搜索：搜索应用、联系人、搜索引擎、系统设置、本地音乐及文件。
- 唯一搜索结果自动启动，支持进入抽屉自动弹出软键盘。
- 应用管理菜单：卸载、重命名、隐藏应用、指纹/密码应用锁、查看应用详情。
- 独立支持应用快捷方式 (App Shortcuts) 及第三方应用固定快捷方式。

**字体与排版 (Fonts & Typography)**
- 独立字体定制：时钟、日期、应用名称、挂件、通知、设置菜单均可独立设置字体及字号大小。
- 全局通用字体模式：一键为所有元素应用同一种字体。
- 支持从手机存储导入自定义 `.ttf` / `.otf` 字体文件。
- 应用名称大小写切换：标准 (Normal)、全小写 (lowercase)、全大写 (UPPERCASE)。

**外观与主题 (Themes & Look)**
- 15 种一键预设主题，支持浅色、深色及跟随系统模式。
- 独立防缩放壁纸系统，支持透明度滑块调节，避免墨水屏残影。
- 4 种图标模式：首字母图标、系统原生图标、单色印记 (Tinted)、第三方图标包。
- 文字岛卡片 (Text Islands)：为文字元素提供胶囊/圆角背景衬底，支持颜色反转。
- 主题配置导入与导出 (JSON)。

**手势与按键 (Gestures & Physical Buttons)**
- 4 方向划动手势 + 双击 + 时钟/日期/挂件点击，均可绑定 15+ 种快捷动作。
- 可选快捷动作：打开应用、抽屉、通知阅读器、最近任务、控制中心、一键刷屏、亮度调节、锁屏、电源菜单、重启、切换私密空间等。
- 音量键翻页：专为墨水屏及按键手机优化的音量加减键翻页功能。
- 边缘划过返回手势。

**通知系统 (Notifications)**
- 应用名称旁星号 (*) 未读通知标记。
- 文本通知预览：在主页应用下方直接展示最新消息文本。
- Letters：全屏通知阅读器，支持上下翻页与键盘/D-Pad 快捷键。
- Simple Tray：分页式通知托盘。
- Hub：设备状态仪表盘（电池、WiFi、蓝牙、存储、亮度、静音模式）。

**墨水屏与硬件优化 (E-Ink & Hardware)**
- 退出应用后自动刷屏（消除残影，可调延迟）。
- 4 种墨水屏显示模式：关闭、高对比度、清晰模式、阅读模式。
- 完整支持 T9、D-pad 及 QWERTY 物理键盘导航。

---

## 🛠️ 技术栈 (Built With)

| 组件 | 详细信息 |
|---|---|
| **开发语言** | Kotlin 2.1.20 |
| **UI 框架** | Jetpack Compose 1.10.4, Material3 1.4.0 |
| **构建系统** | AGP 8.10.1, Gradle 8.x |
| **目标 SDK** | API 26 - 36 (Android 8.0 - 16) |

---

## 📄 许可证 (License)

本项目在 **GPLv3** 开源许可证下发布。您可以自由使用、学习、修改及分发。

# Blank Icon Pack Template

极简 Android 图标包模板 APK 工程 — 专为 Electron 桌面端图标包生成器设计的 APK 模板。

## 为什么不用 CandyBar / React Native？

| 对比维度 | CandyBar | React Native | 本模板 |
|---------|----------|-------------|--------|
| 依赖复杂度 | Blueprint 仪表盘库，大量第三方依赖 | Node 引擎 + Metro bundler | 仅 AndroidX + Material Design |
| APK 大小 | 大（包含 CandyBar 所有资源） | 大（包含 RN 运行时） | 极小（< 3MB） |
| apktool 兼容性 | 差（CandyBar 内部引用复杂，反编译后难以修改） | 极差（JS bundle 混淆） | 优（原生 Java，无混淆） |
| 二次开发 | 需阅读 CandyBar 源码 | 需阅读 RN 桥接代码 | 直接修改 XML / Asset |
| 自定义 UI | 受限（CandyBar 框架约束） | 灵活但复杂 | 完全自主（自绘 Material UI） |

## 项目结构

```
blank-icon-pack-template/
├── .github/workflows/build-template-apk.yml   # GitHub Actions 编译
├── README.md
├── settings.gradle
├── build.gradle
├── gradle.properties
├── gradle/wrapper/gradle-wrapper.properties
├── app/
│   ├── build.gradle                            # 依赖: AndroidX, Material, RecyclerView
│   └── src/main/
│       ├── AndroidManifest.xml                  # 完整类名，无复杂 Provider
│       ├── java/com/template/iconpack/
│       │   ├── MainActivity.java                # Drawer + Fragment 导航
│       │   ├── ui/fragments/                    # 9 个页面 Fragment
│       │   ├── ui/adapters/                     # 6 个 RecyclerView Adapter
│       │   ├── utils/                           # 工具类
│       │   └── models/                          # 数据模型
│       ├── res/
│       │   ├── values/         (strings, colors, themes, dimens)
│       │   ├── xml/            (appfilter.xml, drawable.xml 模板)
│       │   ├── raw/            (appfilter.xml 副本)
│       │   ├── drawable-nodpi/ (placeholder.png)
│       │   ├── drawable/       (vector icons)
│       │   ├── layout/         (activity + fragments + items)
│       │   ├── menu/           (drawer_menu.xml)
│       │   └── mipmap-*/       (ic_launcher.png)
│       └── assets/
│           ├── appfilter.xml    (模板)
│           ├── drawable.xml     (模板)
│           ├── wallpapers.json  (空数组)
│           └── presets.json     (默认 4 个预设)
```

## 页面功能一览

| 页面 | 描述 |
|------|------|
| 首页 Dashboard | 卡片网格：应用 / 捐赠 / 图标数量 / 自适应 / 申请统计 / 壁纸 / 更多 |
| 应用图标 Apply | Launcher 列表（Nova, Lawnchair, Microsoft 等），点击提示手动应用 |
| 图标 Icons | 读取 drawable.xml，网格展示 + 搜索，空状态提示 |
| 申请图标 Request | 扫描已安装应用，appfilter 交叉匹配，筛选/导出/分享 |
| 壁纸 Wallpapers | 读取 wallpapers.json，网格展示，预留设置/下载 |
| 预设 Presets | 读取 presets.json，UI 预留（默认圆形/圆角矩形/深色/浅色） |
| 设置 Settings | 深色模式 / 显示图标名 / 列数 / 清缓存 / 重载 |
| 常见问题 FAQ | 4 个常见问题，展开/收起 |
| 关于 About | 应用信息 + 开源许可 + 分享 |

## 通过 GitHub Actions 编译模板 APK

### 方式 1：手动触发（推荐）

1. 打开你的 GitHub 仓库页面
2. 点击顶部 **Actions** 标签
3. 左侧选择 **Build Blank Icon Pack Template APK**
4. 点击右侧 **Run workflow** 按钮
5. 可选择输入版本号（默认 `1.0.0`）
6. 点击绿色 **Run workflow** 确认
7. 等待编译完成（约 3-5 分钟）
8. 在 workflow run 页面底部 **Artifacts** 区域下载 `blank-icon-pack-template.apk`

### 方式 2：Push 自动触发

推送到 `main` 或 `master` 分支时自动编译（跳过 README/.gitignore 等纯文档变更）。

### 本地编译

```bash
# 需要 JDK 17 + Android SDK
./gradlew :app:assembleDebug --no-daemon

# 输出路径（debug 签名，可直接安装测试）
# app/build/outputs/apk/debug/app-debug.apk
```

## Electron 使用此模板的完整流程

```bash
# 1. 复制模板 APK
cp blank-icon-pack-template.apk /tmp/work/

# 2. apktool 反编译
apktool d /tmp/work/blank-icon-pack-template.apk -o /tmp/work/decompiled/

# 3. 修改应用名
# 编辑 /tmp/work/decompiled/res/values/strings.xml
# 修改 <string name="app_name">Icon Pack Template</string> 为你的应用名

# 4. 修改包名（可选，但强烈建议）
# 编辑 /tmp/work/decompiled/AndroidManifest.xml
# 将 package="com.template.iconpack" 改为你的包名
# ⚠️ 注意：不要修改 Activity 的完整类名
# ⚠️ 无需处理 DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION：
#    模板已在编译时通过 tools:node="remove" 剥离了该权限，
#    反编译后的 manifest 中不会出现这个权限项。
# Activity android:name="com.template.iconpack.MainActivity" 保持不变
# apktool 在重新编译时会自动处理包名映射

# 5. 复制用户图标到 drawable-nodpi
cp /path/to/user/icons/*.png /tmp/work/decompiled/res/drawable-nodpi/

# 6. 重写 appfilter.xml（ComponentInfo -> drawable 映射）
cat > /tmp/work/decompiled/res/xml/appfilter.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <item component="ComponentInfo{com.tencent.mm/com.tencent.mm.ui.LauncherUI}" drawable="wechat" />
    <item component="ComponentInfo{com.tencent.mobileqq/com.tencent.mobileqq.activity.SplashActivity}" drawable="qq" />
    <!-- ... 更多映射 ... -->
</resources>
EOF

# 7. 重写 drawable.xml（图标名称列表）
cat > /tmp/work/decompiled/res/xml/drawable.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <drawable name="wechat" />
    <drawable name="qq" />
    <!-- ... 更多图标 ... -->
</resources>
EOF

# 8. 同步 res/raw/appfilter.xml
cp /tmp/work/decompiled/res/xml/appfilter.xml /tmp/work/decompiled/res/raw/appfilter.xml

# 9. 同步 assets/appfilter.xml
cp /tmp/work/decompiled/res/xml/appfilter.xml /tmp/work/decompiled/assets/appfilter.xml

# 10. 同步 assets/drawable.xml
cp /tmp/work/decompiled/res/xml/drawable.xml /tmp/work/decompiled/assets/drawable.xml

# 11. apktool 重新打包（❗ 必须使用 --use-aapt2）
apktool b /tmp/work/decompiled/ -o /tmp/work/output-unsigned.apk --use-aapt2

# 12. 签名（需要 keystore）
# 生成签名密钥（首次）：
keytool -genkey -v -keystore my-key.keystore -alias my-alias \
    -keyalg RSA -keysize 2048 -validity 10000

# 签名 APK：
apksigner sign --ks my-key.keystore \
    --out /tmp/work/output-signed.apk \
    /tmp/work/output-unsigned.apk

# zipalign 优化：
zipalign -v 4 /tmp/work/output-signed.apk /tmp/work/final.apk
```

### 为什么 apktool 必须用 `--use-aapt2`？

本项目使用 AGP 8.2.0 / AAPT2 编译。apktool 默认使用 AAPT1 重新打包，两者分配的资源 ID（R 类中的整数值）不同。但 smali 字节码里已经硬编码了 AAPT2 分配的原始 ID，用 AAPT1 重新编译会导致 `ResourceNotFoundException` 或 `NullPointerException` → **闪退**。

`--use-aapt2` 让 apktool 用和原始构建相同的 AAPT2 工具分配资源 ID，确保 ID 一致。

```bash
#  正确（不闪退）
apktool b decompiled/ -o output.apk --use-aapt2

#  错误（可能闪退）
apktool b decompiled/ -o output.apk
```

## 关键设计决策

### 1. Manifest Activity 使用完整类名

```xml
<!-- ✅ 正确：完整类名，改包名后不会被破坏 -->
<activity android:name="com.template.iconpack.MainActivity" ... />

<!-- ❌ 错误：相对类名，改包名后可能 ClassNotFoundException -->
<activity android:name=".MainActivity" ... />
```

### 2. 无资源混淆

- `minifyEnabled false`
- `shrinkResources false`
- 不使用 ProGuard 混淆规则
- 所有资源 ID 保持可读

### 3. 无复杂 Provider

- 不使用 `FileProvider`
- 不使用 `AndroidX Startup`
- 不使用自定义权限
- 仅使用 `QUERY_ALL_PACKAGES`（扫描已安装应用所需）

### 4. 最小化第三方依赖

```
androidx.appcompat:appcompat         (防崩溃兼容)
androidx.drawerlayout:drawerlayout   (侧边栏)
androidx.recyclerview:recyclerview   (列表)
com.google.android.material:material (UI 组件)
androidx.constraintlayout            (未使用，预留)
```

## appfilter.xml 格式

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <item
        component="ComponentInfo{com.tencent.mm/com.tencent.mm.ui.LauncherUI}"
        drawable="wechat" />
    <item
        component="ComponentInfo{com.alibaba.android.rimet/com.alibaba.android.rimet.biz.LaunchHomeActivity}"
        drawable="dingtalk" />
</resources>
```

## drawable.xml 格式

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <drawable name="wechat" />
    <drawable name="dingtalk" />
    <drawable name="alipay" />
</resources>
```

## wallpapers.json 格式

```json
[
    {
        "id": "wall_001",
        "title": "星空",
        "thumbnailUrl": "https://example.com/thumb/star.jpg",
        "downloadUrl": "https://example.com/full/star.jpg",
        "author": "Photographer Name"
    }
]
```

## presets.json 格式

```json
[
    { "id": "default_circle", "name": "默认圆形", "iconShape": "circle" },
    { "id": "rounded_square", "name": "圆角矩形", "iconShape": "rounded_square" },
    { "id": "dark_bg", "name": "深色背景", "iconShape": "circle", "background": "dark" },
    { "id": "light_bg", "name": "浅色背景", "iconShape": "circle", "background": "light" }
]
```

## 技术栈

- Java 17
- Android SDK 35 (target) / 23 (min)
- AndroidX AppCompat
- Material Design Components
- View Binding
- Gradle 8.5 + AGP 8.2.0

## 许可

MIT License — 可自由用于商业和非商业项目。

# VETRO: Cyberpunk Foldable Clock

_"Time is not linear. It folds."_

## アプリ画面(新)

<table>
<tr>
<td align="center">
<img src="docs/img/vetro-cyberpunk_1.png" alt="cyberpunk(縦)" height="500">
<br>
<sub>cyberpunk(縦)</sub>
</td>
<td align="center">
<img src="docs/img/vetro-cyberpunk_2.png" alt="折りたたみ時" height="500">
<br>
<sub>cyberpunk(折りたたみ)</sub>
</td>
<td align="center">
<img src="docs/img/vetro-minimal.png" alt="minimal" height="500">
<br>
<sub>minimal(横)</sub>
</td>
</tr>
</table>

## 01. SYSTEM OVERVIEW (概要)

**Vetro** は、razr 60 Ultra などの「縦折りたたみスマートフォン」に特化して設計された、Android ネイティブ時計アプリケーションです。

Verified Environment: razr 60 Ultra / Android 15

単なる時刻表示アプリではありません。端末の物理的な形状（開閉状態）を `Jetpack WindowManager` で検知し、UI そのものを物理形状に合わせて変形させる **"Adaptive Cyberpunk Interface"** を搭載しています。

## 02. CORE MODULES (主な機能)

本システムは、端末のハードウェア状態に応じて 2 つのモードを自動で切り替えます。

### Mode A: "Tabletop" (折りたたみ時)

端末を L 字型に折って机に置くモード。

- **UI:** 画面を上下に分割し、上部に視認性の高い時計、下部にステータスモニターを配置。
- **Use Case:** デスクワーク中の置時計、ベッドサイドクロック。

### Mode B: "Monolith" (全開時)

端末を開いた縦長（アスペクト比 22:9）のモード。

- **UI:** フォントを縦方向にスタック（積み上げ）配置。
- **Design:** 超縦長画面で発生しがちな「余白の崩れ」を防ぎ、画面全体を情報量で埋め尽くすタワー型レイアウト。

## 03. TECH STACK (技術構成)

開発にはモダンな Android 開発技術を採用しています。

- **UI Toolkit:** Jetpack Compose (Material3)
- **Foldable Support:** `androidx.window:window` (WindowManager API)
- **Architecture:** MVVM (Model-View-ViewModel) + Clean Architecture based
- **Fonts Strategy:**
  - `Orbitron` (Bold/Medium): High-Tech なメイン時刻表示
  - `BBH Bartle`: Industrial なシステムログ・装飾表示

## 04. ENGINEERING CHALLENGES (技術的なこだわり)

### フォルダブル対応の最適化

通常のレスポンシブ対応（幅に応じた変化）だけでなく、`FoldingFeature` を用いて「ヒンジ（折り目）」の位置を正確に計算。 物理的な折り目と UI の境界線が完全に一致するように `Spacer` の高さを動的に制御しています。

### "生きた" UI 表現

静止画のような UI ではなく、常に稼働しているデバイス感を出すために以下を実装。

- **Real-time Monitor:** `BatteryManager` と `MemoryInfo` から実数値を取得し表示。
- **Neon Glow Effect:** 標準の Shadow ではなく、Canvas API (`Paint` + `BlurMaskFilter`) を用いて、芯のあるネオン発光を描画。

_Vetro - Time is not linear. It folds._

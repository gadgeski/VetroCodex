# Vetro - Glassmorphism Clock

**Vetro**（ヴェトロ: イタリア語で「ガラス」）は、Android デバイスを「高級なデジタルインテリア」へと変貌させる、グラスモーフィズムデザインの時計アプリです。

システム壁紙を動的に取得し、すりガラスのようなブラー効果と光の反射をリアルタイムで合成することで、**「あなたの壁紙に溶け込む時計」** を実現しました。

## Key Features

- **Real-time Glassmorphism**: Android 12 の`RenderEffect`を活用し、背景画像を動的にぼかして透過させる高度な UI 表現
- **System Wallpaper Integration**: ユーザーが設定している壁紙を自動取得し、アプリの背景として継承
- **High-Quality Widget**: Canvas API によるカスタム描画を行い、ホーム画面でもアプリ同様の「ガラスの質感」と「カスタムフォント」を再現
- **Always-on Display (Screensaver)**: 充電中に自動起動するスクリーンセーバー（Daydream）に対応
- **Burn-in Protection**: 有機 EL ディスプレイ保護のため、1 分ごとに表示位置をランダムにピクセル単位でシフト

## 🛠 Tech Stack

- **Language**: Kotlin
- **UI Toolkit**: Jetpack Compose (Material3)
- **Widget**: Jetpack Glance + Custom Bitmap Rendering
- **Architecture**: MVVM (Model-View-ViewModel)
- **Async**: Coroutines, Flow
- **Background Tasks**: WorkManager, AlarmManager
- **Dependency Injection**: (将来的に Hilt 導入予定)
- **Build System**: Gradle (Version Catalogs / libs.versions.toml)

## 💡 Technical Challenges & Solutions

開発において直面した技術的な課題と、その解決策について解説します。

### 1. ウィジェットにおける「ガラス表現」と「カスタムフォント」の再現

**課題:**

通常の`AppWidget`や`Glance`の標準コンポーネントだけでは、システム壁紙を透過させた「すりガラス表現」や「極細のカスタムフォント」を描画することが技術的に不可能でした。

**解決策:**

ウィジェットの描画戦略として **「Bitmap 生成方式」** を採用しました。バックグラウンドで`Canvas`と`Paint`を使用して、「壁紙の切り抜き」「ブラー処理」「テキスト描画」「光沢エフェクト」を 1 枚の Bitmap として合成し、それを`Glance`の`Image`コンポーネントに渡すことで、アプリ本体と遜色ないリッチなデザインをホーム画面で実現しました。

### 2. 課題と運用の変更

**課題:**

<s>当初は`WorkManager`を使用して定期更新を行っていましたが、Android の省電力仕様により更新間隔が不正確で、時計として致命的な「数分のズレ」が発生しました

更新ロジックを`AlarmManager`の`setExactAndAllowWhileIdle`に移行しました。毎分 00 秒に正確に発火するアラームをセットし、`BroadcastReceiver`内で`goAsync()`を使用してプロセスを維持しながら描画更新を行うことで、バッテリー消費を抑えつつ **「秒単位で正確な時計」** を実現しました。</s>

**調べた内容:**

–「ウィジェットだけで・画像を差し替えて・秒単位で時刻にピッタリ合わせるリッチ時計」--
・OS の更新制限（30 分以上・Doze など)
・RemoteViews を介した描画パイプライン
・バッテリー制約
これらの理由から、ズレやラグなしで動かすのは現実的ではありません。
なので、充電時のスクリーンセーバーによる運用に切り替えました。

### 3. Android 14 以降の権限管理

**課題:**

壁紙を取得するために画像へのアクセス権限が必要ですが、Android 14 から導入された Selected Photos Access により、権限周りの挙動が複雑化しました。

**解決策:**

`READ_MEDIA_VISUAL_USER_SELECTED`権限に対応しつつ、アプリのコア機能（自動での壁紙取得）には広範なアクセスが必要であることをマニフェスト内で明記。ユーザー体験を損なわない権限フローを設計しました。

---

_Vetro - Transform your Android device into a premium digital interior_

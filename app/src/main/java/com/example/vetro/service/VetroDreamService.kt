// app/src/main/java/com/example/vetro/service/VetroDreamService.kt

package com.example.vetro.service

import android.service.dreams.DreamService
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.vetro.ui.screen.ClockScreen
import com.example.vetro.ui.theme.VetroTheme

/**
 * Androidのスクリーンセーバー (Daydream) として動作するサービス
 *
 * ActivityではなくServiceコンテキストでJetpack ComposeとViewModelを使用するために、
 * LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner を手動で実装しています。
 *
 * これにより、充電開始時に自動的に起動し、リッチなUIを提供することが可能になります。
 */
class VetroDreamService : DreamService(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    // 1. ComposeとViewModelをホストするための管理者オブジェクト
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val viewModelStore: ViewModelStore
        get() = store

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        // 状態の復元とライフサイクルの初期化
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        // スクリーンセーバーの設定
        isInteractive = true // タップ操作を有効化
        isFullscreen = true  // ステータスバーを隠す

        // 2. Window（DecorView）に対してオーナーを登録
        // これを行わないと、Compose内部で `viewModel()` や `LocalLifecycleOwner` が機能しません
        window.decorView.let { view ->
            view.setViewTreeLifecycleOwner(this)
            view.setViewTreeViewModelStoreOwner(this)
            view.setViewTreeSavedStateRegistryOwner(this)
        }

        // 3. Compose UIのセットアップ
        setContentView(ComposeView(this).apply {
            // Serviceのライフサイクルに合わせてコンポジションを破棄する設定
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                VetroTheme {
                    ClockScreen()
                }
            }
        })

        // Viewの準備ができたので START 状態へ
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    // ★ Refactor: 実際に画面が表示され、夢を見始めたタイミングで RESUME にする
    override fun onDreamingStarted() {
        super.onDreamingStarted()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    // ★ Refactor: 夢から覚める（画面が消える）タイミングで PAUSE にする
    override fun onDreamingStopped() {
        super.onDreamingStopped()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Windowから外れたら STOP
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 完全に破棄されたら ViewModel もクリアし、DESTROY
        store.clear()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}
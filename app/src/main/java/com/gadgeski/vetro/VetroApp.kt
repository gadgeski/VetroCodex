package com.gadgeski.vetro

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * アプリケーションクラス
 * Hiltを使用するために必須のクラスです。
 * アプリ起動時に一番最初に実行され、DIコンテナの初期化を行います。
 */
@HiltAndroidApp
class VetroApp : Application()
package github.znzsofficial.nekoplayer

import android.app.Application
import com.google.android.material.color.DynamicColors

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        val crashHandler: CrashHandler = CrashHandler.getInstance()
        // 注册crashHandler
        crashHandler.init(applicationContext)
    }
}
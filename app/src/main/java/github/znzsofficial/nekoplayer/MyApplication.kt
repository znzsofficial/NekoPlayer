package github.znzsofficial.nekoplayer

import android.app.Application
import com.google.android.material.color.DynamicColors

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        //if (BuildConfig.DEBUG)
        // 注册crashHandler
        CrashHandler.init(applicationContext)
    }
}
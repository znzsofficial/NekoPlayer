package github.znzsofficial.nekoplayer

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

/**
 * UncaughtException处理类,当程序发生Uncaught异常的时候,有该类来接管程序,并记录发送错误报告.
 *
 * @author user
 */
@SuppressLint("StaticFieldLeak")
object CrashHandler : Thread.UncaughtExceptionHandler {
    //系统默认的UncaughtException处理类 
    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null

    //程序的Context对象
    private var mContext: Context? = null

    //用来存储设备信息和异常信息
    private val infos: MutableMap<String, String> = LinkedHashMap()

    //用于格式化日期,作为日志文件名的一部分
    private val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")

    /**
     * 初始化
     *
     * @param context
     */
    fun init(context: Context?) {
        mContext = context
        //获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        //设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    override fun uncaughtException(thread: Thread, ex: Throwable) {
        if (!handleException(ex) && mDefaultHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler!!.uncaughtException(thread, ex)
        } else {
            /*try
			{
				Thread.sleep(3000);
			}
			catch (InterruptedException e)
			{
				Log.e(TAG, "error : ", e);
			}
			//退出程序
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(0);*/
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private fun handleException(ex: Throwable?): Boolean {
        if (ex == null) {
            return false
        }
        //使用Toast来显示异常信息
        /*new Thread() {
			@Override
			public void run()
			{
				Looper.prepare();
				Toast.makeText(mContext, "很抱歉,程序出现异常,即将退出.", Toast.LENGTH_LONG).show();
				Looper.loop();
			}
		}.start();*/
        //收集设备参数信息 
        collectDeviceInfo(mContext)
        //保存日志文件 
        saveCrashInfo2File(ex)
        return true
    }

    /**
     * 收集设备参数信息
     * @param ctx
     */
    fun collectDeviceInfo(ctx: Context?) {
        try {
            val pm = ctx!!.packageManager
            val pi = pm.getPackageInfo(ctx.packageName, PackageManager.GET_ACTIVITIES)
            if (pi != null) {
                val versionName = if (pi.versionName == null) "null" else pi.versionName
                val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pi.longVersionCode.toString()
                } else {
                    pi.versionCode.toString()
                }
                infos["versionName"] = versionName
                infos["versionCode"] = versionCode
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "an error occurred when collect package info", e)
        }
        var fields = Build::class.java.declaredFields
        for (field in fields) {
            try {
                field.isAccessible = true
                val obj = field[null]
                if (obj is Array<*> && obj.isArrayOf<String>()) infos[field.getName()] =
                    obj.contentToString() else if (obj != null) {
                    infos[field.getName()] =
                        obj.toString()
                }
                Log.d(TAG, field.getName() + " : " + field[null])
            } catch (e: Exception) {
                Log.e(TAG, "an error occured when collect crash info", e)
            }
        }
        fields = Build.VERSION::class.java.declaredFields
        for (field in fields) {
            try {
                field.isAccessible = true
                val obj = field[null]
                if (obj is Array<*> && obj.isArrayOf<String>()) infos[field.getName()] =
                    obj.contentToString() else if (obj != null) {
                    infos[field.getName()] =
                        obj.toString()
                }
                Log.d(TAG, field.getName() + " : " + field[null])
            } catch (e: Exception) {
                Log.e(TAG, "an error occured when collect crash info", e)
            }
        }
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @return 返回文件名称,便于将文件传送到服务器
     */
    private fun saveCrashInfo2File(ex: Throwable): String? {
        val sb = StringBuffer()
        for ((key, value) in infos) {
            sb.append("$key=$value\n")
        }
        val writer: Writer = StringWriter()
        val printWriter = PrintWriter(writer)
        ex.printStackTrace(printWriter)
        var cause = ex.cause
        while (cause != null) {
            cause.printStackTrace(printWriter)
            cause = cause.cause
        }
        printWriter.close()
        val result = writer.toString()
        sb.append(result)
        try {
            val timestamp = System.currentTimeMillis()
            val time = formatter.format(Date())
            val fileName = "crash-$time-$timestamp.log"
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                val path = mContext!!.applicationContext.getExternalFilesDir("")!!
                    .absolutePath + "/crash/"
                val dir = File(path)
                if (!dir.exists()) dir.mkdirs()
                val fos = FileOutputStream(path + fileName)
                //				FileOutputStream fos = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
                fos.write(sb.toString().toByteArray())
                Log.e("crash", sb.toString())
                fos.close()
            }
            return fileName
        } catch (e: Exception) {
            Log.e(TAG, "an error occured while writing file...", e)
        }
        return null
    }

    const val TAG = "CrashHandler"
}

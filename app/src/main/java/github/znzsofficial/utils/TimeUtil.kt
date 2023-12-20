package github.znzsofficial.utils

object TimeUtil {
    /**
     * 将秒单位的数字转为格式化后的字符串
     * @param origin 秒单位的Long
     * @return 格式化后的字符串
     */
    fun toMinSec(origin: Long): String {
        val minutes = origin / 60
        val seconds = origin % 60
        return String.format("%d:%02d", minutes, seconds)
    }
}

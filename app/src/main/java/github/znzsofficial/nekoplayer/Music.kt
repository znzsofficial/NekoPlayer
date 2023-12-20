package github.znzsofficial.nekoplayer

/**
 * @param path 音乐路径
 * @param length 单位为秒的Long
 */
data class Music(val path: String,val title:String, val length: Long,val picture:ByteArray) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Music

        if (path != other.path) return false
        if (length != other.length) return false
        if (!picture.contentEquals(other.picture)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = path.hashCode()
        result = 31 * result + length.hashCode()
        result = 31 * result + (picture.contentHashCode() ?: 0)
        return result
    }
}
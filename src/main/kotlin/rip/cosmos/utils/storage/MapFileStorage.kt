package rip.cosmos.utils.storage

import java.io.FileNotFoundException
import java.util.concurrent.ConcurrentHashMap

class MapFileStorage(
	private val map: ConcurrentHashMap<String, ByteArray> = ConcurrentHashMap()
) : FileStorage {
	override val files: List<String>
		get() = map.keys.toList()

	override val fileMap: Map<String, ByteArray>
		get() = map

	override fun putFile(path: String, data: ByteArray) {
		map[path] = data
	}

	override fun removeFile(path: String): ByteArray =
		map.remove(path) ?: throw FileNotFoundException("File not found")

	override fun findFile(path: String): ByteArray? = map[path]
	override fun iterator(): Iterator<String> = files.iterator()
}
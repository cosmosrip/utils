package rip.cosmos.utils.storage

import java.io.FileNotFoundException

/**
 * Simple file storage
 * There is no concept of directories just paths, when exporting the paths should be split by `/` into directories (flattened paths)
 */
interface FileStorage : Iterable<String> {
	fun getFile(path: String): ByteArray =
		findFile(path) ?: throw FileNotFoundException("\$path\" not found in storage")

	fun putFile(path: String, data: ByteArray)
	val files: List<String>
	val fileMap: Map<String, ByteArray>
	fun removeFile(path: String): ByteArray

	fun moveFile(from: String, to: String) {
		putFile(to, removeFile(from))
	}

	fun findFile(path: String): ByteArray?

	fun transformFile(file: String, cb: (ByteArray) -> ByteArray) {
		putFile(file, cb(getFile(file)))
	}
}

/**
 * Creates a [FileStorage], this may be changed in the future to not always be [MapFileStorage] so don't depend on that :)
 */
fun createStorage(): FileStorage = MapFileStorage()
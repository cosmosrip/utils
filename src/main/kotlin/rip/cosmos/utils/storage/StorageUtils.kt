package rip.cosmos.utils.storage

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import java.io.ByteArrayOutputStream
import java.nio.file.FileSystem
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.io.path.*

/**
 * Reads [bytes] as a zip file into a map with flattened paths
 */
private fun readZip(bytes: ByteArray): ConcurrentHashMap<String, ByteArray> {
	val files = ConcurrentHashMap<String, ByteArray>()
	ZipInputStream(bytes.inputStream()).use {
		var entry: ZipEntry? = it.nextEntry
		while (entry != null) {
			if (entry.isDirectory) {
				entry = it.nextEntry
				continue
			}

			ByteArrayOutputStream().use { output ->
				val buf = ByteArray(2048)
				var len = it.read(buf)
				while (len > 0) {
					output.write(buf, 0, len)
					len = it.read(buf)
				}

				files[entry!!.name] = output.toByteArray()
			}

			entry = it.nextEntry
		}
	}

	return files
}

fun importStorageFromZip(bytes: ByteArray): FileStorage {
	return MapFileStorage(readZip(bytes))
}

fun exportStorageToZip(storage: FileStorage): ByteArray {
	val output = ByteArrayOutputStream()
	ZipOutputStream(output).use { zip ->
		storage.files.forEach {
			zip.putNextEntry(ZipEntry(it))
			zip.write(storage.getFile(it))
			zip.closeEntry()
		}
	}

	return output.toByteArray()
}

private fun visitFile(storage: FileStorage, root: Path, file: Path) {
	if (file.isDirectory()) {
		val children = file.listDirectoryEntries()
		for (f in children) {
			visitFile(storage, root, f)
		}
	} else if (file.isRegularFile()) {
		storage.putFile(file.relativeTo(root).pathString, file.readBytes())
	} else {
		throw IllegalArgumentException("$file is not a file or directory")
	}
}

/**
 * Recursively visits [directory] and creates a storage with all the files found
 */
fun createStorageFromDirectory(directory: Path): FileStorage {
	val storage = createStorage()

	importDirectoryIntoStorage(storage, directory)

	return storage
}

/**
 * Recursively visits [directory] and adds all the files found into a storage, overwiting them if they already exist
 */
fun importDirectoryIntoStorage(storage: FileStorage, directory: Path) {
	require(directory.isDirectory()) { "$directory is not a directory" }
	visitFile(storage, directory, directory)
}

fun FileStorage.asFileSystem(): FileSystem {
	val fs = Jimfs.newFileSystem(Configuration.unix())
	intoFileSystem(fs)
	return fs
}

// TODO: Improve this
fun FileStorage.intoFileSystem(fs: FileSystem) {
	for (file in files) {
		val p = "/$file"
		val path = fs.getPath(p)

		if (file.contains("/")) {
			fs.getPath("/" + file.split("/").dropLast(1).joinToString("/")).createDirectories()
		}

		path.writeBytes(getFile(file))
	}
}
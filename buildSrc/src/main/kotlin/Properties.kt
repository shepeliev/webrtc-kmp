import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.Properties

fun loadProperties(propertiesFile: File) : Properties {
    val properties = Properties()

    if (propertiesFile.isFile) {
        InputStreamReader(FileInputStream(propertiesFile), Charsets.UTF_8).use { reader ->
            properties.load(reader)
        }
    }

    return properties
}

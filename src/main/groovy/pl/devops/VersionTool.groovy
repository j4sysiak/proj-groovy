package pl.devops

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class VersionTool {
    static String getSnapshotVersion(String baseVersion) {
        // Używamy nowoczesnego API daty z Javy
        def datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        return "${baseVersion}-SNAPSHOT-${datePart}"
    }
}
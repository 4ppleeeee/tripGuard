import Foundation

enum IconParser {
    static func parseIconCSS(from cssString: String) -> [String: String] {
        var iconMap: [String: String] = [:]
        let lines = cssString.components(separatedBy: .newlines)

        for (index, line) in lines.enumerated() where line.contains(":before {") {
            let iconName = line
                .trimmingCharacters(in: .whitespaces)
                .replacingOccurrences(of: ".icon-", with: "")
                .replacingOccurrences(of: ":before {", with: "")
                .trimmingCharacters(in: .whitespaces)

            guard index + 1 < lines.count else {
                continue
            }

            let contentLine = lines[index + 1].trimmingCharacters(in: .whitespaces)
            guard contentLine.hasPrefix("content:") else {
                continue
            }

            let unicode = contentLine
                .replacingOccurrences(of: "content:", with: "")
                .replacingOccurrences(of: "\"\\", with: "")
                .replacingOccurrences(of: "\";", with: "")
                .trimmingCharacters(in: .whitespaces)

            iconMap[iconName] = unicode.toIconFont()
        }

        return iconMap
    }

    static func loadAndParseCSS(fileName: String) -> [String: String]? {
        guard let cssURL = Bundle.main.url(forResource: fileName, withExtension: "css"),
              let cssContent = try? String(contentsOf: cssURL, encoding: .utf8) else {
            NSLog("[IconFont] Failed to load CSS file: %@.css", fileName)
            return nil
        }

        return parseIconCSS(from: cssContent)
    }
}

private extension String {
    func toIconFont() -> String {
        guard let codePoint = UInt32(self, radix: 16),
              let unicode = UnicodeScalar(codePoint) else {
            return ""
        }
        return String(unicode)
    }
}

import Foundation

final class IconFontManager {
    static let shared = IconFontManager()

    private(set) var iconMap: [String: String] = [:]

    private init() {
        loadIcons()
    }

    func getUnicode(for name: String) -> String? {
        iconMap[name]
    }

    func getCharacter(for name: String) -> Character? {
        guard let unicode = getUnicode(for: name),
              let scalar = unicode.unicodeScalars.first else {
            return nil
        }
        return Character(scalar)
    }

    private func loadIcons() {
        guard let icons = IconParser.loadAndParseCSS(fileName: "style") else {
            NSLog("[IconFont] Failed to load icon font CSS file")
            return
        }
        iconMap = icons
    }
}

import Foundation
import Security
import umbrella

/// iOS 端 IAppEncoder 实现
/// 提供 Base64 编解码、URL 编解码和 RSA 加密能力，其余加解密/emoji/md5 暂留空
final class IOSAppEncoder: NSObject, IAppEncoder {

    // MARK: - Base64

    func encodeBase64Bytes(bytes: KotlinByteArray) -> String {
        let data = bytes.toData()
        return data.base64EncodedString()
    }

    func decodeBase64(data: String) -> String {
        guard let decodedData = Data(base64Encoded: data),
              let decodedString = String(data: decodedData, encoding: .utf8) else {
            return data
        }
        return decodedString
    }

    // MARK: - URL Encode/Decode

    func urlEncode(data: String) -> String {
        // RFC 3986 非保留字符：A-Z a-z 0-9 - _ . ~
        var allowedCharacters = CharacterSet()
        allowedCharacters.insert(charactersIn: "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_.~")
        return data.addingPercentEncoding(withAllowedCharacters: allowedCharacters) ?? data
    }

    func urlDecodeUtf8(data: String) -> String {
        return data.removingPercentEncoding ?? data
    }

    // MARK: - 留空实现

    func cipherDES(key: String, iv: String, bytes: KotlinByteArray) -> KotlinByteArray {
        return KotlinByteArray(size: 0)
    }

    func cipherAESEncrypt(key: KotlinByteArray, iv: KotlinByteArray, data: KotlinByteArray) -> KotlinByteArray {
        return KotlinByteArray(size: 0)
    }

    func cipherAESDecrypt(key: KotlinByteArray, iv: KotlinByteArray, data: KotlinByteArray) -> KotlinByteArray {
        return KotlinByteArray(size: 0)
    }

    func removeEmoji(text: String?) -> String? {
        return text
    }

    func md5(input: String) -> String {
        return ""
    }

    func rsaEncryptBase64(data: KotlinByteArray, publicKey: String) -> String {
        guard let keyData = Data(base64Encoded: publicKey),
              let secKey = Self.makeRSAPublicKey(from: keyData),
              SecKeyIsAlgorithmSupported(secKey, .encrypt, .rsaEncryptionPKCS1) else {
            return ""
        }
        var error: Unmanaged<CFError>?
        guard let encryptedData = SecKeyCreateEncryptedData(
            secKey,
            .rsaEncryptionPKCS1,
            data.toData() as CFData,
            &error
        ) as Data? else {
            return ""
        }
        return encryptedData.base64EncodedString()
    }

    private static func makeRSAPublicKey(from keyData: Data) -> SecKey? {
        let attributes: [String: Any] = [
            kSecAttrKeyType as String: kSecAttrKeyTypeRSA,
            kSecAttrKeyClass as String: kSecAttrKeyClassPublic,
            kSecAttrKeySizeInBits as String: 2048
        ]
        var error: Unmanaged<CFError>?
        if let key = SecKeyCreateWithData(keyData as CFData, attributes as CFDictionary, &error) {
            return key
        }
        guard let rawKeyData = keyData.strippingX509PublicKeyHeader() else {
            return nil
        }
        return SecKeyCreateWithData(rawKeyData as CFData, attributes as CFDictionary, &error)
    }
}

// MARK: - KotlinByteArray 转换辅助

private extension KotlinByteArray {
    func toData() -> Data {
        let size = Int(self.size)
        var bytes = [UInt8](repeating: 0, count: size)
        for i in 0..<size {
            bytes[i] = UInt8(bitPattern: self.get(index: Int32(i)))
        }
        return Data(bytes)
    }
}

private extension Data {
    func strippingX509PublicKeyHeader() -> Data? {
        let bytes = [UInt8](self)
        var index = 0

        guard bytes.count > index, bytes[index] == 0x30 else { return nil }
        index += 1
        guard index < bytes.count else { return nil }
        index = skipASN1Length(bytes: bytes, index: index)
        guard index > 0 else { return nil }

        let rsaEncryptionOID: [UInt8] = [
            0x30, 0x0d, 0x06, 0x09, 0x2a, 0x86, 0x48, 0x86,
            0xf7, 0x0d, 0x01, 0x01, 0x01, 0x05, 0x00
        ]
        guard bytes.count >= index + rsaEncryptionOID.count,
              Array(bytes[index..<index + rsaEncryptionOID.count]) == rsaEncryptionOID else {
            return nil
        }
        index += rsaEncryptionOID.count

        guard bytes.count > index, bytes[index] == 0x03 else { return nil }
        index += 1
        guard index < bytes.count else { return nil }
        index = skipASN1Length(bytes: bytes, index: index)
        guard index > 0, bytes.count > index, bytes[index] == 0x00 else { return nil }
        index += 1

        guard index < count else { return nil }
        return subdata(in: index..<count)
    }

    private func skipASN1Length(bytes: [UInt8], index: Int) -> Int {
        guard index < bytes.count else { return -1 }
        let lengthByte = bytes[index]
        if lengthByte < 0x80 {
            return index + 1
        }
        let lengthBytesCount = Int(lengthByte - 0x80)
        let nextIndex = index + 1 + lengthBytesCount
        return nextIndex <= bytes.count ? nextIndex : -1
    }
}

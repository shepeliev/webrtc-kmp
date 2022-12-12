package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.logging.Logging
import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.asn1.x509.ExtendedKeyUsage
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.GeneralName.dNSName
import org.bouncycastle.asn1.x509.KeyPurposeId
import org.bouncycastle.asn1.x509.KeyUsage.digitalSignature
import org.bouncycastle.asn1.x509.KeyUsage.keyEncipherment
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.ContentSigner
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.IOException
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.Security
import java.util.Base64
import java.util.Date
import javax.security.auth.x500.X500Principal
import dev.onvoid.webrtc.RTCCertificatePEM as NativeRtcCertificatePem


actual class RtcCertificatePem internal constructor(val native: NativeRtcCertificatePem) {
    actual val privateKey: String
        get() = native.privateKey

    actual val certificate: String
        get() = native.certificate

    actual companion object {

        init {
            Security.addProvider(BouncyCastleProvider())
        }

        actual suspend fun generateCertificate(keyType: KeyType, expires: Long): RtcCertificatePem {
            val generator = when(keyType) {
                KeyType.RSA -> {
                    KeyPairGenerator.getInstance("RSA", "BC").apply {
                        initialize(1024)
                    }
                }
                KeyType.ECDSA -> {
                    KeyPairGenerator.getInstance("ECDSA", "BC").apply {
                        val ecSpec = ECNamedCurveTable.getParameterSpec("secp256r1")
                        initialize(ecSpec)
                    }
                }
            }

            val pair = generator.generateKeyPair()
            val cert = generateSelfSignedCertificate(
                keyPair = pair,
                expires = expires,
                algorithm = when(keyType) {
                    KeyType.ECDSA -> "SHA256withECDSA"
                    KeyType.RSA -> "SHA256withRSA"
                }
            )
            return RtcCertificatePem(
                NativeRtcCertificatePem(
                    pair.getPrivateKeyPkcs1Pem(),
                    cert.getCertificatePem(),
                    cert.notAfter.time,
                )
            )
        }

        private fun generateSelfSignedCertificate(keyPair: KeyPair, expires: Long, algorithm: String): X509CertificateHolder {
            Security.addProvider(BouncyCastleProvider())
            val subject = X500Principal("CN=WebRTC")
            val notAfter = expires + 1000L * 3600L * 24 * 365
            val encodableAltNames = arrayOf<ASN1Encodable>(org.bouncycastle.asn1.x509.GeneralName(dNSName, "WebRTC"))
            val purposes = arrayOf<KeyPurposeId>(KeyPurposeId.id_kp_serverAuth, KeyPurposeId.id_kp_clientAuth)
            val certBuilder = JcaX509v3CertificateBuilder(
                subject,
                BigInteger.ONE,
                Date(expires),
                Date(notAfter),
                subject,
                keyPair.public,
            )

            try {
                certBuilder.addExtension(Extension.basicConstraints, true, BasicConstraints(false))
                certBuilder.addExtension(Extension.keyUsage, true, org.bouncycastle.asn1.x509.KeyUsage(digitalSignature + keyEncipherment))
                certBuilder.addExtension(Extension.extendedKeyUsage, false, ExtendedKeyUsage(purposes))
                certBuilder.addExtension(Extension.subjectAlternativeName, false, DERSequence(encodableAltNames))
                val signer: ContentSigner = JcaContentSignerBuilder(algorithm).build(keyPair.private)
                return certBuilder.build(signer)
            } catch (e: Exception) {
                Logging.error(e.message)
                throw AssertionError(e.message)
            }
        }
    }
}

@Throws(IOException::class)
private fun X509CertificateHolder.getCertificatePem(): String {
    val encoder: Base64.Encoder = Base64.getEncoder()
    val result = StringBuilder()
    result.append("-----BEGIN CERTIFICATE-----\n")
    result.append(encoder.encodeToString(encoded))
    result.append("\n-----END CERTIFICATE-----\n")
    return result.toString()
}

@Throws(IOException::class)
private fun KeyPair.getPrivateKeyPkcs1Pem(): String {
    val encoder: Base64.Encoder = Base64.getEncoder()
    val privateKeyInfo = PrivateKeyInfo.getInstance(private.encoded)
    val result = StringBuilder()
    result.append("-----BEGIN RSA PRIVATE KEY-----\n")
    result.append(encoder.encodeToString(privateKeyInfo.parsePrivateKey().toASN1Primitive().encoded))
    result.append("\n-----END RSA PRIVATE KEY-----\n")
    return result.toString()
}

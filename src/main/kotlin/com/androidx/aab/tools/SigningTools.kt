package com.androidx.aab.tools

import com.android.tools.build.bundletool.model.Password
import com.android.tools.build.bundletool.model.SigningConfiguration
import com.androidx.aab.AABTool
import org.bouncycastle.asn1.ASN1Boolean
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.OperatorCreationException
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.*
import java.math.BigInteger
import java.nio.file.Path
import java.security.*
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.time.Instant
import java.time.ZoneOffset
import java.util.*
import javax.security.auth.x500.X500Principal

/**
 * code from
 * [CertificateFactory](https://github.com/google/bundletool/0583e1f1a7161621b8e4332f77f46d9f8a0da4bc/src/test/java/com/android/tools/build/bundletool/testing/CertificateFactory.java)
 * [SigningConfiguration](https://github.com/google/bundletool/blob/2f2bb4b65444ec64bc89bb7db6fc4d86b7323cb9/src/main/java/com/android/tools/build/bundletool/model/SigningConfiguration.java)
 */
object SigningTools {

    private const val BASIC_CONSTRAINTS_EXTENSION = "2.5.29.19"

    private val keyPair by lazy {
        KeyPairGenerator.getInstance("RSA").genKeyPair()
    }

    private val privateKey: PrivateKey by lazy {
        keyPair.private
    }
    private val certificate: X509Certificate by lazy {
        buildSelfSignedCertificate(keyPair, "CN=SigningConfigurationTest")
    }

//    KeyPair keyPair = KeyPairGenerator.getInstance("RSA").genKeyPair();
//    privateKey = keyPair.getPrivate();
//    certificate = buildSelfSignedCertificate(keyPair, "CN=SigningConfigurationTest");

    @Throws(Exception::class)
    fun extractFromKeystoreJKSFile(file: File): SigningConfiguration {
        val keystorePath: Path = createKeystoreOfType("JKS", file)
        return extractFromKeystore(
            keystorePath,
            AABTool.KEY_ALIAS,
            AABTool.KEYSTORE_PASSWORD,
            AABTool.KEY_PASSWORD
        )
    }

    fun extractFromKeystore(
        keystorePath: Path,
        keyAlias: String,
        keystorePassword: String,
        keyPassword: String
    ): SigningConfiguration {
        return SigningConfiguration.extractFromKeystore(
            keystorePath,
            keyAlias,
            Optional.of(Password.createFromStringValue("pass:$keystorePassword")),
            Optional.of(Password.createFromStringValue("pass:$keyPassword")),
        )
    }

    @Throws(java.lang.Exception::class)
    private fun createKeystoreOfType(keystoreType: String, file: File): Path {
        return createKeystoreWithPasswords(file, keystoreType, AABTool.KEYSTORE_PASSWORD, AABTool.KEY_PASSWORD)
    }

    @Throws(java.lang.Exception::class)
    private fun createKeystoreWithPasswords(
        file: File, keystoreType: String, keystorePassword: String, keyPassword: String
    ): Path {
        val keystore: KeyStore = KeyStore.getInstance(keystoreType)
        keystore.load(null, keystorePassword.toCharArray())
        keystore.setKeyEntry(
            AABTool.KEY_ALIAS, privateKey, keyPassword.toCharArray(), arrayOf<Certificate>(certificate)
        )
        val keystorePath: Path = file.toPath()
        keystore.store(FileOutputStream(keystorePath.toFile()), keystorePassword.toCharArray())
        return keystorePath
    }

    fun buildSelfSignedCertificate(
        keyPair: KeyPair, distinguishedName: String
    ): X509Certificate {
        return inflateCertificate(
            buildSelfSignedCertificateDerEncoded(keyPair, distinguishedName, "SHA256withRSA")
        )
    }

    fun buildSelfSignedCertificateDerEncoded(
        keyPair: KeyPair, distinguishedName: String
    ): ByteArray {
        return buildSelfSignedCertificateDerEncoded(keyPair, distinguishedName, "SHA256withRSA")
    }

    private fun buildSelfSignedCertificateDerEncoded(
        keyPair: KeyPair, distinguishedName: String, signatureAlgorithm: String
    ): ByteArray {
        val principal = X500Principal(distinguishedName)

        // Default is 30 years. Fields are ignored by Android framework anyway (as of Jan 2017).
        val notBefore: Instant = Instant.now()
        val notAfter: Instant = notBefore.atOffset(ZoneOffset.UTC).plusYears(30).toInstant()
        val rng = SecureRandom()
        return try {
            JcaX509v3CertificateBuilder( /* issuer= */
                principal,
                generateRandomSerialNumber(rng),
                Date(notBefore.toEpochMilli()),
                Date(notAfter.toEpochMilli()),  /* subject= */
                principal,
                keyPair.public
            ) // Basic constraints: subject type = CA
                .addExtension(
                    ASN1ObjectIdentifier(BASIC_CONSTRAINTS_EXTENSION),
                    false,
                    DERSequence(ASN1Boolean.TRUE)
                )
                .build(
                    JcaContentSignerBuilder(signatureAlgorithm).build(keyPair.private)
                )
                .encoded
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        } catch (e: OperatorCreationException) {
            throw RuntimeException(e)
        }
    }

    private fun inflateCertificate(encodedCertificate: ByteArray): X509Certificate {
        return try {
            val certFactory = CertificateFactory.getInstance("X.509")
            certFactory.generateCertificate(ByteArrayInputStream(encodedCertificate)) as X509Certificate
        } catch (e: CertificateException) {
            throw RuntimeException(
                "Cannot parse the certificates as X.509 certificates. cert: " + encodedCertificate.contentToString(),
                e
            )
        }
    }

    private fun generateRandomSerialNumber(rng: SecureRandom): BigInteger {
        // Serial number of conforming CAs must be positive and no larger than 20 bytes long.
        val serialBytes = ByteArray(20)
        while (true) {
            rng.nextBytes(serialBytes)
            val serial = BigInteger(1, serialBytes)
            if (serial >= BigInteger.ONE) {
                return serial
            }
        }
    }
}
# Changelog

## Release 1.5.2

Release 1.5.2 is a minor maintenance patch containing the following changes:

* [Code] Isolated improvements to build reproducibility.
* [Code] Update dependencies and third-party libraries.

## Release 1.5.1

Release 1.5.1 includes some feedback from the Federal Chancellery's mandated experts and other experts of the community.
We want to thank the experts for their high-quality, constructive remarks:

* Thomas Edmund Haines (Australian National University), Olivier Pereira (Université catholique Louvain), Vanessa Teague (Thinking Cybersecurity)
* Aleksander Essex (Western University Canada)
* Rolf Haenni, Reto Koenig, Philipp Locher (Bern University of Applied Sciences)

The following functionalities and improvements are included in release 1.5.1:

* [Specification] Improve byte notation consistency in algorithm CutToBitLength (feedback from Olivier Pereira).
* [Specification] Correct the statement about the bit length of 0 (reported in GitLab Issue [24](https://gitlab.com/swisspost-evoting/crypto-primitives/crypto-primitives/-/issues/24)).
* [Code] Update dependencies and third-party libraries.

## Release 1.5.0

Release 1.5.0 includes some feedback from the Federal Chancellery's mandated experts (see above) and other experts of the community.

The following functionalities and improvements are included in release 1.5.0:

* [Code, Specification] Allow edge cases in several algorithms, and so adhere to the Principle of Generality. This affects the pseudocode and implementation of the following algorithms: CutToBitLength, ByteArrayToInteger, ByteLength, ByteArrayToString, Truncate, GenRandomVector, GenRandomString, GenUniqueDecimalStrings, GenCiphertextSymmetric, GetPlaintextSymmetric, GenPermutation (feedback from Rolf Haenni, Reto Koenig, Philipp Locher).
* [Specification] Systematically add modulo operator in pseudocode. This affects the pseudocode of the following algorithms: StarMap, GetShuffleArgument, VerifyShuffleArgument, GetMultiExponentiationArgument, VerifyMultiExponentiationArgument, GetProductArgument, GetHadamardArgument, VerifyHadamardArgument, GetZeroArgument, VerifyZeroArgument, ComputeDVector, VerifySingleValueProductArgument, ComputePhiDecryption, GenDecryptionProof, VerifyDecryption, VerifyExponentiation, ComputePhiPlaintextEquality, GenPlaintextEqualityProof, VerifyPlaintextEquality (reported in GitLab Issues [52](https://gitlab.com/swisspost-evoting/e-voting/e-voting-documentation/-/issues/52) and [20](https://gitlab.com/swisspost-evoting/crypto-primitives/crypto-primitives/-/issues/20)).
* [Code, Specification] Improve variable naming in algorithm IntegerToFixedLengthByteArray (feedback from Aleksander Essex).
* [Code, Specification] Simplify IntegerToByteArray and IntegerToFixedLengthByteArray (feedback from Rolf Haenni, Reto Koenig, Philipp Locher).
* [Code, Specification] Improve variable naming in algorithms KDF and KDFToZq (feedback from Rolf Haenni, Reto Koenig, Philipp Locher).
* [Code, Specification] Add requirement in KDFToZq (feedback from Rolf Haenni, Reto Koenig, Philipp Locher).
* [Code, Specification] Improve the description of how to generate a signing key and certificate and ensure the generation of certificates complies with RFC 5280 (reported in GitLab Issue [17](https://gitlab.com/swisspost-evoting/verifier/verifier/-/issues/17)).
* [Specification] Improve the pseudocode of the algorithm GenKeysAndCert (feedback from Rolf Haenni, Reto Koenig, Philipp Locher).
* [Specification] Improve the description of the output of algorithms GenSignature and VerifySignature (feedback from Rolf Haenni, Reto Koenig, Philipp Locher).
* [Specification] Shorten the description of GenKeyPair by removing textbook-style details (feedback from Rolf Haenni, Reto Koenig, Philipp Locher).
* [Code, Specification] Improve variable naming in the algorithm GenKeyPair (feedback from Rolf Haenni, Reto Koenig, Philipp Locher).
* [Code, Specification] Improve variable naming in the algorithm GetCiphertextVectorExponentiation (feedback from Rolf Haenni, Reto Koenig, Philipp Locher).
* [Code] GenShuffle small alignment to specification (feedback from Rolf Haenni, Reto Koenig, Philipp Locher).
* [Code] Extend test cases for algorithm VerifyDecryption (reported in GitLab Issue [23](https://gitlab.com/swisspost-evoting/crypto-primitives/crypto-primitives/-/issues/23)).
* [Code] Add hash values to test data for VerifyShuffleArgument, VerifyMultiExponentiationArgument, VerifyHadamardArgument (reported in GitLab Issue [21](https://gitlab.com/swisspost-evoting/crypto-primitives/crypto-primitives/-/issues/21)).
* [Code] Replace base16 by base64 in JsonData and json test files.
* [Code] Ensure usage of g=4 in test vectors (feedback from Aleksander Essex).
* [Code] Use new crypto-primitives type ImmutableList instead of List for collections simply holding immutable data.
* [Code] Replace the usage of List<String> auxiliaryInformation by the new type AuxiliaryInformation.
* [Code] Replace the usage of byte[] by the new type ImmutableByteArray.
* [Code] Ensure immutability for map and set objects.
* [Code] Use ImmutableArray whenever we use vectors.
* [Code] Change records with multiple arguments of same type to class and add builder.
* [Specification] Include reasoning for using both SHA3-256 and SHA-256 (feedback from Rolf Haenni, Reto Koenig, Philipp Locher).
* [Specification] Clarification in table 2 about group parameters used for the testing-only security level (feedback from Rolf Haenni, Reto Koenig, Philipp Locher).
* [Specification] Clarification in table 3 about nonce size and key size of the symmetric algorithm for authenticated encryption and decryption (feedback from Rolf Haenni, Reto Koenig, Philipp Locher).
* [Specification] Clarification in table 8 about the variables used for the Argon2 profiles (feedback from Rolf Haenni, Reto Koenig, Philipp Locher).
* [Specification] Minor corrections and clarifications (feedback from Rolf Haenni, Reto Koenig, Philipp Locher).
* [Code] Minor improvements.
* [Code] Update dependencies and third-party libraries.

## Release 1.4.5

The following improvements are included in release 1.4.5:

* [Code] Exposed the randomBytes method with the Random interface.
* [Code, Specification] Specify and implement algorithm IntegerToFixedLengthByteArray.
* [Code] Updated dependencies and third-party libraries.

## Release 1.4.4

The following improvements are included in release 1.4.4:

* [Code] Removed an unused import in SymmetricAuthenticatedEncryptionService.
* [Code] Updated dependencies and third-party libraries.

## Release 1.4.3

Release 1.4.3 includes some feedback from the Federal Chancellery's mandated experts (see above) and other experts of the community.

The following functionalities and improvements are included in release 1.4.3:

* [Code] Avoid repeated calls of stringToByteArray in GenCiphertextSymmetric and GetPlaintextSymmetric (feedback from Rolf Haenni, Reto Koenig, Philipp Locher).
* [Code, Specification] Small adjustments to ByteLength and ByteArrayToInteger (feedback from Rolf Haenni, Reto Koenig, Philipp Locher).
* [Code] Improved the alignment of the GenShuffle implementation to the specification (feedback from Rolf Haenni, Reto Koenig, Philipp Locher).
* [Specification, Code] Improved handling of edge cases in StringToInteger and GenRandomInteger (feedback from Rolf Haenni, Reto Koenig, Philipp Locher).
* [Code] Add try/catch in Base16Decode (feedback from Rolf Haenni, Reto Koenig, Philipp Locher).
* [Code] Minor improvement in CutToBitLength (feedback from Rolf Haenni, Reto Koenig, Philipp Locher).
* [Code, Specification] Remove the letter U+00A0 (Non-Breaking Space) from the latin alphabet (feedback from Rolf Haenni, Reto Koenig, Philipp Locher).
* [Code] Set the CodingErrorAction to REPORT explicitly for ConversionsInternal.stringToByteArray (feedback from Thomas Haines).
* [Specification] Correction of the indexing in algorithm 9.12 VerifyShuffleArgument.
* [Specification] Minor corrections and clarifications (feedback from Rolf Haenni, Reto Koenig, Philipp Locher).
* [Code] Updated dependencies and third-party libraries.

## Release 1.4.2

Release 1.4.2 is a minor maintenance patch containing the following changes:

* [Code] Ensured that all crypto-primitives internal classes invoke the RandomService or TestRandomService instead of SecureRandom directly (feedback from Rolf Haenni, Reto Koenig, and Philipp Locher).
* [Code] Included the bound 0 in the method genRandomIntegerOfLength used in test classes.
* [Code] Minor improvements in test classes.
* [Code] Updated dependencies and third-party libraries.

## Release 1.4.1

Release 1.4.1 is a minor maintenance patch containing the following changes:

* [Code] Align the generation of PKCS12 keystores to common standards.
* [Code] Updated dependencies and third-party libraries.

## Release 1.4.0

Release 1.4.0 includes some feedback from the Federal Chancellery's mandated experts (see above) and other experts of the community.

The following functionalities and improvements are included in release 1.4.0:

* [Code, Specification] Legacy security level removed, extended security level renamed to standard.
* [Specification] Changes in the section about security level: The overview of the primitives and parametrization used was improved.
* [Specification] Removed the ambiguous "uniformly safe" description in the Argon2 section (feedback from Aleksander Essex).
* [Code] Removed the confusing Argon2 profiles (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Code, Specification] Removed unused algorithm LeftPad.
* [Code, Specification] Made algorithm Truncate more flexible.
* [Specification] Added a section about alphabets, including a user-friendly alphabet for codes and an extended Latin alphabet.
* [Code] Added several alphabets from the e-voting source code.
* [Code] Replaced the legacy BouncyCastle Jacobi with the Legendre symbol (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Code] Improved object-oriented design of the GqElement class (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Code] Improved the encapsulation and consistency of internal methods (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Code] Defined a GenRandomInteger method working with the Java Integer type.
* [Code, Specification] Added new algorithm GenRandomString and removed the old GenRandomBaseXXString algorithms (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Code, Specification] Simplified algorithm GenUniqueDecimalStrings by using GenRandomString.
* [Code, Specification] Removed magic numbers in RecursiveHashToZq and KDFToZq.
* [Code, Specification] Improvements to algorithm GetEncryptionParameters including using only MillerRabin instead of isProbablePrime, and using SHAKE256 instead of SHAKE128 (feedback from Aleksander Essex, Thomas Haines, Olivier Pereira, Vanessa Teague, Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Code] Removed the legacy isProbablePrime method.
* [Code] Implemented a mechanism to check the integrity of the native VMGJ library.
* [Specification] Improved the description of the encryption parameters generation in sections Primality Testing and Parameters Generation accordingly (feedback from Aleksander Essex, Thomas Haines, Olivier Pereira, and Vanessa Teague).
* [Code, Specification] Fixed minor errors and inconsistencies.
* [Code] Updated dependencies and third-party libraries.

## Release 1.3.3

Release 1.3.3 is a minor maintenance patch containing the following changes:

* [Code] Updated dependencies and third-party libraries.

## Release 1.3.2

Release 1.3.2 is a minor maintenance patch containing the following changes:

* [Code] Updated dependencies and third-party libraries.

## Release 1.3.1

Release 1.3.1 includes some feedback from the Federal Chancellery's mandated experts (see above) and other experts of the community.

The following functionalities and improvements are included in release 1.3.1:

* [Code, Specification] Slightly modified the algorithm RecurisiveHashToZq, and thereby also the algorithms HashAndSquare and GetVerifiableCommitmentKey (feedback from Thomas Haines, Olivier Pereira, and Vanessa Teague).
* [Code] Use Douglas Wikström's VMGJ library to allow fixed-base and product exponentiation.
* [Code] Optimized the exponentiation proof generation and verification by adding parallelization.
* [Code] Remove unnecessary pre-condition verifications to improve performance.
* [Specification] Corrected error in the table showing the Argon2id profiles (feedback from Aleksander Essex, Thomas Haines, Olivier Pereira, and Vanessa Teague).
* [Code, Specification] VerifyExponentiation and VerifyDecryption - merged lines to be consistent with the generation (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Code] Updated dependencies and third-party libraries.

---

## Release 1.3.0

Release 1.3.0 includes some feedback from the Federal Chancellery's mandated experts (see above) and other experts of the community.

The following functionalities and improvements are included in release 1.3.0:

* [Code, Specification] Added additional properties in the x.509 certificates, clarified the description, and improved input validation for digital signatures (feedback from Aleksander Essex).
* [Code, Specification] Added a section on Argon2id profiles containing a justification of the chosen parameters (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Code, Specification] Improved the KdfToZq algorithm to ensure collision resistance (reported in GitLab Issue [46 / #YWH-PGM232-121](https://gitlab.com/swisspost-evoting/e-voting/e-voting-documentation/-/issues/46)).
* [Code] Added a validation in the StringToByteArray method that the input is a valid UTF-8 string (feedback from Vanessa Teague, Olivier Pereira, and Thomas Haines and reported in GitLab Issue [46 / #YWH-PGM232-122](https://gitlab.com/swisspost-evoting/e-voting/e-voting-documentation/-/issues/46)).
* [Code] Improved the performance of the verification of the zero-knowledge proofs.
* [Code] Minor fix of a potential resource leak.
* [Code] Updated dependencies and third-party libraries.

---

## Release 1.2.1

Release 1.2.1 includes some feedback from the Federal Chancellery's mandated experts (see above)

The following functionalities and improvements are included in release 1.2.1:

* [Code, Specification] Improved performance of the GetEncryptionParameters algorithm (feedback from François Weissbaum and Patrick Liniger from the Swiss Armed Forces Command Support Organisation).
* [Code, Specification] Included a proper Miller-Rabin primality test in the GetEncryptionParameters algorithm and stated formal error bounds for primality of the group parameters p and q (feedback from Aleksander Essex).
* [Specification] Strengthened justification for parameters generation (feedback from Aleksander Essex).
* [Specification] Improved description of primality testing (feedback from Aleksander Essex).
* [Code, Specification] Improved the RecursiveHashToZq algorithm to ensure that it is collision resistant (reported in GitLab Issue [46 / #YWH-PGM232-121](https://gitlab.com/swisspost-evoting/e-voting/e-voting-documentation/-/issues/46)).
* [Code] Updated dependencies and third-party libraries.

---

## Release 1.2

Release 1.2 includes some feedback from the Federal Chancellery's mandated experts (see above)

The following functionalities and improvements are included in release 1.2:

* [Specification] Justified the use-case dependent parametrization of Argon2 (feedback from Aleksander Essex).
* [Specification] Simplified the specification of the GetCiphertextVectorExponentiation and GetVerifiableCommitmentKey algorithms.
* [Specification] Improved the specification of the LeftPad algorithm (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Specification] Specified the output value of the truncate method (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Specification] Fixed the assignment symbol in the GetUniqueDecimalStrings algorithm (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Specification] Improved the definition of the HashAndSquare algorithm's inputs (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Code, Specification] Improved the GetEncryptionParameters algorithm (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Code, Specification] Optimized the isSmallPrime method using the optimized school method (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Code, Specification] Improved the input validation in the algorithms GenVerifiableDecryptions, VerifyProductArgument, and ComputeDVector (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Code, Specification] Fixed the requirements and indices in the CombinePublicKeys method (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Specification] Fixed the domain of the GenPermutation method (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Specification] Improved the definition of the neutral element in the GetDiagonalProducts algorithm (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Specification] Fixed wrong fonts in various algorithms (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Code] Introduced a divide method for the division of GqElements (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Code] Improved the alignment of the GetSmallPrimeGroupMembers algorithm (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Code] Simplified the stream in the GenShuffle algorithm (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Code] Streamlined the GetMatrixDimensions method (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Code] Improved the alignment of the VerifySchnorr and VerifyProcessPlaintext algorithms (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Code] Improved the input validation in the setters of the AuthorityInformation class (corresponds to GitLab issue [#14](https://gitlab.com/swisspost-evoting/crypto-primitives/crypto-primitives/-/issues/14 )).
* [Code] Updated dependencies and third-party libraries.

---

## Release 1.1

Release 1.1 includes some feedback from the Federal Chancellery's mandated experts (see above)

The following functionalities and improvements are included in release 1.1:

* [Code] Implemented the BaseXEncode and BaseXDecode wrapper methods (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Code] Implemented the Truncate (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Code] Improved the immutability of objects (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Code] Improved the package structure (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Code, Specification] Improved the specification of the method ByteLength and implemented it faithfully (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Specification] Simplified and moved the section on probabilistic primality tests (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Code] Improved the alignment of the method GenRandomInteger (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Code] Improved the alignment of the method GenKeyPair (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Code] Improved the alignment of the method VerifyDecryptions (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Code] Improved the input validation of the method GetCommitmentVector (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).

---

## Release 1.0

The following functionalities and improvements are included in release 1.0:

* [Code, Specification] Improved the RecursiveHash's collision-resistance for nested objects (refers to #YWH-PGM2323-69 mentioned in [GitLab issue #37](https://gitlab.com/swisspost-evoting/e-voting/e-voting-documentation/-/issues/37)).
* [Code, Specification] Ensured the injective encoding of the associated data in the GenCiphertextSymmetric method (refers to #YWH-PGM2323-70 mentioned in [GitLab issue #37](https://gitlab.com/swisspost-evoting/e-voting/e-voting-documentation/-/issues/37)).
* [Code, Specification] Ensured the injective encoding of the KDF's additional context information (refers to #YWH-PGM2323-71 mentioned in [GitLab issue #37](https://gitlab.com/swisspost-evoting/e-voting/e-voting-documentation/-/issues/37)).
* [Code, Specification] Split the algorithm Argon2id into two separate algorithm GenArgon2id and GetArgon2id.
* [Code, Specification] Switched to the EXTENDED (128-bits) security level. Renamed the DEFAULT (112-bits) security level to LEGACY.
* [Code, Specification] Fixed the incorrect Require Statement in the algorithm GenUniqueDecimalStrings.
* [Code, Specification] Added an upper limit on the isSmallPrime algorithm.
* [Code] Allowed hashing of empty lists (necessary for signing XML files).
* [Code] Updated dependencies and third-party libraries.

---

## Release 0.15

Release 0.15 includes some feedback from the Federal Chancellery's mandated experts (see above)

The following functionalities and improvements are included in release 0.15:

* [Code, Specification] Specified and implemented the Argon2id method.
* [Code] Renamed the methods multiply and exponentiate to GetCiphertextProduct and GetCiphertextExponentiation (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Code] Updated the library to Java 17.
* [Code] Updated dependencies and third-party libraries.
* [Code] Changed the context data in the GenSignature and VerifySignature methods to Hashable object instead of a single String object.

---

## Release 0.14

Release 0.14 includes some feedback from the Federal Chancellery's mandated experts (see above)

The following functionalities and improvements are included in release 0.14:

* [Code, Specification] Specified and implemented methods for handling digital signatures: key and certificate generation, signing, and verifying a signature (feedback from Vanessa Teague, Olivier Pereira, and Thomas Haines).
* [Code, Specification] Specified and implemented a GenUniqueDecimalStrings method (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Code, Specification] Specified and implemented an ElGamal CombinePublicKeys method.
* [Code, Specification] Specified and implemented the Schnorr Proof of knowledge (feedback from Vanessa Teague, Olivier Pereira, and Thomas Haines).
* [Code] Implemented the ByteArrayToString method.
* [Specification] Specified a method for probabilistic primality testing (feedback from Aleksander Essex).
* [Specification] Aligned the definition of Base16, Base32, and Base64 alphabets and made the padding character explicit.

---

## Release 0.13

Release 0.13 includes some feedback from the Federal Chancellery's mandated experts (see above)

The following functionalities and improvements are included in release 0.13:

* [Code] Implemented the generation and verification of plaintext-equality proofs.
* [Code, Specification] Specified and implemented a KDF and KDFtoZq method based on HKDF (feedback from Vanessa Teague, Olivier Pereira, and Thomas Haines).
* [Code, Specification] Specified and implemented the collision-resistant HashAndSquare method.
* [Code, Specification] Updated the generation of commitment keys (GetVerifiableCommitmentKeys) to use the entire domain of generators (feedback from Vanessa Teague, Olivier Pereira, and Thomas Haines).
* [Code] Updated the unit tests with the test vectors from the collision-resistant hash functions.
* [Code] Aligned the input of the hash functions in the zero-knowledge proofs to the specification (corresponds to Gitlab issue [#10](https://gitlab.com/swisspost-evoting/crypto-primitives/crypto-primitives/-/issues/10 )).
* [Code, Specification] Specified and implemented authenticated symmetric encryption (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Code, Specification] Specified and implemented the IntegerToString and StringToInteger methods (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Code, Specification] Removed compression of excess public keys in ElGamal operations (feedback from Vanessa Teague, Olivier Pereira, and Thomas Haines).
* [Code] Updated dependencies.
* [Specification] Made the usage of Require and Ensure uniform across all algorithms (feedback from Aleksander Essex).
* [Specification] Introduced a specific section for defining the desired security level (feedback from Aleksander Essex, Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Specification] Aligned the usage of various symbols (feedback from Aleksander Essex).
* [Specification] Specified the truncate method (feedback from Aleksander Essex).
* [Specification] Some minor fixes and alignments in various algorithms (feedback from Aleksander Essex).

---

## Release 0.12

Release 0.12 includes some feedback from the Federal Chancellery's mandated experts (see above)

The following functionalities and improvements are included in release 0.12:

* [Code] Implemented the GetSmallGroupPrimeMembers and isPrime methods.
* [Code] Ensured thread safety across all services.
* [Code] Various minor improvements.
* [Code] Optimized mathematical operations with the GNU Multi Precision Arithmetic Library (GMP).
* [Specification] Updated to the new version of the Federal Chancellery's Ordinance on Electronic Voting (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, and Eric Dubuis).
* [Specification] Added the GetSmallGroupPrimeMembers and isPrime algorithms. (feedback from Rolf Haenni, Reto Koenig, Philipp Locher, Eric Dubuis, Vanessa Teague, Olivier Pereira, and Thomas Haines)
* [Specification] Made the RecursiveHash function collision-resistant across different input domains (corresponds to Gitlab issue [#9](https://gitlab.com/swisspost-evoting/crypto-primitives/crypto-primitives/-/issues/9)).
* [Specification] Specified a RecursiveHashToZq method that outputs a collision resistant hash in the domain of the group Z_q. (feedback from Vanessa Teague, Olivier Pereira, and Thomas Haines).
* [Specification] Masked out excess bits in the GenRandomInteger algorithm (feedback from Vanessa Teague, Olivier Pereira, and Thomas Haines).
* [Specification] Clarified that the algorithm GenEncryptionParameters requires |p| to be a multiple of 8 (feedback from Vanessa Teague, Olivier Pereira, and Thomas Haines).
* [Specification] Specified the Base32 and Base64 alphabet variant (corresponds to Gitlab issue [#17](https://gitlab.com/swisspost-evoting/e-voting/e-voting-documentation/-/issues/17 )).
* [Specification] Some minor fixes and alignments in various algorithms.

---

## Release 0.11

The following functionalities and improvements are included in release 0.11:

* [Code] Implemented the VerifyDecryptions method.
* [Specification] Simplified the _GetCommitmentVector_ algorithm.
* [Specification] Added the _HashAndSquare_ algorithm.

---

## Release 0.10

The following functionalities and improvements are included in release 0.10:

* Specified a VerifyDecryptions method that verifies a vector of decryptions.
* Implemented exponentiation proof verification.
* Integrated the certainty into the SecurityLevel class.
* Added some minor precondition and robustness checks.

---

## Release 0.9

The following functionalities and improvements are included in release 0.9:

* Implemented exponentiation proof generation.
* Added the method GetVerifiableEncryptionParameters in the ElGamalEncryption scheme.
* Documented a clear naming convention for the translation of mathematical notations to code and applied it consistently across the codebase.
* Outsourced the concatenation of byte arrays in the recursive hash function to a utility function.
* Completed some additional « Ensure » statements in the mix net algorithms description to increase robustness.
* Fixed some minor alignment issues in a few algorithms.

---

## Release 0.8

The following functionalities and improvements are included in release 0.8:

* Provided decryption proof generation and verification.
* Specified the exponentiation and plaintext equality proof.
* Improved specification of handling errors in Base32/Base64 encoding (corresponds to Gitlab issue [#1](https://gitlab.com/swisspost-evoting/crypto-primitives/crypto-primitives/-/issues/1)).
* Fixed handling of empty byte arrays in the method ByteArrayToInteger (corresponds to Gitlab issue [#2](https://gitlab.com/swisspost-evoting/crypto-primitives/crypto-primitives/-/issues/2)).
* Improved specification of UCS decoding (corresponds to Gitlab issue [#3](https://gitlab.com/swisspost-evoting/crypto-primitives/crypto-primitives/-/issues/3)).
* Fixed the bounds' domain in GenRandomIntegerWithinBounds (corresponds to Gitlab issue [#6](https://gitlab.com/swisspost-evoting/crypto-primitives/crypto-primitives/-/issues/6)).
* Removed the exclusion of 0 and 1 when generating exponents (corresponds to Gitlab issue [#7](https://gitlab.com/swisspost-evoting/crypto-primitives/crypto-primitives/-/issues/7)).
* Clarified the purpose of GenRandomBaseXXString methods (corresponds to Gitlab issue [#8](https://gitlab.com/swisspost-evoting/crypto-primitives/crypto-primitives/-/issues/8)).
* Decoupled the size of the commitment key and the size of the public key in the mix net.
* Fixed the problem with some randomized unit tests failing for exceptional edge cases.

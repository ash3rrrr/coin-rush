"""Generate the Coin Rush release signing keystore (PKCS#12).

Run once from the repo root:

    python scripts/gen_keystore.py

Creates keystore/coin-rush-release.p12 and keystore/keystore.info
(neither is committed; keystore/ is gitignored). Keep the .p12 safe —
losing it means you can't update the app on Google Play.
"""

import base64
import datetime
import os
import secrets

from cryptography import x509
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography.hazmat.primitives.serialization.pkcs12 import serialize_key_and_certificates
from cryptography.x509.oid import NameOID

KEY_SIZE = 2048
VALIDITY_DAYS = 10950  # 30 years
ALIAS = "coinrush"

OUT_DIR = "keystore"


def main():
    os.makedirs(OUT_DIR, exist_ok=True)

    password = secrets.token_urlsafe(24)

    key = rsa.generate_private_key(public_exponent=65537, key_size=KEY_SIZE)

    name = x509.Name(
        [
            x509.NameAttribute(NameOID.COMMON_NAME, "Coin Rush"),
            x509.NameAttribute(NameOID.ORGANIZATION_NAME, "ash3rrrr"),
            x509.NameAttribute(NameOID.COUNTRY_NAME, "US"),
        ]
    )
    now = datetime.datetime.now(datetime.timezone.utc)
    cert = (
        x509.CertificateBuilder()
        .subject_name(name)
        .issuer_name(name)
        .public_key(key.public_key())
        .serial_number(x509.random_serial_number())
        .not_valid_before(now)
        .not_valid_after(now + datetime.timedelta(days=VALIDITY_DAYS))
        .add_extension(
            x509.BasicConstraints(ca=False, path_length=None),
            critical=True,
        )
        .sign(key, hashes.SHA256())
    )

    p12_bytes = serialize_key_and_certificates(
        name=b"coinrush",
        key=key,
        cert=cert,
        cas=None,
        encryption_algorithm=serialization.BestAvailableEncryption(password.encode()),
    )

    p12_path = os.path.join(OUT_DIR, "coin-rush-release.p12")
    with open(p12_path, "wb") as f:
        f.write(p12_bytes)

    with open(os.path.join(OUT_DIR, "keystore.info"), "w") as f:
        f.write(f"alias={ALIAS}\npassword={password}\n")

    with open(os.path.join(OUT_DIR, "keystore.b64"), "w") as f:
        f.write(base64.b64encode(p12_bytes).decode())

    print(f"created {p12_path}")
    print(f"alias:   {ALIAS}")
    print("password: (stored in keystore/keystore.info)")
    print("base64:  (stored in keystore/keystore.b64)")


if __name__ == "__main__":
    main()

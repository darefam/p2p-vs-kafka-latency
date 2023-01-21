#!/bin/bash

set -o nounset \
    -o errexit \
    -o verbose \
    -o xtrace

# Generate CA key
rm -f *.crt *.csr *_creds *.jks *.srl *.key *.pem *.der *.p12 *.log
openssl req -new -x509 -keyout snakeoil-ca-1.key -out snakeoil-ca-1.crt -days 365 -subj '/CN=ca1.kafkalondon.meetup.io/OU=TEST/O=ULTRADOVESYSTEMS/L=London/ST=London/C=UK' -passin pass:meetupsecret -passout pass:meetupsecret

for i in kafka-1 kafka-2 kafka-3 zookeeper admin-client producer consumer
do
	echo $i
	# Create keystores
	keytool -genkey -noprompt \
  			 -alias $i \
  			 -dname "CN=$i,OU=TEST,O=ULTRADOVESYSTEMS,L=London,ST=London,C=UK" \
                           -ext "SAN=dns:$i,dns:localhost" \
  			 -keystore kafka.$i.keystore.jks \
  			 -keyalg RSA \
  			 -storepass meetupsecret \
  			 -keypass meetupsecret \
  			 -storetype pkcs12

  # Create the certificate signing request (CSR)
  keytool -keystore kafka.$i.keystore.jks -alias $i -certreq -file $i.csr -storepass meetupsecret -keypass meetupsecret -ext "SAN=dns:$i,dns:localhost"

  DNS_ALT_NAMES=$(printf '%s\n' "DNS.1 = $i" "DNS.2 = localhost")

  # Sign the host certificate with the certificate authority (CA)
  CERT_SERIAL=$(awk -v seed="$RANDOM" 'BEGIN { srand(seed); printf("0x%.4x%.4x%.4x%.4x\n", rand()*65535 + 1, rand()*65535 + 1, rand()*65535 + 1, rand()*65535 + 1) }')
  openssl x509 -req -CA snakeoil-ca-1.crt -CAkey snakeoil-ca-1.key -in $i.csr -out $i-ca1-signed.crt -sha256 -days 365 -set_serial ${CERT_SERIAL} -passin pass:meetupsecret -extensions v3_req -extfile <(cat <<EOF
[req]
distinguished_name = req_distinguished_name
x509_extensions = v3_req
prompt = no
[req_distinguished_name]
CN = $i
[v3_req]
extendedKeyUsage = serverAuth, clientAuth
subjectAltName = @alt_names
[alt_names]
$DNS_ALT_NAMES
EOF
)
  # Sign and import the CA cert into the keystore
  keytool -noprompt -keystore kafka.$i.keystore.jks -alias snakeoil-caroot -import -file snakeoil-ca-1.crt -storepass meetupsecret -keypass meetupsecret

  # Sign and import the host certificate into the keystore
  keytool -noprompt -keystore kafka.$i.keystore.jks -alias $i -import -file $i-ca1-signed.crt -storepass meetupsecret -keypass meetupsecret -ext "SAN=dns:$i,dns:localhost"

  # Create truststore and import the CA cert
  keytool -noprompt -keystore kafka.$i.truststore.jks -alias snakeoil-caroot -import -file snakeoil-ca-1.crt -storepass meetupsecret -keypass meetupsecret

  # Save credentials
  echo "meetupsecret" > ${i}_sslkey_creds
  echo "meetupsecret" > ${i}_keystore_creds
  echo "meetupsecret" > ${i}_truststore_creds
done

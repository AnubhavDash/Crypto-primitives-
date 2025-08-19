#!/bin/bash
#
# (c) Copyright 2025 Swiss Post Ltd.
#
set -e

hash_filename="$1_$2_checksum.md"
hash_path="$3/reproducible-build"

mkdir -p "$hash_path"

echo '| Artefact | Version | Checksum |' > "$hash_path/$hash_filename"
echo '|----------|---------|----------|' >> "$hash_path/$hash_filename"

for file in $3/crypto-primitives-*.jar; do
  if [[ -f "$file" ]]; then
    filename=$(basename "$file")
    checksum=$(sha256sum "$file" | awk '{print $1}')
    echo "| **$filename** | $2 | $checksum |" >> "$hash_path/$hash_filename"
  fi
done

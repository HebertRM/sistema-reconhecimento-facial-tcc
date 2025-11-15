#!/bin/bash

for arq in capture_*_*.png; do
    # Ex: capture_2_20251028200424424_face.png
    IFS='_' read -r prefix id timestamp face rest <<< "${arq%%.*}"

    # Monta o novo formato: capture_TIMESTAMP_face_ID.png
    novo="capture_${timestamp}_face_${id}.png"

    echo "Renomeando: $arq  →  $novo"
    mv "$arq" "$novo"
done

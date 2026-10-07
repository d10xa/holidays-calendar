#!/usr/bin/env bash
# Скачивает производственный календарь с consultant.ru за текущий и следующий год,
# обновляет json/consultant{YEAR}.json, json/calendar.json и ссылки в README.md.
set -euo pipefail

UA="Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0 Safari/537.36"
CURRENT_YEAR=$(date +%Y)

mkdir -p html

for YEAR in "$CURRENT_YEAR" "$((CURRENT_YEAR + 1))"; do
    HTML="html/consultant$YEAR.html"
    JSON="json/consultant$YEAR.json"
    URL="https://www.consultant.ru/law/ref/calendar/proizvodstvennye/$YEAR/"

    if ! curl -fsSL --retry 3 --retry-delay 10 -A "$UA" -o "$HTML" "$URL"; then
        if [ "$YEAR" = "$CURRENT_YEAR" ]; then
            echo "::error::$YEAR: не удалось скачать $URL"
            exit 1
        fi
        echo "::notice::$YEAR: страница ещё не опубликована"
        continue
    fi

    # До выхода постановления consultant.ru публикует проект календаря — его не берём
    if ! grep -q "О переносе выходных дней в $YEAR году" "$HTML"; then
        echo "::notice::$YEAR: постановление о переносе выходных ещё не вышло, пропускаем"
        continue
    fi

    ./gradlew -q runConsultant --args="--input $PWD/$HTML --output $PWD/$JSON"
    echo "$YEAR: $JSON обновлён"

    LINK="[$YEAR](json/consultant$YEAR.json)"
    PREV_LINK="[$((YEAR - 1))](json/consultant$((YEAR - 1)).json)"
    if ! grep -qF "$LINK" README.md; then
        awk -v prev="$PREV_LINK" -v link="$LINK" \
            '{ print; line = $0; sub(/[ \t\r]+$/, "", line) } line == prev { print link }' \
            README.md > README.md.tmp
        mv README.md.tmp README.md
    fi
done

./gradlew -q mergeJson

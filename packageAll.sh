#! /bin/bash
SHELL_SCRIPT_PATH="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd ${SHELL_SCRIPT_PATH}
mkdir -p distribute/lib 2>/dev/null
rm -f distribute/lib/*
mvn clean package -Dmaven.test.skip=true
find . -type f -name "*.jar" | xargs -I{} cp -n {} distribute/lib 
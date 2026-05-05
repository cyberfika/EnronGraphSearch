#!/bin/bash
# Script para gerar PNG dos diagramas PlantUML
# Requer: Java, PlantUML JAR

set -e

PLANTUML_JAR="${PLANTUML_JAR:-plantuml.jar}"
DIAG_DIR="docs/diagrams"
OUT_DIR="diagrams-rendered"

echo "=== Gerando diagramas UML ==="

# Criar diretório de saída
mkdir -p "$OUT_DIR"

# Verificar se PlantUML está disponível
if ! command -v plantuml &> /dev/null && [ ! -f "$PLANTUML_JAR" ]; then
    echo ""
    echo "⚠️  PlantUML não encontrado!"
    echo ""
    echo "Para gerar os diagramas, você precisa:"
    echo ""
    echo "OPÇÃO 1: Instalar PlantUML (recomendado)"
    echo "  - Windows/Scoop: scoop install plantuml"
    echo "  - macOS/Homebrew: brew install plantuml"
    echo "  - Linux/APT: sudo apt-get install plantuml"
    echo ""
    echo "OPÇÃO 2: Usar PlantUML online"
    echo "  - Acesse: https://www.plantuml.com/plantuml/uml/"
    echo "  - Cole o conteúdo de cada .puml"
    echo "  - Export como PNG para diagrams-rendered/"
    echo ""
    echo "OPÇÃO 3: Usar Docker"
    echo "  - docker run --rm -v \$PWD:/work plantuml/plantuml -png docs/diagrams/*.puml -o /work/diagrams-rendered"
    echo ""
    exit 1
fi

# Processar cada arquivo PlantUML
for puml_file in "$DIAG_DIR"/*.puml; do
    filename=$(basename "$puml_file")
    basename_no_ext="${filename%.puml}"
    output_file="$OUT_DIR/${basename_no_ext}.png"

    echo "Renderizando: $filename → $output_file"

    if command -v plantuml &> /dev/null; then
        plantuml -png "$puml_file" -o "$PWD/$OUT_DIR"
    elif [ -f "$PLANTUML_JAR" ]; then
        java -jar "$PLANTUML_JAR" -png "$puml_file" -o "$PWD/$OUT_DIR"
    fi
done

echo ""
echo "✅ Diagramas gerados em: $OUT_DIR/"
echo ""
echo "Próximo passo: Compile o LaTeX com:"
echo "  pdflatex RelatorioAprendizado.tex"
echo "  ou acesse https://www.overleaf.com e upload de RelatorioAprendizado.tex"

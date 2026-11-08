#!/usr/bin/env python3
"""Gera o ZIP de apresentação exigido no slide de POO do professor Tenório.

Uso: python scripts/gerar-entrega.py Nome1_Nome2_Nome3_Nome4
Exige: Python 3, Graphviz ("dot") e projeto executado da raiz.
O programa mantém somente Main.java como entrada; testes são JUnit.
"""
from __future__ import annotations

import argparse
import re
import shutil
import subprocess
import sys
import tempfile
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
GRAPH = ROOT / "docs" / "diagrama-classes.dot"
INDEX = ROOT / "docs" / "indice-22-itens-poo.md"
EXCLUDES = {".git", ".idea", ".vscode", "target", "dist", "__pycache__", ".pytest_cache"}
ALLOWED_TOP_LEVEL = {"pom.xml", "nbactions.xml", ".gitignore", "src", "docs", "scripts", ".github"}


def validate_rubric_files() -> None:
    """Impedir ZIP incompleto: dois pacotes, único main e 22 itens indexados."""
    if not GRAPH.is_file() or not INDEX.is_file():
        raise RuntimeError("Diagrama e índice devem existir em docs/.")

    source_files = sorted((ROOT / "src").rglob("*.java"))
    packages: set[str] = set()
    main_count = 0
    for source in source_files:
        text = source.read_text(encoding="utf-8")
        match = re.search(r"(?m)^\s*package\s+([\w.]+);", text)
        if not match:
            raise RuntimeError(f"Arquivo sem package: {source}")
        packages.add(match.group(1))
        main_count += len(re.findall(r"\bstatic\s+void\s+main\s*\(", text))

    expected = {"com.alemdoequilibrio.core", "com.alemdoequilibrio.game"}
    if packages != expected:
        raise RuntimeError(f"Esperados exatamente 2 pacotes {expected}; encontrados {packages}")
    if main_count != 1:
        raise RuntimeError(f"Esperado um único método main, encontrados {main_count}")

    indexed = INDEX.read_text(encoding="utf-8")
    wanted = [f"1.{i}" for i in range(1, 6)] + [
        "2.1", "2.2", "3", "4", "5", "6.1", "6.2",
        "7.1", "7.2", "7.3", "7.4", "8", "9", "10", "11", "12", "13"
    ]
    found = re.findall(r"^\| \*\*(\d+(?:\.\d+)?)\*\*", indexed, flags=re.MULTILINE)
    if found != wanted:
        raise RuntimeError("O índice precisa listar exatamente os 22 itens na ordem do slide.")

    graph = GRAPH.read_text(encoding="utf-8")
    for package in expected:
        if package not in graph:
            raise RuntimeError(f"Pacote ausente do diagrama: {package}")
    for item in wanted:
        # Rótulos de vários itens dividem a mesma linha: POO 1.1,1.2,...
        if not re.search(r"\b" + re.escape(item) + r"\b", graph):
            raise RuntimeError(f"Item {item} sem indicação no diagrama.")


def include_file(path: Path) -> bool:
    relative = path.relative_to(ROOT)
    if not relative.parts or relative.parts[0] not in ALLOWED_TOP_LEVEL:
        return False
    if any(part in EXCLUDES or part == "private" for part in relative.parts):
        return False
    if path.suffix in {".class", ".pyc"}:
        return False
    return path.is_file()


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Empacota projeto, índice e diagrama completo em ZIP para POO"
    )
    parser.add_argument(
        "integrantes",
        help="Primeiros nomes separados por _: Nome1_Nome2_Nome3_Nome4"
    )
    args = parser.parse_args()
    label = args.integrantes
    if not re.fullmatch(r"[^\W\d_]+(?:_[^\W\d_]+){1,3}", label, flags=re.UNICODE):
        parser.error("Informe entre 2 e 4 primeiros nomes, separados por sublinhado.")

    try:
        validate_rubric_files()
        graphviz = shutil.which("dot")
        if not graphviz:
            raise RuntimeError(
                "Graphviz ausente. Instale o Graphviz e confira com: dot -V"
            )

        output = ROOT / "dist"
        output.mkdir(exist_ok=True)
        destination = output / (label + ".zip")
        with tempfile.TemporaryDirectory(prefix="poo-entrega-") as workspace:
            pdf = Path(workspace) / "diagrama-classes.pdf"
            svg = Path(workspace) / "diagrama-classes.svg"
            for fmt, target in (("pdf", pdf), ("svg", svg)):
                subprocess.run(
                    [graphviz, "-T" + fmt, str(GRAPH), "-o", str(target)],
                    check=True, timeout=90,
                )
                if not target.is_file() or target.stat().st_size < 100:
                    raise RuntimeError("O diagrama não foi gerado corretamente.")

            files = sorted(
                (p for p in ROOT.rglob("*") if include_file(p)),
                key=lambda p: str(p.relative_to(ROOT)),
            )
            if not files:
                raise RuntimeError("Nenhum arquivo encontrado para a entrega.")
            with zipfile.ZipFile(
                destination, "w", compression=zipfile.ZIP_DEFLATED,
                compresslevel=6,
            ) as archive:
                for file in files:
                    archive.write(file, file.relative_to(ROOT))
                archive.write(pdf, "documentacao/diagrama-classes.pdf")
                archive.write(svg, "documentacao/diagrama-classes.svg")
                archive.writestr(
                    "documentacao/LEIA-ME.txt",
                    "Além do Equilíbrio - Projeto POO\n"
                    "Código: src/main/java (2 pacotes).\n"
                    "Único ponto de entrada: com.alemdoequilibrio.core.Main.\n"
                    "Índice: docs/indice-22-itens-poo.md.\n"
                    "Diagrama de classes: documentacao/diagrama-classes.pdf.\n"
                    "Requisitos de execução: JDK 26, NetBeans e Maven.\n"
                    "Compilar e testar: mvn verify\n"
                    "Rodar: mvn javafx:run\n"
                    "Verificar prazo e equipe antes de entregar.\n",
                )

        with zipfile.ZipFile(destination) as created:
            if created.testzip() is not None:
                raise RuntimeError("O ZIP apresentou erro de integridade.")
        print(f"Entrega gerada: {destination}")
        print(f"Arquivos do projeto: {len(files)}, diagrama PDF e SVG incluídos.")
        return 0
    except (RuntimeError, OSError, subprocess.SubprocessError) as exc:
        print(f"ERRO NA ENTREGA: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())

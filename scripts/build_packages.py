#!/usr/bin/env python3
"""
Script para empacotar somente o código-fonte de produção de cada SDK em arquivos .zip independentes.
Gera pacotes limpos em 'packages/' para cópia e colagem direta em projetos de clientes/empresas,
excluindo suítes de testes, ambientes Docker (.docker-dev-*), arquivos de compilação (bin, obj, target, node_modules) e caches.
"""

import os
import zipfile
from pathlib import Path

ROOT_DIR = Path(__file__).resolve().parent.parent
OUTPUT_DIR = ROOT_DIR / "packages"

PACKAGES_SPEC = {
    "php": {
        "zip_name": "integranotas-php.zip",
        "archive_prefix": "integranotas-php",
        "files": [
            ("php/IntegraClass.php", "IntegraClass.php"),
            ("php/IntegraServices.php", "IntegraServices.php"),
            ("php/README.md", "README.md"),
        ],
        "dirs": [],
    },
    "csharp": {
        "zip_name": "integranotas-csharp.zip",
        "archive_prefix": "integranotas-csharp",
        "files": [
            ("csharp/README.md", "README.md"),
        ],
        "dirs": [
            ("csharp/src/IntegraNotas", "", lambda p: p.suffix in (".cs", ".csproj") and "obj" not in p.parts and "bin" not in p.parts),
        ],
    },
    "typescript": {
        "zip_name": "integranotas-typescript.zip",
        "archive_prefix": "integranotas-typescript",
        "files": [
            ("typescript/package.json", "package.json"),
            ("typescript/tsconfig.json", "tsconfig.json"),
            ("typescript/README.md", "README.md"),
        ],
        "dirs": [
            ("typescript/src", "src", lambda p: p.suffix == ".ts"),
        ],
    },
    "python": {
        "zip_name": "integranotas-python.zip",
        "archive_prefix": "integranotas-python",
        "files": [
            ("python/pyproject.toml", "pyproject.toml"),
            ("python/README.md", "README.md"),
        ],
        "dirs": [
            ("python/integranotas", "integranotas", lambda p: p.suffix == ".py" and "__pycache__" not in p.parts),
        ],
    },
    "golang": {
        "zip_name": "integranotas-golang.zip",
        "archive_prefix": "integranotas-golang",
        "files": [
            ("golang/integranotas.go", "integranotas.go"),
            ("golang/services.go", "services.go"),
            ("golang/validator.go", "validator.go"),
            ("golang/errors.go", "errors.go"),
            ("golang/go.mod", "go.mod"),
            ("golang/README.md", "README.md"),
        ],
        "dirs": [],
    },
    "java": {
        "zip_name": "integranotas-java.zip",
        "archive_prefix": "integranotas-java",
        "files": [
            ("java/pom.xml", "pom.xml"),
            ("java/README.md", "README.md"),
        ],
        "dirs": [
            ("java/src/main/java/br/com/integranotas/sdk", "src/main/java/br/com/integranotas/sdk", lambda p: p.suffix == ".java"),
        ],
    },
    "rust": {
        "zip_name": "integranotas-rust.zip",
        "archive_prefix": "integranotas-rust",
        "files": [
            ("rust/Cargo.toml", "Cargo.toml"),
            ("rust/README.md", "README.md"),
        ],
        "dirs": [
            ("rust/src", "src", lambda p: p.suffix == ".rs"),
        ],
    },
}


def build_package(sdk_key: str, spec: dict):
    zip_path = OUTPUT_DIR / spec["zip_name"]
    prefix = spec["archive_prefix"]

    added_files = []

    with zipfile.ZipFile(zip_path, "w", compression=zipfile.ZIP_DEFLATED) as zf:
        # Adiciona arquivos individuais
        for src_rel, dest_rel in spec.get("files", []):
            src_file = ROOT_DIR / src_rel
            if src_file.exists():
                arcname = f"{prefix}/{dest_rel}" if dest_rel else prefix
                zf.write(src_file, arcname=arcname)
                added_files.append(dest_rel)
            else:
                print(f"  [AVISO] Arquivo não encontrado: {src_rel}")

        # Adiciona diretórios com filtro
        for src_dir_rel, dest_dir_rel, filter_fn in spec.get("dirs", []):
            src_dir = ROOT_DIR / src_dir_rel
            if src_dir.exists():
                for root, _, files in os.walk(src_dir):
                    for file in files:
                        p = Path(root) / file
                        if filter_fn(p):
                            rel_to_src = p.relative_to(src_dir)
                            if dest_dir_rel:
                                arcname = f"{prefix}/{dest_dir_rel}/{rel_to_src}"
                            else:
                                arcname = f"{prefix}/{rel_to_src}"
                            zf.write(p, arcname=arcname)
                            added_files.append(str(rel_to_src))
            else:
                print(f"  [AVISO] Diretório não encontrado: {src_dir_rel}")

    size_kb = zip_path.stat().st_size / 1024
    print(f"✔ Pacote [{sdk_key.upper()}] gerado: {zip_path.name} ({len(added_files)} arquivos, {size_kb:.1f} KB)")


def main():
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    print("Iniciando geração de pacotes limpos de código-fonte...")

    for sdk_key, spec in PACKAGES_SPEC.items():
        build_package(sdk_key, spec)

    print("\nTodos os pacotes foram gerados com sucesso na pasta 'packages/'!")


if __name__ == "__main__":
    main()

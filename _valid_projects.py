import os
import json
import xml.etree.ElementTree as ET

CURRENT_DIR = os.path.dirname(__file__)
BAN_DIR = ['.vscode', '.idea', '.git']


def get_client_projects() -> [str]:
    res: [str] = []
    for path in os.listdir(CURRENT_DIR):
        if path in BAN_DIR:
            continue
        project_path = os.path.join(CURRENT_DIR, path)
        if os.path.isdir(project_path):
            res.append(project_path)
    return res


def check_maven_package(pom_file_path: str):
    try:
        tree = ET.parse(pom_file_path)
        root = tree.getroot()
        namespaces = {
            '': 'http://maven.apache.org/POM/4.0.0'
        }

        packaging = root.find('packaging', namespaces)

        if packaging is not None and packaging.text == 'war':
            raise Exception(f"Packaging is 'war' in {pom_file_path}. WAR packaging is not allowed.")
    except Exception as e:
        raise Exception(f"{pom_file_path} parse fail.")


def validate_project(project_dir: str):
    # validate base file
    old_pom_path = os.path.join(project_dir, 'pom.xml.old')
    if not os.path.exists(old_pom_path):
        raise Exception(f"Missing file: {old_pom_path}. This file is required for project validation.")
    check_maven_package(old_pom_path)

    new_pom_path = os.path.join(project_dir, 'pom.xml.new')
    if not os.path.exists(new_pom_path):
        raise Exception(f"Missing file: {new_pom_path}. This file is required for project validation.")
    check_maven_package(new_pom_path)

    metadata_path = os.path.join(project_dir, 'GT_Metadata.json')
    if not os.path.exists(metadata_path):
        raise Exception(f"Missing file: {metadata_path}. This file is required for project validation.")

    # validate metadata file
    with open(metadata_path, 'r') as f:
        metadata = json.load(f)

    maven_project_path = os.path.join(project_dir, metadata['root_path'])
    if not os.path.isdir(maven_project_path):
        raise Exception(
            f"Invalid root path in metadata: {maven_project_path}. The specified directory does not exist or is not a directory.")

    change_pom_file = os.path.join(maven_project_path, metadata['change_pom_path'])
    if not os.path.exists(change_pom_file):
        raise Exception(f"Missing POM file in metadata: {change_pom_file}. The specified POM file does not exist.")


def main():
    client_projects = get_client_projects()
    for project_dir in client_projects:
        validate_project(project_dir)
    print('Validate Success.')


if __name__ == '__main__':
    main()

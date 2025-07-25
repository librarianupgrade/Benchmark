import os
import json
import subprocess
from multiprocessing import Manager
import cpu_heater

CURRENT_DIR = os.path.dirname(__file__)
BAN_DIR = ['.vscode', '.idea', '.git']
CACHE_FILE_NAME = '_cache_projects.json'
CACHE_FILE_PATH = os.path.join(CURRENT_DIR, CACHE_FILE_NAME)
RECORD_FILE_NAME = '_record_package.json'
RECORD_FILE_PATH = os.path.join(CURRENT_DIR, RECORD_FILE_NAME)
FAIL_PROJECT_PATH = os.path.join(CURRENT_DIR, '_fail_package_projects.json')

def get_client_projects() -> [str]:
    res: [str] = []
    for path in os.listdir(CURRENT_DIR):
        if path in BAN_DIR:
            continue
        project_path = os.path.join(CURRENT_DIR, path)
        if os.path.isdir(project_path):
            res.append(project_path)
    return res


def package_client_project(project_path: str, lock, fail_project_list):
    metadata_file_path = os.path.join(project_path, 'GT_Metadata.json')
    if not os.path.exists(metadata_file_path):
        raise FileNotFoundError(metadata_file_path)

    with open(metadata_file_path, 'r') as f:
        metadata: dict = json.load(f)

    old_pom_path = os.path.join(project_path, 'pom.xml.old')
    assert os.path.exists(old_pom_path)
    new_pom_path = os.path.join(project_path, 'pom.xml.new')
    assert os.path.exists(new_pom_path)

    maven_project_path = os.path.join(project_path, metadata.get('root_path'))
    assert os.path.exists(maven_project_path)

    modify_pom_path = os.path.join(maven_project_path, metadata.get('change_pom_path'), 'pom.xml')
    assert os.path.exists(modify_pom_path)

    os.chdir(maven_project_path)
    with open(old_pom_path, 'r', encoding='utf-8') as file_A:
        content_A = file_A.read()

    with open(modify_pom_path, 'w', encoding='utf-8') as file_B:
        file_B.write(content_A)

    result = subprocess.run(['mvn', 'clean', 'package', '-DskipTests'], capture_output=True, text=True,
                            cwd=maven_project_path)

    if 'BUILD SUCCESS' in result.stdout:
        with lock:
            with open(RECORD_FILE_PATH, 'r') as f:
                record_map = json.load(f)
            record_map[project_path] = 1
            with open(RECORD_FILE_PATH, 'w') as f:
                f.write(json.dumps(record_map, indent=4, ensure_ascii=False))
    else:
        with lock:
            with open(RECORD_FILE_PATH, 'r') as f:
                record_map = json.load(f)
            record_map[project_path] = 2
            with open(RECORD_FILE_PATH, 'w') as f:
                f.write(json.dumps(record_map, indent=4, ensure_ascii=False))
            fail_project_list.append(project_path)
            print(f'{project_path} mvn clean package fail.')


def switch_jdk(jdk_version: str):
    command = {
        '1.8': 'jenv global 1.8',
        '11': 'jenv global 11',
        '17': 'jenv global 17',
        '23': 'jenv global 23'
    }

    command_str = command.get(jdk_version.strip())

    if command_str is None:
        raise Exception(f"{jdk_version}'s JDK version is not valid.")
    os.system(command_str)


def classify(client_projects):
    res: dict = {
        '1.8': [],
        '11': [],
        '17': [],
        '23': []
    }
    for project_path in client_projects:
        metadata_file_path = os.path.join(project_path, 'GT_Metadata.json')
        if not os.path.exists(metadata_file_path):
            raise FileNotFoundError(metadata_file_path)

        with open(metadata_file_path, 'r') as f:
            metadata: dict = json.load(f)
        res[metadata.get('jdk_version')].append(project_path)
    return res


def main():
    client_projects = get_client_projects()
    record_map = dict()
    for project_path in client_projects:
        record_map[project_path] = 0

    with open(RECORD_FILE_PATH, 'w') as f:
        f.write(json.dumps(record_map, indent=4, ensure_ascii=False))

    with Manager() as manager:
        lock = manager.Lock()
        jdk_map = classify(client_projects)
        fail_project_list = manager.list()

        for jdk_version, project_path_list in jdk_map.items():
            switch_jdk(jdk_version)
            args = [(project_path, lock, fail_project_list) for project_path in project_path_list]
            cpu_heater.multiprocess(package_client_project, args, max_workers=20, show_progress=True)

        with open(FAIL_PROJECT_PATH, 'w') as f:
            f.write(json.dumps(list(fail_project_list), indent=4, ensure_ascii=False))


if __name__ == '__main__':
    main()

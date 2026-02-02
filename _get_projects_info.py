import os
import json

CURRENT_DIR = os.path.dirname(__file__)
BAN_DIR = ['.vscode', '.idea', '.git', '.github', '__pycache__']
PROJECTS_FILE_NAME = '_project_name_list.json'
PROJECTS_FILE_PATH = os.path.join(CURRENT_DIR, PROJECTS_FILE_NAME)


def get_client_projects() -> [str]:
    res: [str] = []
    for path in os.listdir(CURRENT_DIR):
        if path in BAN_DIR:
            continue
        project_path = os.path.join(CURRENT_DIR, path)
        if os.path.isdir(project_path):
            res.append(project_path)
    return res


def main():
    project_name_list: list = []
    client_projects = get_client_projects()
    for project_path in client_projects:
        project_name = os.path.basename(project_path)
        project_name_list.append(project_name)

    with open(PROJECTS_FILE_PATH, 'w') as f:
        f.write(json.dumps(project_name_list, indent=4, ensure_ascii=False))


if __name__ == '__main__':
    main()

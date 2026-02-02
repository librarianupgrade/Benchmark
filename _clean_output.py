import os
import shutil

CURRENT_DIR = os.path.dirname(__file__)
BAN_DIR = ['.vscode', '.idea', '.git', '.github', '__pycache__']


def get_client_projects() -> [str]:
    res: [str] = []
    for path in os.listdir(CURRENT_DIR):
        if path in BAN_DIR:
            continue
        project_path = os.path.join(CURRENT_DIR, path)
        if os.path.isdir(project_path):
            res.append(project_path)
    return res


def remove_drstrange_output(project_dir: str):
    output_dir = os.path.join(project_dir, 'output')
    if os.path.exists(output_dir):
        shutil.rmtree(output_dir)


def main():
    client_projects = get_client_projects()
    for project_dir in client_projects:
        remove_drstrange_output(project_dir)
    print('Clean Output Directory Success.')


if __name__ == '__main__':
    main()

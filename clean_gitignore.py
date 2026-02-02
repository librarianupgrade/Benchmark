import os

def delete_gitignore_files(root_path: str):
    if not os.path.isabs(root_path):
        raise ValueError("请输入绝对路径，例如 /Users/xxx/project 或 C:\\Users\\xxx\\project")

    if not os.path.isdir(root_path):
        raise FileNotFoundError(f"路径不存在或不是文件夹: {root_path}")

    deleted = 0

    for dirpath, dirnames, filenames in os.walk(root_path):
        if ".gitignore" in filenames:
            target_file = os.path.join(dirpath, ".gitignore")
            try:
                os.remove(target_file)
                deleted += 1
                print(f"[删除成功] {target_file}")
            except Exception as e:
                print(f"[删除失败] {target_file} -> {e}")

    print(f"\n完成：共删除 {deleted} 个 .gitignore 文件")


if __name__ == "__main__":
    path = os.path.dirname(__file__)
    delete_gitignore_files(path)

### Benchmark Introduction

Each folder in this repository represents a single library upgrade sample. Inside each folder, you will find three sample-specific files:

- `pom.xml.old`: The Maven POM file before the library upgrade.  
- `pom.xml.new`: The Maven POM file after the library upgrade.  
- `GT_Metadata.json`: Detailed metadata for the sample. 


For example:
```json
{
  "id": "qcz-left__GT__slv-init-manage__1", 
  "client_project_name": "qcz-left/slv-init-manage",
  "github_url": "git@github.com:qcz-left/slv-init-manage.git",
  "before_upgrade_commit": "9f0d11854853d3a7f2aa4cfe28cf56a162c03159",
  "after_upgrade_commit": "17f155a4499938d272232548838313a0669e5ac8",
  "incompatibility_category": "",
  "root_path": "",
  "change_pom_path": "",
  "old_version": "pom.xml.old",
  "new_version": "pom.xml.new",
  "jdk_version": "11",
  "fixed_commit_url": "https://github.com/qcz-left/slv-init-manage/commit/17f155a4499938d272232548838313a0669e5ac8",
  "fix_type": 0
}
```
The `GT_Metadata.json` file includes the following fields:

- id: The sample folder name.

- client_project_name: The GitHub repository name.

- github_url: The Git clone URL for the repository.

- before_upgrade_commit: The commit SHA before the library was upgraded.

- after_upgrade_commit: The commit SHA after the library was upgraded.

- root_path: Relative path to the project root (useful for multi-module projects).

- change_pom_path: Relative path to the modified pom.xml file (useful for multi-module projects).

- old_version: File name of the pre-upgrade POM (pom.xml.old).

- new_version: File name of the post-upgrade POM (pom.xml.new).

- jdk_version: The JDK version used by the project (e.g., 11).

- fixed_commit_url: The URLs of the commit in which developers fixed incompatibility errors.
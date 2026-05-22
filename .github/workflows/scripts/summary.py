"""
A script to scan through all valid mod jars in build-artifacts.zip/$version/build/libs,
and generate an artifact summary table for that to GitHub action step summary
"""
__author__ = 'Fallen_Breath'

import functools
import glob
import hashlib
import json
import os

import jproperties


def read_prop(file_name: str, key: str) -> str:
    configs = jproperties.Properties()
    with open(file_name, 'rb') as f:
        configs.load(f)
    return configs[key].data


def get_sha256_hash(file_path: str) -> str:
    sha256_hash = hashlib.sha256()
    with open(file_path, 'rb') as f:
        for buf in iter(functools.partial(f.read, 4096), b''):
            sha256_hash.update(buf)
    return sha256_hash.hexdigest()


def human_readable_size(file_size: int) -> str:
    if file_size < 1024:
        return f"{file_size} B"
    elif file_size < 1024 ** 2:
        return f"{file_size / 1024:.1f} KB"
    elif file_size < 1024 ** 3:
        return f"{file_size / (1024 ** 2):.1f} MB"
    else:
        return f"{file_size / (1024 ** 3):.1f} GB"


def main():
    target_subproject_env = os.environ.get('TARGET_SUBPROJECT', '')
    target_subprojects = list(filter(None, target_subproject_env.split(',') if target_subproject_env != '' else []))
    print('target_subprojects: {}'.format(target_subprojects))

    with open('settings.json') as f:
        settings: dict = json.load(f)

    with open(os.environ['GITHUB_STEP_SUMMARY'], 'w') as f:
        f.write('## Build Artifacts Summary\n\n')
        f.write('| Subproject | for Minecraft | File | Size | SHA-256 |\n')
        f.write('| --- | --- | --- | --- | --- |\n')

        warnings = []
        for subproject in settings['versions']:
            if len(target_subprojects) > 0 and subproject not in target_subprojects:
                print('skipping {}'.format(subproject))
                continue
            game_versions = read_prop('versions/{}/gradle.properties'.format(subproject), 'game_versions')
            game_versions = game_versions.strip().replace('\r', '').replace('\n', ', ')
            file_paths = glob.glob('build-artifacts/tmp/submods/META-INF/jars/*.jar'.format(subproject))
            file_paths = list(filter(lambda fp: not fp.endswith('-sources.jar') and not fp.endswith('-dev.jar') and not fp.endswith('-shadow.jar'),file_paths))
            if len(file_paths) == 0:
                file_name = '*not found*'
                file_size = '*N/A*'
                sha256 = '*N/A*'
            else:
                file_name = f'`{os.path.basename(file_paths[0])}`'
                file_size = human_readable_size(os.path.getsize(file_paths[0]))
                sha256 = f'`{get_sha256_hash(file_paths[0])}`'
                if len(file_paths) > 1:
                    warnings.append('Found too many build files in subproject {}: {}'.format(subproject, ', '.join(file_paths)))

            f.write('| {} | {} | {} | {} | {} |\n'.format(subproject, game_versions, file_name, file_size, sha256))

        if len(warnings) > 0:
            f.write('\n### Warnings\n\n')
            for warning in warnings:
                f.write('- {}\n'.format(warning))


if __name__ == '__main__':
    main()
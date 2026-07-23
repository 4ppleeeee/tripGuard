import { hapTasks } from '@ohos/hvigor-ohos-plugin';
import { hvigor, HvigorNode, HvigorPlugin } from '@ohos/hvigor';
import { execSync } from 'node:child_process';
import * as path from 'path';
import * as fs from 'fs';
import * as os from 'os';

function qnKuiklyCompilePlugin(): HvigorPlugin {
    return {
        pluginId: 'qnKuiklyCompilePlugin',
        apply(node: HvigorNode) {
            if (os.platform() !== 'darwin') {
                return;
            }

            const config = readLocalProperties();
            if (config.compilePluginEnabled === 'false') {
                return;
            }

            node.registerTask({
                name: 'kuikly_build',
                run: () => {
                    runKuiklyBuild(config);
                },
                postDependencies: ['default@PreBuild'],
            });

            node.registerTask({
                name: 'kuikly_clean',
                run: () => {
                    runKuiklyClean(config);
                },
                postDependencies: ['clean'],
            });
        },
    };
}

interface KuiklyConfig {
    projectPath: string;
    moduleName: string;
    ohosGradleSettings: string;
    soPath: string;
    headerPath: string;
    compilePluginEnabled: string;
}

function readLocalProperties(): KuiklyConfig {
    const rootPath = hvigor.getRootNode().getNodePath();
    const localPropsPath = path.join(rootPath, 'local.properties');

    const config: KuiklyConfig = {
        projectPath: '../.',
        moduleName: 'umbrella',
        ohosGradleSettings: 'settings.gradle.kts',
        soPath: 'entry/libs/arm64-v8a',
        headerPath: 'shared/src/main/cpp',
        compilePluginEnabled: 'true',
    };

    if (!fs.existsSync(localPropsPath)) {
        throw new Error('[qnKuiklyCompilePlugin] local.properties 不存在，请新建并填写 kuikly.* 参数');
    }

    const content = fs.readFileSync(localPropsPath, 'utf-8');
    content.split('\n').forEach((line) => {
        const trimmed = line.trim();
        if (!trimmed.startsWith('kuikly.')) return;
        const eq = trimmed.indexOf('=');
        if (eq < 0) return;
        const key = trimmed.substring(0, eq).trim();
        const value = trimmed.substring(eq + 1).trim();
        switch (key) {
            case 'kuikly.projectPath':
                config.projectPath = value;
                break;
            case 'kuikly.moduleName':
                config.moduleName = value;
                break;
            case 'kuikly.ohosGradleSettings':
                config.ohosGradleSettings = value;
                break;
            case 'kuikly.soPath':
                config.soPath = value;
                break;
            case 'kuikly.headerPath':
                config.headerPath = value;
                break;
            case 'kuikly.compilePluginEnabled':
                config.compilePluginEnabled = value;
                break;
        }
    });
    return config;
}

function getBuildMode(): 'debug' | 'release' {
    const params = hvigor.getParameter() as any;
    const ext = typeof params.getExtParams === 'function' ? params.getExtParams() : {};
    const mode = (ext && ext.buildMode) || 'debug';
    return mode === 'release' ? 'release' : 'debug';
}

function getLinkOutputDirName(buildMode: 'debug' | 'release'): string {
    return buildMode === 'release' ? 'umbrellaReleaseShared' : 'debugShared';
}

function capitalize(value: string): string {
    return value.charAt(0).toUpperCase() + value.slice(1);
}

function runKuiklyBuild(config: KuiklyConfig) {
    const buildMode = getBuildMode();
    const buildModeCap = capitalize(buildMode);
    const outputDir = getLinkOutputDirName(buildMode);
    const settingsArg = config.ohosGradleSettings ? ` -c ${config.ohosGradleSettings}` : '';
    const gradleCmd =
        `./gradlew${settingsArg} :${config.moduleName}:link${buildModeCap}SharedOhosArm64` +
        ` -Pqqnews.kmm.build.platform=ohos` +
        (buildMode === 'debug' ? ` -Pdebug.mode=true` : ` -Pdebug.mode=false`);
    const soFile = `lib${config.moduleName}.so`;
    const headerFile = `lib${config.moduleName}_api.h`;

    const script =
        `#!/bin/sh\n` +
        `set -e\n` +
        `export QN_COMPAT_BUILD_TYPE=ohos\n` +
        `if [ -x "$(command -v /usr/libexec/java_home)" ]; then\n` +
        `  export JAVA_HOME=$(/usr/libexec/java_home -v 17)\n` +
        `  export PATH=$JAVA_HOME/bin:$PATH\n` +
        `fi\n` +
        `echo "[qnKuiklyCompilePlugin] working path: $(pwd)"\n` +
        `echo "[qnKuiklyCompilePlugin] buildMode: ${buildMode}"\n` +
        `ohosAppPath=$(pwd)\n` +
        `echo "[qnKuiklyCompilePlugin] sync compose resources"\n` +
        `TARGET_RES_DIR=${config.projectPath}/wsCompose/src/commonMain/composeResources\n` +
        `OHOS_RES_DIR=\${ohosAppPath}/entry/src/main/resources/resfile\n` +
        `if [ -d "$TARGET_RES_DIR" ]; then\n` +
        `  rm -rf "$OHOS_RES_DIR"\n` +
        `  mkdir -p "$(dirname "$OHOS_RES_DIR")"\n` +
        `  cp -rf "$TARGET_RES_DIR" "$OHOS_RES_DIR"\n` +
        `else\n` +
        `  echo "[qnKuiklyCompilePlugin] WARN: $TARGET_RES_DIR not found"\n` +
        `fi\n` +
        `echo "[qnKuiklyCompilePlugin] remove last production"\n` +
        `rm -f ./${config.soPath}/${soFile}\n` +
        `rm -f ./${config.headerPath}/${headerFile}\n` +
        `echo "[qnKuiklyCompilePlugin] run gradle"\n` +
        `pushd ${config.projectPath}\n` +
        `${gradleCmd}\n` +
        `popd\n` +
        `LINK_DIR=${config.projectPath}/${config.moduleName}/build/bin/ohosArm64/${outputDir}\n` +
        `if [ ! -f "$LINK_DIR/${soFile}" ]; then\n` +
        `  echo "[qnKuiklyCompilePlugin] ERROR: $LINK_DIR/${soFile} not found"\n` +
        `  exit 1\n` +
        `fi\n` +
        `if [ ! -f "$LINK_DIR/${headerFile}" ]; then\n` +
        `  echo "[qnKuiklyCompilePlugin] ERROR: $LINK_DIR/${headerFile} not found"\n` +
        `  exit 1\n` +
        `fi\n` +
        `mkdir -p \${ohosAppPath}/${config.soPath}\n` +
        `cp $LINK_DIR/${soFile} \${ohosAppPath}/${config.soPath}/${soFile}\n` +
        `mkdir -p \${ohosAppPath}/${config.headerPath}\n` +
        `cp $LINK_DIR/${headerFile} \${ohosAppPath}/${config.headerPath}/${headerFile}\n` +
        `echo "[qnKuiklyCompilePlugin] rebuild kuikly finish"\n`;

    try {
        execSync(script, { stdio: 'inherit' });
    } catch (e) {
        throw new Error('kuikly gradle build Error (custom plugin), please check gradle output above');
    }
}

function runKuiklyClean(config: KuiklyConfig) {
    const soFile = `lib${config.moduleName}.so`;
    const headerFile = `lib${config.moduleName}_api.h`;
    const script =
        `#!/bin/sh\n` +
        `set -e\n` +
        `echo "[qnKuiklyCompilePlugin] working path: $(pwd)"\n` +
        `echo "[qnKuiklyCompilePlugin] remove last production"\n` +
        `rm -f ./${config.soPath}/${soFile}\n` +
        `rm -f ./${config.headerPath}/${headerFile}\n` +
        `rm -rf ./entry/.cxx\n` +
        `echo "[qnKuiklyCompilePlugin] clean gradle"\n` +
        `pushd ${config.projectPath}\n` +
        `./gradlew :${config.moduleName}:clean -Pqqnews.kmm.build.platform=ohos\n` +
        `./gradlew --stop\n` +
        `popd\n`;

    try {
        execSync(script, { stdio: 'inherit' });
    } catch (e) {
        throw new Error('kuikly gradle clean Error (custom plugin), please check gradle output above');
    }
}

export default {
    system: hapTasks,
    plugins: [qnKuiklyCompilePlugin()],
};

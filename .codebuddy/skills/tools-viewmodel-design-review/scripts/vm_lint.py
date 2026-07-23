#!/usr/bin/env python3
"""
ViewModel 架构审查 - 静态预检脚本

对 VM 相关文件执行确定性的文本匹配检查，快速发现硬性违规。
将结果输出为 JSON，供 SKILL 直接消费，减少 AI 逐行审查的工作量。

所有工程相关参数从 config.json 读取，迁移到新工程时无需修改本脚本。

用法：
    python3 vm_lint.py [file1.kt file2.kt ...]
    若不传文件参数，则自动从 git diff 中提取 VM 相关文件。

输出：JSON 格式的审查结果到 stdout
"""

import json
import os
import re
import subprocess
import sys
from dataclasses import dataclass, field, asdict
from enum import Enum
from pathlib import Path


# ============================================================
# 配置加载
# ============================================================

def load_config() -> dict:
    """从 config.json 加载配置，支持脚本所在目录和当前目录两种查找方式"""
    script_dir = Path(__file__).parent.parent  # scripts/ 的上级目录即 skill 根目录
    config_path = script_dir / "config.json"
    if not config_path.exists():
        # 尝试当前工作目录
        config_path = Path("config.json")
    if not config_path.exists():
        print(json.dumps({"error": f"找不到 config.json，已搜索: {script_dir / 'config.json'}"}), file=sys.stderr)
        sys.exit(1)
    with open(config_path, "r", encoding="utf-8") as f:
        return json.load(f)


CONFIG = load_config()

# 从配置中读取参数
INTERFACE_PATH_MARKER = CONFIG["layers"]["interface"]["path_marker"]
UI_PATH_MARKER = CONFIG["layers"]["ui"]["path_marker"]
WHITELIST_TYPES = set(CONFIG["whitelist_types"])
FORBIDDEN_UI_CALLS = CONFIG["forbidden_ui_calls"]
MUTABLE_KEYWORDS = CONFIG["mutable_keywords"]
SAFE_TYPE_PREFIXES = tuple(CONFIG["safe_type_prefixes"])
BUSINESS_MODEL_SUFFIXES = tuple(CONFIG["business_model_suffixes"])
VM_FILE_KEYWORDS = CONFIG["vm_file_keywords"]
FILE_EXTENSION = CONFIG["file_extension"]
TARGET_BRANCH = CONFIG["git"]["target_branch"]


# ============================================================
# 数据结构
# ============================================================

class Severity(str, Enum):
    CRITICAL = "critical"
    WARNING = "warning"
    INFO = "info"


@dataclass
class Issue:
    file: str
    line: int
    rule: str
    severity: str
    message: str
    code: str = ""


@dataclass
class LintResult:
    """最终输出结构"""
    vm_files: list = field(default_factory=list)
    interface_files: list = field(default_factory=list)
    impl_files: list = field(default_factory=list)
    ui_files: list = field(default_factory=list)
    issues: list = field(default_factory=list)
    summary: dict = field(default_factory=dict)
    config_used: dict = field(default_factory=dict)  # 输出使用的配置，便于调试


# ============================================================
# 工具函数
# ============================================================

def get_diff_files() -> list[str]:
    """从 git diff 获取变更文件列表"""
    try:
        result = subprocess.run(
            ["git", "diff", "--name-only", f"{TARGET_BRANCH}...HEAD"],
            capture_output=True, text=True, check=True
        )
        return [f.strip() for f in result.stdout.strip().split("\n") if f.strip()]
    except subprocess.CalledProcessError:
        return []


def classify_file(filepath: str) -> str:
    """判断文件所属层级"""
    basename = os.path.basename(filepath)
    has_vm_keyword = any(kw in basename for kw in VM_FILE_KEYWORDS)

    if INTERFACE_PATH_MARKER in filepath and has_vm_keyword:
        return "interface"
    if UI_PATH_MARKER in filepath:
        return "ui"
    if has_vm_keyword:
        return "impl"
    return "unknown"


def is_vm_related(filepath: str) -> bool:
    """判断是否为 VM 相关文件"""
    if not filepath.endswith(FILE_EXTENSION):
        return False
    basename = os.path.basename(filepath)
    # 接口层目录中含 VM 关键字的文件
    if INTERFACE_PATH_MARKER in filepath and any(kw in basename for kw in VM_FILE_KEYWORDS):
        return True
    # UI 层目录中的文件（可能消费 VM）
    if UI_PATH_MARKER in filepath:
        return True
    # 业务模块中含 VM 关键字的文件
    if any(kw in basename for kw in VM_FILE_KEYWORDS):
        return True
    return False


def read_file_lines(filepath: str) -> list[str]:
    """读取文件内容，返回行列表"""
    try:
        with open(filepath, "r", encoding="utf-8") as f:
            return f.readlines()
    except (FileNotFoundError, UnicodeDecodeError):
        return []


# ============================================================
# 检查规则
# ============================================================

def check_interface_file(filepath: str, lines: list[str]) -> list[Issue]:
    """接口层检查"""
    issues = []
    has_stateflow = False
    has_fun = False
    has_val = False
    in_interface_block = False

    for i, line in enumerate(lines, 1):
        stripped = line.strip()

        # 跟踪是否在 interface 块内
        if re.match(r"^interface\s+", stripped):
            in_interface_block = True
        if in_interface_block and stripped == "}":
            in_interface_block = False

        if not in_interface_block:
            continue

        # 检查 var（硬性）
        if re.match(r"\s*var\s+\w+", line):
            issues.append(Issue(
                file=filepath, line=i, rule="H2-var",
                severity=Severity.CRITICAL,
                message="接口中使用了 var，应改为 val",
                code=stripped
            ))

        # 检查 MutableStateFlow / MutableSharedFlow（硬性）
        for kw in MUTABLE_KEYWORDS:
            if kw in line:
                issues.append(Issue(
                    file=filepath, line=i, rule="H2-mutable-flow",
                    severity=Severity.CRITICAL,
                    message=f"接口暴露了 {kw}，应改为只读 Flow",
                    code=stripped
                ))

        # 检查非白名单业务模型泄漏（硬性）
        type_match = re.search(r"val\s+\w+\s*:\s*(I[A-Z]\w+)", line)
        if type_match:
            type_name = type_match.group(1)
            if type_name not in WHITELIST_TYPES:
                if type_name.endswith(BUSINESS_MODEL_SUFFIXES) and not type_name.startswith(SAFE_TYPE_PREFIXES):
                    issues.append(Issue(
                        file=filepath, line=i, rule="H1-business-model",
                        severity=Severity.CRITICAL,
                        message=f"接口暴露了非白名单业务模型 {type_name}",
                        code=stripped
                    ))

        # 统计结构完整性
        if "StateFlow" in line or "SharedFlow" in line:
            has_stateflow = True
        if re.match(r"\s*fun\s+", line):
            has_fun = True
        if re.match(r"\s*val\s+", line):
            has_val = True

        # 检查公开方法是否有注释
        if re.match(r"\s*fun\s+", line):
            prev_line = lines[i - 2].strip() if i >= 2 else ""
            has_comment = (
                prev_line.startswith("//") or
                prev_line.startswith("/**") or
                prev_line.startswith("*") or
                prev_line.endswith("*/")
            )
            if not has_comment:
                issues.append(Issue(
                    file=filepath, line=i, rule="H6-no-comment",
                    severity=Severity.CRITICAL,
                    message="新增公开方法缺少注释",
                    code=stripped
                ))

    # 结构完整性
    if not has_val:
        issues.append(Issue(file=filepath, line=0, rule="S2.1-no-val",
                            severity=Severity.WARNING, message="接口中未发现固定属性（val）"))
    if not has_stateflow:
        issues.append(Issue(file=filepath, line=0, rule="S2.1-no-flow",
                            severity=Severity.INFO, message="接口中未发现 StateFlow/SharedFlow（简单场景可忽略）"))
    if not has_fun:
        issues.append(Issue(file=filepath, line=0, rule="S2.1-no-fun",
                            severity=Severity.WARNING, message="接口中未发现动作方法（fun）"))

    return issues


def check_impl_file(filepath: str, lines: list[str]) -> list[Issue]:
    """实现层检查"""
    issues = []
    for i, line in enumerate(lines, 1):
        stripped = line.strip()
        for kw in MUTABLE_KEYWORDS:
            if kw in line and "private" not in line:
                issues.append(Issue(
                    file=filepath, line=i, rule="H2-public-mutable-flow",
                    severity=Severity.CRITICAL,
                    message=f"{kw} 未标记为 private",
                    code=stripped
                ))
    return issues


def check_ui_file(filepath: str, lines: list[str]) -> list[Issue]:
    """UI 层检查"""
    issues = []
    interface_pkg = INTERFACE_PATH_MARKER.replace("/", ".").rstrip(".")

    for i, line in enumerate(lines, 1):
        stripped = line.strip()

        # 检查 UI 中是否调用了禁止的业务服务
        for call in FORBIDDEN_UI_CALLS:
            if call in line and not stripped.startswith("//") and not stripped.startswith("*"):
                issues.append(Issue(
                    file=filepath, line=i, rule="H5-ui-business-call",
                    severity=Severity.CRITICAL,
                    message=f"UI 层直接调用了业务服务 {call}",
                    code=stripped
                ))

        # 检查 import 是否引用了实现类
        if stripped.startswith("import "):
            import_target = stripped.removeprefix("import ").split(" as ")[0].strip()
            imported_name = import_target.split(".")[-1]
            has_vm_keyword = any(kw in imported_name for kw in VM_FILE_KEYWORDS)
            is_vm_interface = imported_name.startswith("I") and has_vm_keyword
            if has_vm_keyword and interface_pkg not in import_target and not is_vm_interface:
                issues.append(Issue(
                    file=filepath, line=i, rule="H4-ui-depends-impl",
                    severity=Severity.CRITICAL,
                    message="UI 层可能导入了 VM 实现类而非接口",
                    code=stripped
                ))

    return issues


# ============================================================
# 主流程
# ============================================================

def main():
    if len(sys.argv) > 1:
        all_files = sys.argv[1:]
    else:
        all_files = get_diff_files()

    vm_files = [f for f in all_files if is_vm_related(f)]

    interface_files, impl_files, ui_files = [], [], []
    for f in vm_files:
        layer = classify_file(f)
        if layer == "interface":
            interface_files.append(f)
        elif layer == "impl":
            impl_files.append(f)
        elif layer == "ui":
            ui_files.append(f)

    all_issues = []
    for f in interface_files:
        lines = read_file_lines(f)
        if lines:
            all_issues.extend(check_interface_file(f, lines))
    for f in impl_files:
        lines = read_file_lines(f)
        if lines:
            all_issues.extend(check_impl_file(f, lines))
    for f in ui_files:
        lines = read_file_lines(f)
        if lines:
            all_issues.extend(check_ui_file(f, lines))

    critical_count = sum(1 for i in all_issues if i.severity == Severity.CRITICAL)
    warning_count = sum(1 for i in all_issues if i.severity == Severity.WARNING)
    info_count = sum(1 for i in all_issues if i.severity == Severity.INFO)

    result = LintResult(
        vm_files=vm_files,
        interface_files=interface_files,
        impl_files=impl_files,
        ui_files=ui_files,
        issues=[asdict(i) for i in all_issues],
        summary={
            "total_vm_files": len(vm_files),
            "total_issues": len(all_issues),
            "critical": critical_count,
            "warning": warning_count,
            "info": info_count,
            "has_hard_violation": critical_count > 0,
        },
        config_used={
            "interface_marker": INTERFACE_PATH_MARKER,
            "ui_marker": UI_PATH_MARKER,
            "target_branch": TARGET_BRANCH,
            "whitelist_types": list(WHITELIST_TYPES),
            "forbidden_ui_calls": FORBIDDEN_UI_CALLS,
        }
    )

    print(json.dumps(asdict(result), ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()

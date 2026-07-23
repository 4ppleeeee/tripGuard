#!/bin/sh
# 公共脚本：合并 KNIOGEN 目录下各模块子目录的 knoi 产物
# 用法: merge_kniogen.sh <KNIOGEN_DIR> [OUTPUT_DIR]
#   KNIOGEN_DIR: knoi 产物的源目录（包含各模块子目录）
#   OUTPUT_DIR:  合并后的输出目录（可选，默认等于 KNIOGEN_DIR）
#
# 优化：仅在内容变化时才更新输出文件，避免触发不必要的重编译

KNIOGEN_DIR="$1"
OUTPUT_DIR="${2:-$KNIOGEN_DIR}"

if [ -z "$KNIOGEN_DIR" ]; then
  echo "错误：请指定 KNIOGEN_DIR 参数"
  exit 1
fi

# 写入临时文件，仅在内容变化时才覆盖目标文件
update_if_changed() {
  local tmp_file="$1"
  local target_file="$2"
  if [ -f "$target_file" ] && cmp -s "$tmp_file" "$target_file"; then
    rm -f "$tmp_file"
  else
    mv -f "$tmp_file" "$target_file"
  fi
}

# 合并 consumer.ets / callback.ets
for file_type in consumer callback; do
  output_file="${OUTPUT_DIR}/${file_type}.ets"
  tmp_file="${output_file}.tmp"
  echo "/***" > "$tmp_file"
  echo "*    !!!  GEN CODE DO NOT EDIT  !!!" >> "$tmp_file"
  echo "***/" >> "$tmp_file"
  for sub_dir in "${KNIOGEN_DIR}"/*/; do
    if [ -f "${sub_dir}${file_type}.d.ts" ]; then
      tail -n +4 "${sub_dir}${file_type}.d.ts" >> "$tmp_file"
    fi
  done
  update_if_changed "$tmp_file" "$output_file"
done

# 合并 provider.ets
provider_output="${OUTPUT_DIR}/provider.ets"
tmp_provider="${provider_output}.tmp"
echo "/***" > "$tmp_provider"
echo "*    !!!  GEN CODE DO NOT EDIT  !!!" >> "$tmp_provider"
echo "***/" >> "$tmp_provider"
echo 'import { getService } from "knoi"' >> "$tmp_provider"
echo '' >> "$tmp_provider"
for sub_dir in "${KNIOGEN_DIR}"/*/; do
  if [ -f "${sub_dir}provider.ets" ]; then
    sed '1,/^import/d' "${sub_dir}provider.ets" >> "$tmp_provider"
  fi
done
update_if_changed "$tmp_provider" "$provider_output"
